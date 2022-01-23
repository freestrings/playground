docker run --rm -it --name zdt \
    --cap-add=NET_ADMIN \
    -v `pwd`/conf/nginx.1:/etc/nginx.1:ro \
    -v `pwd`/conf/nginx.2:/etc/nginx.2:ro \
    -v `pwd`/conf/nginx.backend:/etc/nginx.backend:ro \
    -v `pwd`/conf/envoy:/etc/envoy \
    -v `pwd`/shl:/shl \
    -v `pwd`/repo/nginx.repo:/etc/yum.repos.d/nginx.repo \
    -p 10080:80 \
    -p 18001:8001 \
    -p 18000:8000 \
    oraclelinux:7 bash
