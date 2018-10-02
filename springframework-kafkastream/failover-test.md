# 개별 컨슈머 테스트

> 컨슈머 시작1
>   - kafak2, kafka3
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \
    --link springframework-kafkastream_kafka2_1:kafka2 \
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-consumer \
    --bootstrap-server kafka3:29093,kafka2:29092 \
    --topic streams-wordcount-input \
    --from-beginning
```


> 컨슈머 시작2
>   - kafak2, kafka3
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \
    --link springframework-kafkastream_kafka2_1:kafka2 \
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-consumer \
    --bootstrap-server kafka3:29093,kafka2:29092 \
    --topic streams-wordcount-input \
    --from-beginning
```

> 프로듀서 시작
>   - kafka1, kafka2
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \면
    --link springframework-kafkastream_kafka2_1:kafka2 \면
    --link springframework-kafkastream_kafka3_1:kafka3 \면
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-producer \
    --broker-list kafka1:29091,kafka2:29092 \
    --topic streams-wordcount-input
```

> 컨슈머 시작1, 컨슈머 시작2 각각 메시지 뜸 

------------------------------------------------------------------------
# 컨슈머 그룹 테스트

> 컨슈머 시작1
>   - kafak2, kafka3
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \
    --link springframework-kafkastream_kafka2_1:kafka2 \
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-consumer \
    --bootstrap-server kafka3:29093,kafka2:29092 \
    --topic streams-wordcount-input \
    --consumer-property group.id=mygroup
```

> 컨슈머 시작2
>   - kafak2, kafka3
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \
    --link springframework-kafkastream_kafka2_1:kafka2 \
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-consumer \
    --bootstrap-server kafka3:29093,kafka2:29092 \
    --topic streams-wordcount-input \
    --consumer-property group.id=mygroup
```

> 컨슈머 시작3
>   - kafak2, kafka3
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \
    --link springframework-kafkastream_kafka2_1:kafka2 \
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-console-consumer \
    --bootstrap-server kafka3:29093,kafka2:29092 \
    --topic streams-wordcount-input \
    --consumer-property group.id=mygroup
```

> 1. 컨슈머 시작1, 컨슈머 시작2, 컨슈머 시작3 번갈아 가며 메시지 뜸 
> 2. kill 컨슈머 시작2
> 3. 컨슈머 시작1, 컨슈머 시작3 만 번갈아 가며 뜸

-----------------------------------------------------------------------------------
# 브로커 failover 테스트

> 토픽 조회
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \면
    --link springframework-kafkastream_kafka2_1:kafka2 \면
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-topics --describe \
    --topic streams-wordcount-input \
    --zookeeper zookeeper:32181
Topic:streams-wordcount-input	PartitionCount:13	ReplicationFactor:3	Configs:
	Topic: streams-wordcount-input	Partition: 0	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
	Topic: streams-wordcount-input	Partition: 1	Leader: 3	Replicas: 3,2,1	Isr: 3,2,1
	Topic: streams-wordcount-input	Partition: 2	Leader: 1	Replicas: 1,3,2	Isr: 1,3,2
	Topic: streams-wordcount-input	Partition: 3	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1
	Topic: streams-wordcount-input	Partition: 4	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2
	Topic: streams-wordcount-input	Partition: 5	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
	Topic: streams-wordcount-input	Partition: 6	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
	Topic: streams-wordcount-input	Partition: 7	Leader: 3	Replicas: 3,2,1	Isr: 3,2,1
	Topic: streams-wordcount-input	Partition: 8	Leader: 1	Replicas: 1,3,2	Isr: 1,3,2
	Topic: streams-wordcount-input	Partition: 9	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1
	Topic: streams-wordcount-input	Partition: 10	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2
	Topic: streams-wordcount-input	Partition: 11	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
	Topic: streams-wordcount-input	Partition: 12	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
```

> 브로커 kafka1 죽임
```
docker-compose kill kafka1
```

> 토픽 조회
```
docker run --rm -it \
    --link springframework-kafkastream_kafka1_1:kafka1 \면
    --link springframework-kafkastream_kafka2_1:kafka2 \면
    --link springframework-kafkastream_kafka3_1:kafka3 \
    --network springframework-kafkastream_default \
    confluentinc/cp-kafka:5.0.0 \
    kafka-topics --describe \
    --topic streams-wordcount-input \
    --zookeeper zookeeper:32181
Topic:streams-wordcount-input	PartitionCount:13	ReplicationFactor:3	Configs:
	Topic: streams-wordcount-input	Partition: 0	Leader: 2	Replicas: 2,1,3	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 1	Leader: 3	Replicas: 3,2,1	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 2	Leader: 3	Replicas: 1,3,2	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 3	Leader: 2	Replicas: 2,3,1	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 4	Leader: 3	Replicas: 3,1,2	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 5	Leader: 2	Replicas: 1,2,3	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 6	Leader: 2	Replicas: 2,1,3	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 7	Leader: 3	Replicas: 3,2,1	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 8	Leader: 3	Replicas: 1,3,2	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 9	Leader: 2	Replicas: 2,3,1	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 10	Leader: 3	Replicas: 3,1,2	Isr: 3,2
	Topic: streams-wordcount-input	Partition: 11	Leader: 2	Replicas: 1,2,3	Isr: 2,3
	Topic: streams-wordcount-input	Partition: 12	Leader: 2	Replicas: 2,1,3	Isr: 2,3
```

> 그러나, 프로듀서에서 값 입력하면 동작 안된다 (FIXME 되는게 정상인데,,)