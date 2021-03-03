package com.nyble.main;

import com.nyble.exceptions.RuntimeSqlException;
import com.nyble.facades.kafkaConsumer.KafkaConsumerFacade;
import com.nyble.managers.ProducerManager;
import com.nyble.processing.ActionProcessor;
import com.nyble.topics.Names;
import com.nyble.topics.consumerAttributes.ConsumerAttributesKey;
import com.nyble.topics.consumerAttributes.ConsumerAttributesValue;
import com.nyble.util.DBUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@SpringBootApplication(scanBasePackages = {"com.nyble.rest"})
public class App {

    final static String KAFKA_CLUSTER_BOOTSTRAP_SERVERS = "10.100.1.17:9093";
    final static Logger logger = LoggerFactory.getLogger(App.class);
    final static Properties consumerProps = new Properties();
    final static Properties producerProps = new Properties();
    public static ProducerManager producerManager;
    public static int lastRmcActionId;
    public static int lastRrpActionId;
    static{
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-recency-and-frequency");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        producerProps.put("bootstrap.servers", KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        producerProps.put("acks", "all");
        producerProps.put("retries", 5);
        producerProps.put("batch.size", 16384);
        producerProps.put("linger.ms", 1);
        producerProps.put("buffer.memory", 33554432);
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", IntegerSerializer.class.getName());
        producerManager = ProducerManager.getInstance(producerProps);
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            producerManager.getProducer().flush();
            producerManager.getProducer().close();
        }));
    }

    public static void initLastActionIds(){
        final String configName = "RECENCY_AND_FREQUENCY_LAST_ACTION_ID_%";
        final String lastActIdsQ = String.format("select vals[1]::int as rmc, vals[2]::int as rrp from\n" +
                "(\tselect array_agg(value order by key) as vals \n" +
                "\tfrom config_parameters cp \n" +
                "\twhere key like '%s'\n" +
                ") foo", configName);
        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(lastActIdsQ)){
            if(rs.next()){
                lastRmcActionId = rs.getInt("rmc");
                lastRrpActionId = rs.getInt("rrp");
            }else{
                throw new RuntimeException("Last action ids not set");
            }
        } catch (SQLException e) {
            throw new RuntimeSqlException(e.getMessage(), e);
        }
    }

    public static List<KafkaConsumerFacade<String, String>> initKafkaConsumers(){
        KafkaConsumerFacade<String, String> consumerActionsFacade = new KafkaConsumerFacade<>(consumerProps,
                2, KafkaConsumerFacade.PROCESSING_TYPE_BATCH);
        consumerActionsFacade.subscribe(Arrays.asList(Names.CONSUMER_ACTIONS_RMC_TOPIC, Names.CONSUMER_ACTIONS_RRP_TOPIC));
        consumerActionsFacade.startPolling(Duration.ofSeconds(10), ActionProcessor.class);

        return Collections.singletonList(consumerActionsFacade);
    }


    public static ScheduledExecutorService scheduleBatchUpdate(){
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable task = ()->{
            try {
                logger.info("Start update recency and frequency");
                updateCounts();
                logger.info("End update recency and frequency");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        };
        Duration delay = Duration.between(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS));
        scheduler.scheduleAtFixedRate(task, delay.toMillis(), Duration.ofHours(1).toMillis(), TimeUnit.MILLISECONDS);
        logger.info("Task will start in: "+delay.toMillis()+" millis");

        return scheduler;
    }

    public static void cleanUp(ExecutorService scheduler, ConfigurableApplicationContext restApp,
                               List<KafkaConsumerFacade<String, String>> consumerFacades ){
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            if(restApp != null){
                logger.warn("Closing rest server");
                restApp.close();
                logger.warn("Rest server closed");
            }
            if(scheduler != null){
                logger.warn("Closing decrement scheduler");
                scheduler.shutdown();
                try {
                    scheduler.awaitTermination(100, TimeUnit.SECONDS);
                    logger.warn("Decrement scheduler closed");
                } catch (InterruptedException e) {
                    logger.error("Force killed scheduler", e);
                }
            }
            for(KafkaConsumerFacade<String, String> consumerFacade : consumerFacades){
                try {
                    logger.warn("Closing kafka consumer facade");
                    consumerFacade.stopPolling();
                    logger.warn("Kafka consumer facade closed");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void main(String[] args) {

        try{
            ConfigurableApplicationContext restApp = SpringApplication.run(App.class, args);
            ExecutorService scheduler = scheduleBatchUpdate();
            initLastActionIds();
            List<KafkaConsumerFacade<String, String>> consumerFacades = initKafkaConsumers();
            cleanUp(scheduler, restApp, consumerFacades);
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            logger.error("EXITING");
        }
    }

    public static void updateCounts() throws Exception {
        Map<String, Integer> decrements = new HashMap<>();
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String startDate = getLastDate();
        Calendar now = new GregorianCalendar();
        now.add(Calendar.YEAR, -2);
        now.set(Calendar.MILLISECOND, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MINUTE, 0);
        final String endDate = sdf.format(now.getTime());
        final String currentTime = System.currentTimeMillis()+"";

        logger.info("Remove actions from {} to {}", startDate, endDate);

        final String query = String.format("select ca.system_id, ca.consumer_id, (-1)* count(*)\n" +
                "from consumer_actions ca join affinity.action_scores acts using (action_id, system_id) \n" +
                "where external_system_date >= '%s' and external_system_date < '%s'\n" +
                "group by ca.system_id, ca.consumer_id", startDate, endDate);

        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query)){
            st.setFetchSize(1000);
            while(rs.next()){
                int systemId = rs.getInt(1);
                int consumerId = rs.getInt(2);
                int cnt = rs.getInt(3);
                decrements.put(systemId+"#"+consumerId, cnt);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        for(Map.Entry<String, Integer> e : decrements.entrySet()){
            String[] consumerIdTokens = e.getKey().split("#");
            ConsumerAttributesKey cak = new ConsumerAttributesKey(Integer.parseInt(consumerIdTokens[0]),
                    Integer.parseInt(consumerIdTokens[1]));
            ConsumerAttributesValue cav = new ConsumerAttributesValue(consumerIdTokens[0],
                    consumerIdTokens[2],
                    "frequency", e.getValue()+"", currentTime, currentTime);
            String keyStr = cak.toJson();
            String valStr = cav.toJson();
            logger.debug("Sending {} and {} to {}", keyStr, valStr, Names.CONSUMER_ATTRIBUTES_TOPIC);
            producerManager.getProducer().send(new ProducerRecord<>(Names.CONSUMER_ATTRIBUTES_TOPIC, keyStr, valStr));
        }
        updateLastDate(endDate);
    }

    final static String potentialsLastDateKey = "RECENCY_AND_FREQUENCY_DECREMENT_FROM";
    public static String getLastDate() throws Exception {

        final String query = "SELECT value from config_parameters where key = '"+potentialsLastDateKey+"'";
        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query)){

            if(rs.next()){
                return rs.getString(1);
            }else{
                throw new Exception("Parameter "+potentialsLastDateKey+" not found");
            }
        }
    }

    public static void updateLastDate(String newDate) throws SQLException {
        final String query = "UPDATE config_parameters set value = '"+newDate+"' where key = '"+potentialsLastDateKey+"'";
        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement() ){
            logger.info("Updating last date");
            st.executeUpdate(query);
            logger.info("Updated last date");
        }
    }
}
