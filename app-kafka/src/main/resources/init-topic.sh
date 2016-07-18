if (( $# > 0 ))
then
   case $1 in
       create) $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --partitions 10 --replication-factor 2 --topic my-topic
            ;;
       desc) $KAFKA_HOME/bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic my-topic
            ;;
       *) echo "./init-topic.sh [create|desc]"
            ;;
   esac
else
    echo "Usage: ./init-topic.sh [create|desc]"
fi
