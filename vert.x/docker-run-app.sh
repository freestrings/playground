if [ "$#" -ne 2 ];
then
    echo "first arg: vertx id"
    echo "second arg: cpu numbers"
    echo "ex) ./docker-run-app.sh 1 1,2"
    exit
fi
docker run --name fs-vertx$1 \
    --rm \
    --link redis \
    --cpuset-cpus=$2 \
    -it \
    -p 808$1:8080 \
    freestrings/vert-x