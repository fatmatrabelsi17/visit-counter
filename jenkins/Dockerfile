# Jenkins + client Docker : les jobs utilisent le démon Docker de l'hôte (Docker Desktop)
FROM jenkins/jenkins:lts-jdk17
USER root
RUN apt-get update && apt-get install -y --no-install-recommends docker.io \
    && rm -rf /var/lib/apt/lists/*
RUN jenkins-plugin-cli --plugins git github workflow-aggregator docker-workflow credentials-binding junit timestamper
USER jenkins
