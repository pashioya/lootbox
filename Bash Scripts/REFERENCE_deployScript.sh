#!/bin/bash
# This script is used to deploy the application to the server
# Author: Filip Nowak
# Date: 04/05/2023
# ------------------------------Variables---------------------------
instance_name="youth-council"
zone="europe-west1-b"
project="youth-council-cloud"
network="default"
db_instance="ycdb"
tags="http-server,https-server"
bucket="yc-01"

# -----------------------------Functions----------------------------

create_sql() {
  # If sql instance does not exist, create it
  if ! gcloud sql instances describe $db_instance --quiet; then
    # Create instance
    echo "Creating SQL instance"
    gcloud sql instances create $db_instance \
    --database-version=POSTGRES_14 \
    --cpu=1 \
    --memory=3840MiB \
    --zone=$zone \
    --project=$project \
    --root-password=postgres
    # Create database
    gcloud sql databases create ycdb --instance=$db_instance
  else
    echo "SQL instance already exists"
  fi

}

create_vm() {
  echo "Creating VM instance"
  # Delete instance if it exists
  gcloud compute instances delete $instance_name --zone=$zone --quiet

  # Create instance
  # shellcheck disable=SC2016
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
  iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 443

  # Get the files we need
  gsutil cp gs://yc-01/fatjar.jar .

  gcloud sql instances patch ycdb --authorized-networks=$(curl -s icanhazip.com) --quiet

  # Install psql
  apt-get -y install postgresql-client

  # Get sql ip
  db_ip=$(gcloud sql instances describe ycdb \
      --format="value(ipAddresses.ipAddress)" | cut -d";" -f1)

  # Copy sql file
  gsutil cp gs://yc-01/data_prod.sql .


  # DNS
  echo url="https://www.duckdns.org/update?domains=youth-council&token=d19f34c6-3d1d-4911-8f8b-44f335c18612&ip=" | curl -k -o ~/duckdns/duck.log -K -

#  # SSL
#  snap install --classic certbot
#  ln -s /snap/bin/certbot /usr/bin/certbot
#  certbot --apache -m filip.nowak@student.kdg.be -d youth-council.duckdns.org -n --agree-tos

  # Start server
  java -jar -Dspring.profiles.active=prod fatjar.jar

  # Connect to psql with password
  export PGPASSWORD="postgres"; psql -h "$db_ip" -U postgres -d postgres -f data_prod.sql
  '
}

# -------------------------------Main-------------------------------

create_sql
create_vm

# ------------------------------End--------------------------------
