#!/usr/bin/env bash
./gradlew clean -x test build &&
  docker compose build &&
  docker compose up