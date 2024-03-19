# Jenkins Builder image

FROM maven:3.9.6-amazoncorretto-17-debian

USER root

# Install docker CLI for docker images generation inside the container itself
#RUN curl -sSL https://get.docker.com/ |  sh

RUN apt update -y
RUN apt install -y curl
RUN curl https://get.docker.com/builds/Linux/x86_64/docker-latest.tgz | tar xvz -C /tmp/ && mv /tmp/docker/docker /usr/bin/docker

# Customize here your container with your instructions...


CMD ["cat"]
