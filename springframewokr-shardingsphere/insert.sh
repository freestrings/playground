#!/usr/bin/env bash
for i in {0..12}
do
  curl localhost:8080/person\?seq=${i}
  curl localhost:8080/animal\?seq=${i}
done
