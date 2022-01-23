docker run --rm -it --name zdt \
    --cap-add=NET_ADMIN \
    -v `pwd`/conf/nginx.1:/etc/nginx.1:ro \
    -v `pwd`/conf/nginx.2:/etc/nginx.2:ro \
    -v `pwd`/shl:/shl \
    freestrings/zdt bash
