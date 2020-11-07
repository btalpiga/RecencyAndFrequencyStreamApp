# RecencyAndFrequencyStreamApp
Kafka Stream application for computing recency and frequency consumer attributes

# Embedded web server
url= http://10.100.1.17:7005  
changeLog: PUT /logger `{"logName": "com.nyble", "logLevel": "warn"}`


# Installation

`cd ~/kits/confluent-5.5.1/`  

#### check that stream app RecencyAndFrequencyStreamApp.jar is not running

#### get the latest id from actions in database:
`select max(id) from consumer_actions where system_id = 1; --lastRmcActionId`  
`select max(id) from consumer_actions where system_id = 2; --lastRrpActionId`  
--update ids in the application and build it  
--update ids in the initial-recency-and-frequency-calc.sql  
`PGPASSWORD=postgres10@ nohup psql -U postgres -h localhost -d datawarehouse -f "/home/crmsudo/jobs/kafkaClients/scripts/initial-recency-and-frequency-calc.sql"&`

#### empty recency-frequency-source (source topic) :
`bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter --entity-type topics --entity-name recency-frequency-source --add-config retention.ms=10`  
--wait  
`bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter --entity-type topics --entity-name recency-frequency-source --delete-config retention.ms`  

#### use reset for recency-frequency-stream streams app:
`./bin/kafka-streams-application-reset --application-id recency-frequency-stream --input-topics  recency-frequency-source --bootstrap-servers 10.100.1.17:9093`

#### delete internal stream app topics
`./bin/kafka-topics --bootstrap-server 10.100.1.17:9093  --delete --topic <everything starting with recency-frequency-stream-...>`

#### start streams app

