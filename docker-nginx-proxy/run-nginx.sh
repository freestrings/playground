docker run --name nginx -d -v "$PWD"/nginx.conf:/etc/nginx/conf/nginx.conf -p 8082:80 --link nginx_api nginx