#
# apache ab HTTP/1.0
#docker run --rm --link zdt jordi/ab -c 4 -n 100000 http://zdt/ 
docker run --rm -t --link zdt yokogawa/siege -d1 -r10 -c25 zdt