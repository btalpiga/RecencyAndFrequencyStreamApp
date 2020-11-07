package com.nyble.main;

import com.nyble.managers.ProducerManager;
import com.nyble.topics.Names;
import com.nyble.topics.TopicObjectsFactory;
import com.nyble.topics.consumerActions.ConsumerActionsValue;
import com.nyble.topics.consumerAttributes.ConsumerAttributesKey;
import com.nyble.topics.consumerAttributes.ConsumerAttributesValue;
import com.nyble.util.DBUtil;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(scanBasePackages = {"com.nyble.rest"})
public class App {

    final static String KAFKA_CLUSTER_BOOTSTRAP_SERVERS = "10.100.1.17:9093";
    final static Logger logger = LoggerFactory.getLogger(App.class);
    final static String sigAppName = "recency-frequency";
    final static String appName = sigAppName+"-stream";
    final static Properties streamsConfig = new Properties();
    final static Properties producerProps = new Properties();
    static KafkaProducer<String, Integer> integerProducer;
    static KafkaProducer<String, String> stringProducer;
    static{
        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        streamsConfig.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        streamsConfig.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 2);
        streamsConfig.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        producerProps.put("bootstrap.servers", KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        producerProps.put("acks", "all");
        producerProps.put("retries", 5);
        producerProps.put("batch.size", 16384);
        producerProps.put("linger.ms", 1);
        producerProps.put("buffer.memory", 33554432);
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", IntegerSerializer.class.getName());
        integerProducer = new KafkaProducer<>(producerProps);
        producerProps.put("value.serializer", StringSerializer.class.getName());
        stringProducer = ProducerManager.getInstance(producerProps).getProducer();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            integerProducer.flush();
            integerProducer.close();

            stringProducer.flush();
            stringProducer.close();
        }));
    }

    public static void initSourceTopic(List<String> topics) throws ExecutionException, InterruptedException {
        //this values should be updated on redeployment, if actions are reloaded
        final int lastRmcActionId = 662871381;
        final int lastRrpActionId = 2281565;
        //
        final String consumerGroupId = sigAppName+"-source-creator";

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER_BOOTSTRAP_SERVERS);
        AdminClient adminClient = AdminClient.create(adminProps);
        Set<String> existingTopics = adminClient.listTopics().names().get();
        for(String checkTopic : topics){
            if(!existingTopics.contains(checkTopic)){
                int numPartitions = 4;
                short replFact = 2;
                NewTopic st = new NewTopic(checkTopic, numPartitions, replFact).configs(new HashMap<>());
                adminClient.createTopics(Collections.singleton(st));
            }
        }

        String sourceTopic = topics.get(0);
        Thread poolActionsThread = new Thread(()->{
            KafkaConsumer<String, String> kConsumer = new KafkaConsumer<>(consumerProps);
            kConsumer.subscribe(Arrays.asList(Names.CONSUMER_ACTIONS_RMC_TOPIC, Names.CONSUMER_ACTIONS_RRP_TOPIC));
            while(true){
                ConsumerRecords<String, String> records = kConsumer.poll(1000*10);
                records.forEach(record->{
                    String provenienceTopic = record.topic();
                    int lastAction;
                    if(provenienceTopic.endsWith("rmc")){
                        lastAction = lastRmcActionId;
                    } else if(provenienceTopic.endsWith("rrp")){
                        lastAction = lastRrpActionId;
                    } else {
                        return;
                    }

                    //filter actions
                    ConsumerActionsValue cav = (ConsumerActionsValue) TopicObjectsFactory
                            .fromJson(record.value(), ConsumerActionsValue.class);
                    if(Integer.parseInt(cav.getId()) > lastAction && ActionsDict.filter(cav)){
                        String actionDate = cav.getExternalSystemDate();
                        String systemId = cav.getSystemId();
                        String consumerId = cav.getConsumerId();
                        Recency.updateConsumerRecency(systemId, consumerId, actionDate);
                        integerProducer.send(new ProducerRecord<>(sourceTopic, systemId+"#"+consumerId, 1));
                        logger.debug("Create {} topic input",sourceTopic);
                    }
                });
            }
        });
        poolActionsThread.start();
    }

    public static void scheduleBatchUpdate(String intermediateTopic){
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable task = ()->{
            try {
                logger.info("Start update recency and frequency");
                updateCounts(intermediateTopic);
                logger.info("End update recency and frequency");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        };
        Duration delay = Duration.between(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS));
        scheduler.scheduleAtFixedRate(task, delay.toMillis(), Duration.ofHours(1).toMillis(), TimeUnit.MILLISECONDS);
        logger.info("Task will start in: "+delay.toMillis()+" millis");

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SpringApplication.run(App.class,args);

        final String sourceTopic = sigAppName+"-source";

        initSourceTopic(Collections.singletonList(sourceTopic));
        scheduleBatchUpdate(sourceTopic);

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(sourceTopic,Consumed.with(Serdes.String(), Serdes.Integer()))
                .groupBy((consumer, cnt)->consumer, Grouped.with(Serdes.String(), Serdes.Integer()))
                .reduce(Integer::sum)
                .toStream()
                .map( (consumer, actionCounts) -> {
                    String[] tokens = consumer.split("#");
                    final String key = "frequency";
                    final String now = new Date().getTime()+"";
                    String systemId = tokens[0];
                    String consumerId = tokens[1];
                    ConsumerAttributesKey cak = new ConsumerAttributesKey(Integer.parseInt(systemId), Integer.parseInt(consumerId));
                    ConsumerAttributesValue cav = new ConsumerAttributesValue(systemId, consumerId,
                            key, actionCounts+"", now, now);
                    return KeyValue.pair(cak.toJson(), cav.toJson());
                })
                .to(Names.CONSUMER_ATTRIBUTES_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        Topology topology = builder.build();
        logger.debug(topology.describe().toString());
        KafkaStreams streams = new KafkaStreams(topology, streamsConfig);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    public static void updateCounts(String intermediateTopic) throws Exception {
        Map<String, Integer> decrements = new HashMap<>();
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String startDate = getLastDate();
        Calendar now = new GregorianCalendar();
        now.add(Calendar.YEAR, -2);
        now.set(Calendar.MILLISECOND, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MINUTE, 0);
        final String endDate = sdf.format(now.getTime());

        logger.info("Remove actions from {} to {}", startDate, endDate);

        final String query = String.format("select ca.system_id, ca.consumer_id, (-1)* count(*)\n" +
                "from consumer_actions ca join affinity.action_scores acts on ca.action_id = acts.action_id \n" +
                "where external_system_date >= '%s' and external_system_date < '%s'\n" +
                "group by ca.system_id, ca.consumer_id", startDate, endDate);

        try(Connection conn = DBUtil.getInstance().getConnection("datawarehouse");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query)){
            st.setFetchSize(1000);
            while(rs.next()){
                int systemId = rs.getInt(1);
                int consumerId = rs.getInt(2);
                int cnt = rs.getInt(2);
                decrements.put(systemId+"#"+consumerId, cnt);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        for(Map.Entry<String, Integer> e : decrements.entrySet()){
            String keyStr = e.getKey();
            Integer valStr = e.getValue();
            logger.debug("Sending {} and {} to {}", keyStr, valStr, intermediateTopic);
            integerProducer.send(new ProducerRecord<>(intermediateTopic, keyStr, valStr));
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
