#!/bin/bash

# Description	: This script deploys a web app on a newly instantiated VM and Cloud SQL database on a VPC and configures DNS

# Set variables
PROJECT_ID="infra3-leemans-freddy"
BUCKET_NAME="lootbox-bucketeu"
VM_NAME="lootbox-vm-1"
ZONE="europe-west1-b"
MACHINE_TYPE="e2-standard-2"
IMAGE_FAMILY="ubuntu-2204-lts"
IMAGE_PROJECT="ubuntu-os-cloud"
TAGS="http-server"
EXT_IP_ADDRESS="lootbox-vm-static"
STARTUP_SCRIPT='#!/bin/bash

# Install dependencies
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
sudo apt -y install apt-transport-https ca-certificates curl software-properties-common
sudo apt upgrade -y

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker-archive-keyring.gpg
sudo add-apt-repository -y "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

sudo apt-get install -y ufw docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Enable and configure UFW
ufw --force enable
ufw allow 22/tcp
ufw allow 80/tcp'
DB_NAME="postgres-sql-1684826042eu"
DB_SERVER_NAME="postgres-sql-1684825152eu"
DB_TIER="db-g1-small"
REGION="europe-west1"
DB_VERSION="POSTGRES_14"
USER="lootbox-db-eu"
VPC_NAME="lootbox-vpceu"
SUBNET_NAME="lootbox-subneteu"
SUBNET_RANGE="10.0.0.0/28"
FIREWALL_RULE="web-traffic-ssh-traffic"
PORTS="tcp:22,tcp:80,tcp:443"
DNS_ZONE="lootbox-store"
DNS_NAME="www.lootbox-store.site."

# Creates the Cloud SQL instance
create_sql() { 
	echo "Creating Cloud SQL instance..."
	gcloud sql instances create $DB_SERVER_NAME \
		--project=$PROJECT_ID \
		--tier=$DB_TIER \
		--region=$REGION \
		--network=$VPC_NAME \
		--database-version=$DB_VERSION \
		--authorized-networks=0.0.0.0/0 \
		--root-password=root
}

# Create Cloud database and user
create_db() { 
	echo "Creating database and user..."
	gcloud sql databases create $DB_NAME \
		--instance=$DB_SERVER_NAME
	read -s -p "Enter password for $USER: " passwd
	gcloud sql users create $USER --instance=$DB_SERVER_NAME --password=$passwd
}

# Clone the web page repo, Create Cloud Storage bucket and upload the static files
create_bucket() { 
	echo "Creating storage bucket..."
	gsutil mb -l $REGION -p $PROJECT_ID gs://$BUCKET_NAME
	gsutil iam ch allUsers:objectViewer gs://$BUCKET_NAME
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
		--address=$EXT_IP_ADDRESS \
		--private-network-ip="10.0.0.2" \
		--tags=$TAGS \
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
		--range=$SUBNET_RANGE

	gcloud compute firewall-rules create $FIREWALL_RULE \
		--project=$PROJECT_ID \
		--network=$VPC_NAME \
		--allow=$PORTS \
		--target-tags=$TAGS \
		--description="Allow HTTP and HTTPS traffic"

	gcloud compute networks subnets update $SUBNET_NAME \
		--project=$PROJECT_ID \
		--region=$REGION \
		--add-secondary-ranges=private-range=10.0.1.0/28
}

# Set up private connection from VM to SQL
setup_private() { 
	echo "Establish private connection from VM to SQL..."
	gcloud sql instances patch $DB_SERVER_NAME \
		--project=$PROJECT_ID \
		--require-ssl \
		--authorized-networks=10.0.0.2/32 \
		--quiet
}

# Create DNS zone and configure DNS settings
setup_dns() { 
	echo "Setting up DNS..."
	gcloud dns --project=$PROJECT_ID managed-zones create $DNS_ZONE \
		--description="" \
		--dns-name=$DNS_NAME \
		--visibility="public" \
		--dnssec-state="off"

	gcloud dns --project=$PROJECT_ID record-sets update $DNS_NAME \
		--type="A" \
		--zone=$DNS_ZONE \
		--rrdatas=$EXT_IP_ADDRESS \
		--ttl="300"
}

create_vpc
create_sql
create_db
create_bucket
create_vm
setup_private
setup_dns
