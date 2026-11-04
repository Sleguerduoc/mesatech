#!/usr/bin/env bash
# EC2 A y B (Ubuntu 24.04): Java 17, cliente MySQL y carpetas de despliegue.
set -euo pipefail
sudo apt-get update -y
sudo apt-get install -y openjdk-17-jdk-headless mysql-client unzip curl
sudo mkdir -p /opt/mesatech /etc/mesatech
sudo chown ubuntu:ubuntu /opt/mesatech
sudo chmod 750 /etc/mesatech
java -version
mysql --version
