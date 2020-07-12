#!/bin/bash

set -e #Exit immediately if a command exits with a non-zero status.


event_deal_sqls=(
    "schema.sql"
    "data.sql")

sqlExecute() {
	path=$1
	shift
	sqls=("${@}")

	for file in "${sqls[@]}"
	do
		echo "- import: /$path/$file"
		mysql --default-character-set=utf8 -uroot -p1234 < "/$path/$file"
	done
}

sqlExecute "db" "${event_deal_sqls[@]}"