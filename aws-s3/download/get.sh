#!/bin/bash
docker run -it --rm -v `pwd`/downloaded:/downloaded freestrings/aws-s3-download node /download.js $1 $2 "directcs-docker-images-test" $3