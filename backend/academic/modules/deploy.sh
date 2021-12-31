#!/usr/bin/bash

if [ $# -gt 1 ]; then
  echo "too few arguments"
  exit 1
fi

if [[ $1 =~ "--docker" ]]; then
  f="-f Dockerfile"
  v="-v /var/java/logs/:/var/java/logs/"
  net="--net host"
  cd ./eureka && \
  docker build "${f}" -t academic-eureka . && \
  docker run "${v}" --name server-eureka "${net}" academic-eureka && \
  cd ../gateway && \
  docker build "${f}" -t academic-gateway . && \
  docker run "${v}" --name client-gateway "${net}" academic-gateway && \
  cd ../search && \
  docker build "${f}" -t academic-search . && \
  docker run "${v}" --name client-search "${net}" academic-search && \
  cd ../account && \
  docker build "${f}" -t academic-account . && \
  docker run "${v}" --name client-account "${net}" academic-account && \
  cd ../analysis && \
  docker build "${f}" -t academic-analysis . && \
  docker run "${v}" -v /data/tmp/analysis:/data/tmp/analysis --name client-analysis "${net}" academic-analysis && \
  cd ../spider && \
  docker build "${f}" -t academic-analysis . && \
  docker run "${v}" -v /data/tmp/analysis:/data/tmp/analysis --name client-analysis "${net}" academic-analysis && \
  cd ../scholar &&
  docker build "${f}" -t academic-scholar . && \
  docker run "${v}" --name client-scholar "${net}" academic-scholar && \
  cd ../admin && \
  docker build "${f}" -t academic-admin . && \
  docker run "${v}" --name client-admin "${net}" academic-admin && \
  cd ../resource && \
  docker build "${f}" -t academic-resource . && \
  docker run "${v}" -v /data/resources/:/data/resources/ --name client-resource "${net}" academic-resource
  cd ..
elif [[ $1 =~ "--non-docker" ]]; then
  profiles="--spring.profiles.active=prod"
  java -jar ./eureka/*.jar "${profiles}" &
  java -jar ./gateway/*.jar "${profiles}" &
  java -jar ./search/*.jar "${profiles}" &
  java -jar ./account/*.jar "${profiles}" &
  java -jar ./analysis/*.jar "${profiles}" &
  java -jar ./spider/*.jar "${profiles}" &
  java -jar ./scholar/*.jar "${profiles}" &
  java -jar ./admin/*.jar "${profiles}" &
  java -jar ./resource/*.jar "${profiles}" &
fi
