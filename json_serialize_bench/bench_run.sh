#!/bin/bash

set -e

file_path=`pwd`/example.json
curr=`pwd`

cd "${curr}"/jackson-bench && ./gradlew jar > /dev/null 2>&1
cd "${curr}"/rust-serde-bench && cargo build --release > /dev/null 2>&1
cd "${curr}"

__htop() {
	echo 
#htop -p "$1"
}

__java() {
	java -jar jackson-bench/build/libs/jackson-benchmark.jar "${file_path}" $1 &
	_java_pid="$!"
	export _java_pid
	jconsole "${_java_pid}"
}

__rust() {
	rust-serde-bench/target/release/rust-serde-bench "${file_path}" $1 &
	_rust_pid="$!"
	export _rust_pid
}

if [ -z "$2" ]
then
	__java $1
	sleep 1
	__rust $1
	__htop "${_java_pid},${_rust_pid}"
fi

if [ "$2" == "java" ]
then 
	__java $1
	__htop "${_java_pid}"
fi

if [ "$2" == "rust" ]
then
	__rust $1
	__htop "${_rust_pid}"
fi
