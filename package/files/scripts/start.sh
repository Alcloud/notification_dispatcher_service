#!/bin/sh

set -e  

# if $variable is not set, then default to $value
export server_port=${server_port:-8080}
# kafka config
export consumers=${consumers:-1}
export kafka_group_id=${kafka_group_id:-test}
export kafka_bootstrap_servers=${kafka_bootstrap_servers:-kafka:9092}
export notification_preference_db_host=${notification_preference_db_host:-notification-preference-db}
export notification_preference_db_port=${notification_preference_db_port:-27017}
export notification_db_host=${notification_db_host:-notification-db}
export notification_db_port=${notification_db_port:-27017}
export notification_mgmt_service_host=${notification_mgmt_service_host:-notification-mgmt-service}
export notification_mgmt_service_port=${notification_mgmt_service_port:-8080}
export subscribetopic=${subscribetopic:-notification}
export sendtopic=${sendtopic:-fcmnotification}

echo "Creating config files using confd"
/usr/local/bin/confd -onetime -backend env

#echo "Starting REST Service"
java -jar /tmp/notification_dispatcher_service.jar -conf /tmp/service.properties -s