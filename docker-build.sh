#!/bin/bash
## Remove existing docker images
docker image rm zeusprogetto/account-processor
mvn clean package
docker build -t zeusprogetto/account-processor:latest .
docker push zeusprogetto/account-processor