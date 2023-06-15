# Steps Taken

- follow script
-  create a vpc 
-  create a sql
  - if it doesnt work remove the network line from the create sql method go to gcloud and manually add the network for the created vpc to the sql instance
- create db
- create bucket
- create reserved external ip with the name 'lootbox-vm-static'
- create vm instance 
- ssh into vm instance
- pull from freddykdg/lootbox docker repo
- run the freddykdg/lootbox with port 8080 exposed


## Docker repo explained
- make a fat jar of the lootbox project
- put the fat jar in the docker file directory
- create build a image using docker file and the fat jar
- push the image to docker hub