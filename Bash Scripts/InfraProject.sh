#!/bin/bash

# Description	: This script deploys a web app on a newly instantiated VM and Cloud SQL database. [ config dhcp and dns ... ]

# Set variables
PROJECT_ID="infra3-leemans-freddy"
BUCKET_NAME="bucket-$(date +%s)"
VM_NAME="project-vm-$(date +%s)"
ZONE="europe-west1-b"
MACHINE_TYPE="e2-standard-2"
IMAGE_FAMILY="ubuntu-2204-lts"
IMAGE_PROJECT="ubuntu-os-cloud"
TAGS="http-server"
STARTUP_SCRIPT="#!/bin/bash
sudo apt-get update
sudo apt-get install -y apache2
sudo apt-get install ufw
sudo ufw --force enable
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo apt-get install -y postgresql-client
gsutil -m cp -r gs://$BUCKET_NAME/* /var/www/html"
DB_NAME="postgres-db-$(date +%s)"
DB_SERVER_NAME="postgres-sql-$(date +%s)"
DB_TIER="db-g1-small"
REGION="europe-west1"
DB_VERSION="POSTGRES_14"
USER="user-$(date +%s)"
VPC_NAME="vpc-$(date +%s)"
SUBNET_NAME="subnet-$(date +%s)"
FIREWALL_RULE="web-traffic"
PORTS="tcp:80,tcp:443"

# Creates the Cloud SQL instance
create_sql() { 
	echo "Creating Cloud SQL instance..."
	gcloud sql instances create $DB_SERVER_NAME \
		--project=$PROJECT_ID \
		--tier=$DB_TIER \
		--region=$REGION \
		--authorized-networks=0.0.0.0/0 \
		--database-version=$DB_VERSION \
		--root-password=root
}

# Create Cloud database and user
create_db() { 
	echo "Creating database and user..."
	gcloud sql databases create $DB_NAME \
		--instance=$DB_SERVER_NAME
	read -s -p "Enter password for $USER: " passwd
	gcloud sql users create $USER --instance=$DB_SERVER_NAME --password=$passwd
	
	echo "Setting up db structure..." # TODO
	gcloud sql connect $DB_SERVER_NAME --user=$USER --password=$passwd --quiet \
		--project=$PROJECT_NAME --database=$DB_NAME <<EOF
CREATE TABLE public.advertisement
(
    id           bigserial PRIMARY KEY,
    description  varchar(255) NOT NULL,
    email        varchar(255) NOT NULL,
    image        varchar(255) NOT NULL,
    phone_number varchar(255) NOT NULL,
    title        varchar(255) NOT NULL
);

ALTER TABLE public.advertisement OWNER TO postgres;

);
EOF
	exit
}

# Clone the web page repo, Create Cloud Storage bucket and upload the static files
create_bucket() { 
	echo "Creating storage bucket..."
	gsutil mb -p $PROJECT_ID gs://$BUCKET_NAME
	gsutil iam ch allUsers:objectViewer gs://$BUCKET_NAME
	
	echo "Downloading git repo..."
	git clone https://gitlab.com/noah.diderich/infra3_project.git
	cd infra3_project/src/main/resources/
	gsutil -m rsync -r . gs://$BUCKET_NAME
}

# Creates the VM
create_vm() { 
	echo "Creating VM..."
	gcloud compute instances create $VM_NAME \
		--project=$PROJECT_ID \
		--zone=$ZONE \
		--machine-type=$MACHINE_TYPE \
		--image-family=$IMAGE_FAMILY \
		--image-project=$IMAGE_PROJECT \
		--network=$VPC_NAME \
		--subnet=$SUBNET_NAME \
		--private-network-ip="10.0.0.10"
		--metadata="startup-script=$STARTUP_SCRIPT"
}

# Creates and configures the VPC
create_vpc() { 
	echo "Creating VPC network..."
	gcloud compute networks create $VPC_NAME \
		--project=$PROJECT_ID \
		--subnet-mode=custom \
		--bgp-routing-mode=regional

	gcloud compute networks subnets create $SUBNET_NAME \
	       	--project=$PROJECT_ID \
		--region=$REGION \
		--network=$VPC_NAME \
		--range=10.0.0.0/24

	gcloud compute firewall-rules create $FIREWALL_RULE \
		--project=$PROJECT_ID \
		--network=$VPC_NAME \
		--allow=$PORTS \
		--target-tags=$TAGS \
		--description="Allow HTTP and HTTPS traffic"

	gcloud compute networks subnets update $SUBNET_NAME \
		--project=$PROJECT_ID \
		--region=$REGION \
		--add-secondary-ranges=private-range=10.0.1.0/24
}

# Set up private connection from VM to SQL
setup_private() { 
	echo "Establish private connection from VM to SQL..."
	gcloud sql instances patch $DB_SERVER_NAME \
		--project=$PROJECT_ID \
		--network=$VPC_NAME \
		--no-assign-ip \
		--authorized-networks=10.0.0.10/32
}

#create_sql
#create_db
#create_bucket
create_vpc
create_vm
setup_private
