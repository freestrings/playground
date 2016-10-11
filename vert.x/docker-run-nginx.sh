if [ "$#" -ne 1 ];
then
    echo "./docker-run-nginx.sh <config number>"
    exit
fi

echo "config=> `pwd`/src/test/resources/nginx$1.conf:/etc/nginx/nginx.conf";

link=""
for (( i=1 ; i<=$1; i++))
do
    link+="--link fs-vertx$i "
done

docker run --rm --name nginx \
            $link \
            -it \
            -v `pwd`/src/test/resources/nginx$1.conf:/etc/nginx/nginx.conf:ro \
            nginx