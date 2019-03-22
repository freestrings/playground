#!/bin/bash

for I in {1..1000}
do
	r=$(expr $I % 100)
	if [ "$r" == 0 ]; then printf "."
	fi
	target/release/jsonpath-crate-test
done

