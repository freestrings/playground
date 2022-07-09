#!/bin/bash

docker cp create.sql mha-mha-master-1:/create.sql
docker exec -it mha-mha-master-1 bash -c "mysql -uroot -p1234 < /create.sql"

docker cp 10000000_1.sql mha-mha-master-1:/all.sql
docker exec -it mha-mha-master-1 bash -c "mysql -uroot -p1234 testa < /all.sql"