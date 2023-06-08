#!/bin/bash
# This script is used to deploy the application to the server
# Author: Filip Nowak
# Date: 04/05/2023


# Variables
#instance_name="youth-council-$(date +%s)"
instance_name="youth-council"
zone="europe-west1-b"
project="infra3-leemans-freddy"
network="default"
db_instance="postgres-sql-1684825152"
tags="http-server"
bucket="yc-01"

# Delete instance if it exists
gcloud compute instances delete $instance_name --zone=$zone --quiet

# Create instance
gcloud compute instances create $instance_name \
--zone=$zone \
--project=$project \
--machine-type=e2-small \
--subnet=default \
--network=$network \
--tags=$tags \
--scopes=https://www.googleapis.com/auth/cloud-platform \
--metadata BUCKET=$bucket,startup-script='#!/bin/bash
# Install dependencies
apt-get update
apt-get -y install openjdk-17-jdk
iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8080

# Get the files we need
gsutil cp gs://yc-01/fatjar.jar .

gcloud sql instances patch ycdb --authorized-networks=$(curl -s icanhazip.com) --quiet

# Start server
java -jar -Dspring.profiles.active=prod fatjar.jar
'


echo "Adding IP to authorized networks"
# Get IP and add to networks
ip=$(curl -4 icanhazip.com)
ip+="/32"
authorized_networks=$(gcloud sql instances describe $db_instance \
    --format="value(settings.ipConfiguration.authorizedNetworks.value)")
if [ -z "$authorized_networks" ]
then
    authorized_networks=$ip
else
    # append
    authorized_networks+=","
    authorized_networks+=$ip
fi
# patch instance
gcloud sql instances patch $db_instance \
    --authorized-networks=$authorized_networks \
    --quiet

gsutil cp gs://yc-01/data_prod.sql .

sudo apt install postgresql postgresql-contrib -y

# import file to database

# get db instance ip
db_ip=$(gcloud sql instances describe $db_instance \
    --format="value(ipAddresses.ipAddress)")

psql -h "$db_ip" -U postgres -d postgres -f data_prod.sql
