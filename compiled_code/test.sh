#!/bin/bash




FILENAME="DeployedNodeV0_11.jar"

# set the nodes to have reserved IPs
case $HOSTNAME in

("zes01") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.101 40.4431325 -79.9423925;;
("zes02") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.102 40.4431325 -79.9423375;;
("zes03") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.103 40.4431325 -79.9422825;;
("zes04") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.104 40.4431325 -79.9422275;;

("zes05") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.105 40.4430775 -79.9423925;;
("zes06") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.106 40.4430775 -79.9423375;;
("zes07") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.107 40.4430775 -79.9422825;;
("zes08") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.108 40.4430775 -79.9422275;;

("zes09") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.109 40.4430225 -79.9423925;;
("zes10") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.110 40.4430225 -79.9423375;;
("zes11") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.111 40.4430225 -79.9422825;;
("zes12") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.112 40.4430225 -79.9422275;;

("zes13") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.113 40.4429675 -79.9423925;;
("zes14") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.114 40.4429675 -79.9423375;;
("zes15") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.115 40.4429675 -79.9422825;;
("zes16") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.116 40.4429675 -79.9422275;;

(*)   echo "Default case";;
esac