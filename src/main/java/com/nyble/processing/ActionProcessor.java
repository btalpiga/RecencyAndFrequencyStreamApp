package com.nyble.processing;

import com.nyble.facades.kafkaConsumer.RecordProcessor;
import com.nyble.main.ActionsDict;
import com.nyble.main.App;
import com.nyble.topics.Names;
import com.nyble.topics.TopicObjectsFactory;
import com.nyble.topics.consumerActions.ConsumerActionsValue;
import com.nyble.topics.consumerAttributes.ConsumerAttributesKey;
import com.nyble.topics.consumerAttributes.ConsumerAttributesValue;
import com.nyble.types.ConsumerActionDescriptor;
import com.nyble.types.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ActionProcessor implements RecordProcessor<String, String> {

    private final static Logger logger = LoggerFactory.getLogger(ActionProcessor.class);

    @Override
    public boolean process(ConsumerRecord<String, String> consumerRecord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean processBatch(ConsumerRecords<String, String> consumerRecords) {
        Map<String, Pair<Long, Integer>> consumersCache = new HashMap<>();
        final String now = System.currentTimeMillis()+"";

        consumerRecords.forEach(action->{
            ConsumerActionsValue actionValue = (ConsumerActionsValue) TopicObjectsFactory.fromJson(action.value(), ConsumerActionsValue.class);
            int systemId = action.topic().contains("rmc") ? Names.RMC_SYSTEM_ID : Names.RRP_SYSTEM_ID;
            if( (systemId == Names.RMC_SYSTEM_ID && Integer.parseInt(actionValue.getId()) <= App.lastRmcActionId) ||
                    (systemId == Names.RRP_SYSTEM_ID && Integer.parseInt(actionValue.getId()) <= App.lastRrpActionId )){
                return;
            }
            if(ActionsDict.filter(actionValue)){
                String consumerIdentification = actionValue.getSystemId()+"#"+actionValue.getConsumerId();
                Pair<Long, Integer> currentConsumerStats = consumersCache.get(consumerIdentification);
                if(currentConsumerStats == null){
                    currentConsumerStats = new Pair<>(Long.parseLong(actionValue.getExternalSystemDate()), 1);
                    consumersCache.put(consumerIdentification, currentConsumerStats);
                }else{
                    long receivedActionDate = Long.parseLong(actionValue.getExternalSystemDate());
                    long lastRecency = currentConsumerStats.getLeft();
                    currentConsumerStats.setRight(currentConsumerStats.getRight()+1);
                    if(receivedActionDate > lastRecency){
                        currentConsumerStats.setLeft(receivedActionDate);
                    }
                }
            }
        });

        for(Map.Entry<String, Pair<Long, Integer>> e : consumersCache.entrySet()){
            Pair<Long,Integer> consumerStats = e.getValue();
            String[] consumerIdTokens = e.getKey().split("#");
            String recencyPropName = "recency";
            String frequencyPropName = "frequency";
            String incrementFrequency = "+"+consumerStats.getRight();

            ConsumerAttributesKey cak = new ConsumerAttributesKey(Integer.parseInt(consumerIdTokens[0]),
                    Integer.parseInt(consumerIdTokens[1]));
            ConsumerAttributesValue cav = new ConsumerAttributesValue(consumerIdTokens[0],
                    consumerIdTokens[2],
                    recencyPropName, consumerStats.getLeft()+"", now, now);
            String keyStr = cak.toJson();
            String valStr = cav.toJson();
            logger.debug("Sending {} and {} to {}", keyStr, valStr, Names.CONSUMER_ATTRIBUTES_TOPIC);
            App.producerManager.getProducer().send(new ProducerRecord<>(Names.CONSUMER_ATTRIBUTES_TOPIC, keyStr, valStr));

            cav = new ConsumerAttributesValue(consumerIdTokens[0],
                    consumerIdTokens[2],
                    frequencyPropName, incrementFrequency, now, now);
            keyStr = cak.toJson();
            valStr = cav.toJson();
            logger.debug("Sending {} and {} to {}", keyStr, valStr, Names.CONSUMER_ATTRIBUTES_TOPIC);
            App.producerManager.getProducer().send(new ProducerRecord<>(Names.CONSUMER_ATTRIBUTES_TOPIC, keyStr, valStr));
        }

        return true;
    }
}
