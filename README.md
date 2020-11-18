# RecencyAndFrequencyStreamApp
Kafka Stream application for computing recency and frequency consumer attributes

# Embedded web server
url= http://10.100.1.17:7005  
changeLog: PUT /logger `{"logName": "com.nyble", "logLevel": "warn"}`


# Installation
 
#### check that actions rmc/rrp kafka connectors are paused and recency-frequency consumers have lag = 0 
 
#### check that stream app RecencyAndFrequencyStreamApp.jar is not running

#### get the latest id from actions in database:
```
update config_parameters
set value = q.id  
from (select max(id) as id from consumer_actions where system_id = 1) q  
where key = 'RECENCY_AND_FREQUENCY_LAST_ACTION_ID_RMC';--lastRmcActionId
create temp table tmp as select id from consumer_actions ca where system_id = 2;
update config_parameters  
set value = q.id  
from (select max(id) as id from tmp) q  
where key = 'RECENCY_AND_FREQUENCY_LAST_ACTION_ID_RRP';--lastRmcActionId
drop table tmp;
```
```shell script
cd /home/crmsudo/jobs/kafkaClients/scripts && node utility.js --get-script name=recency-and-frequency replace=:lastRmcActionId^TODO replace=:lastRrpActionId^TODO
```

#### empty recency-frequency-source (source topic) : 
```shell script
~/kits/confluent-5.5.1/bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter \
--entity-type topics --entity-name recency-frequency-source --add-config retention.ms=10
```
--wait  
```shell script
~/kits/confluent-5.5.1/bin/kafka-configs --bootstrap-server 10.100.1.17:9093 --alter \
--entity-type topics --entity-name recency-frequency-source --delete-config retention.ms
```

#### use reset for recency-frequency-stream streams app:
```shell script
~/kits/confluent-5.5.1/bin/kafka-streams-application-reset --application-id recency-frequency-stream \
--input-topics  recency-frequency-source --bootstrap-servers 10.100.1.17:9093
```

#### delete internal stream app topics
`~/kits/confluent-5.5.1/bin/kafka-topics --bootstrap-server 10.100.1.17:9093  --delete --topic <everything starting with recency-frequency-stream-...>`

#### load initial state
```shell script
PGPASSWORD=postgres10@ nohup psql -U postgres -h localhost \
-d datawarehouse -f "/home/crmsudo/jobs/kafkaClients/scripts/initial-recency-and-frequency-calc.sql"&
```
```shell script
PGPASSWORD=postgres10@ nohup psql -U postgres -h localhost -d datawarehouse \
-c "\copy (select system_id||'#'||consumer_id,frequency from recency_and_frequency_start) to '/tmp/frequencies.csv' delimiter ';' csv" &
```
```shell script
cd /home/crmsudo/jobs/kafkaClients/scripts/kafkaToolsJava
./kafkaTools.sh producer --topic recency-frequency-source --bootstrap-server 10.100.1.17:9093 \
--value-serializer Integer --key-serializer String --format key-value --key-value-delimiter ";" \
--file /tmp/frequencies.csv
```

#### start streams app

