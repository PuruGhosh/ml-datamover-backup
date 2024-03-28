FROM artifacts.corp.xperi.com/ml2020-docker-local/ml-jdk-1.15-gradle:latest

LABEL maintainer="Kushal Karmakar"
LABEL email="kushal.karmakar@xperi.com"

# Setting up Docker Arguments
ARG ARTIFACT_URL=""
ARG SERVICE_NAME=""
ARG BUILD_VERSION=""

# setting up Present Working Directory
ARG ML_USER_HOME=/usr/local/app/

# Setting Up Present Working Directory
WORKDIR ${ML_USER_HOME}

# Copying the Java Jar file to /app directory
COPY build/libs/${SERVICE_NAME}-${BUILD_VERSION}.jar ${ML_USER_HOME}

# Exposing the Docker Port
EXPOSE 9700