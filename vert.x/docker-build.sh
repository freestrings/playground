#!/bin/bash

./gradlew clean shadowJar \
    && docker build --tag freestrings/vert-x .