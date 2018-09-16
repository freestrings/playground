docker run \
    --rm -it \
    -p 8000:8000 \
    -e "KAFKA_REST_PROXY_URL=http://localhost:8082"  landoop/kafka-topics-ui
