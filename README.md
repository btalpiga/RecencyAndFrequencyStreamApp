# RecencyAndFrequencyStreamApp
Kafka Stream application for computing recency and frequency consumer attributes

# Embedded web server
url= http://10.100.1.17:7005  
changeLog: PUT /logger `{"logName": "com.nyble", "logLevel": "warn"}`


# Installation

`cd ~/kits/confluent-5.5.1/`  

#### get the latest id from actions in database:
`select max(id) from consumer_actions where system_id = 1; --lastRmcActionId`  
`select max(id) from consumer_actions where system_id = 2; --lastRrpActionId`  
--update ids in the application and build it  
--update ids in the initial-recency-and-frequency-calc.sql  
`PGPASSWORD=postgres10@ nohup psql -U postgres -h localhost -d datawarehouse -f "/home/crmsudo/jobs/kafkaClients/scripts/initial-recency-and-frequency-calc.sql"&`

#### check that stream app RecencyAndFrequencyStreamApp.jar is not running

#### check that kafka connector jdbc_source_recency_and_frequency_start does not exist OR pause and delete it
#### reset kafka connector jdbc_source_recency_and_frequency_start offset
`./bin/kafka-console-producer --bootstrap-server 10.100.1.17:9093 --topic connect-consumer-action-offsets --property "parse.key=true" --property "key.separator=;"`  
`["jdbc_source_recency_and_frequency_start",{"query":"query"}];{"incrementing":0}`

#### empty recency-frequency-source (source topic) :
`bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter --entity-type topics --entity-name recency-frequency-source --add-config retention.ms=10`  
--wait  
`bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter --entity-type topics --entity-name recency-frequency-source --delete-config retention.ms`  

#### use reset for recency-frequency-stream streams app:
`./bin/kafka-streams-application-reset --application-id recency-frequency-stream --input-topics  recency-frequency-source --bootstrap-servers 10.100.1.17:9093`

#### delete internal stream app topics
`./bin/kafka-topics --bootstrap-server 10.100.1.17:9093  --delete --topic <everything starting with recency-frequency-stream-...>`

#### update consumers table and remove all recency and frequency attributes
`update consumers set payload = payload-'recency'-'frequency';`

#### start streams app

--create kafka connector jdbc_source_recency_and_frequency_start and wait until full table is loaded  
--pause and delete kafka connector jdbc_source_recency_and_frequency_start  

