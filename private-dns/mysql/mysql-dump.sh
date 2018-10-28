# mysql-dump.sh $host $user $password $database
docker run --rm \
    mysql mysqldump \
    -h $1 \
    -u$2 \
    -p$3 \
    --single-transaction \
    $4 \
    > ./dump.sql