#!/bin/bash

export KAFKA_ADVERTISED_HOST_NAME="aaaa"

for VAR in `env`
do
    if [[ $VAR =~ ^KAFKA_ && ! $VAR =~ ^KAFKA_HOME ]]; then
        kafka_name=`echo "$VAR" | sed -r "s/KAFKA_(.*)=.*/\1/g" | tr '[:upper:]' '[:lower:]' | tr _ .`
        #env_var=`echo "$VAR" | sed -r "s/(.*)=.*/\1/g"`
        #if egrep -q "(^|^#)$kafka_name=" $KAFKA_HOME/config/server.properties; then
        #    #sed -r -i "s@(^|^#)($kafka_name)=(.*)@\2=${!env_var}@g" $KAFKA_HOME/config/server.properties #note that no    config values may contain an '@' char
        #    echo $(sed "s@(^|^#)($kafka_name)=(.*)@\2=${!env_var}@g")
        #else
        #    echo "$kafka_name=${!env_var}" #>> $KAFKA_HOME/config/server.properties
        #fi
    fi
done
