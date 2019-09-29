# oracle xe 공식 이미지 없나보다. 찾아보고 사용!
# docker search oracle
#
docker run -it --rm --name oracle \
  -p 1521:1521 \
  -e ORACLE_ALLOW_REMOTE=true \
  -v ${PWD}/init.sql:/docker-entrypoint-initdb.d/
  oracleinanutshell/oracle-xe-11g