#!/bin/bash


# put code here to initialize pin 14 as an output pin
# double check this code:

# not my laptop
if [ "$HOSTNAME" != "E2-kupfer" ]; then
    echo "test";
    echo 14 > /sys/class/gpio/export || echo "14 already open";
    echo in > /sys/class/gpio/gpio14/direction;
    cat /sys/class/gpio/gpio14/value;
fi

FILENAME="DeployedNodeV0_02.jar"

# set the nodes to have reserved IPs
case $HOSTNAME in
("E2-kupfer") echo "on kupfer $FILENAME";;

("zes01") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.101 2 2;;
("zes02") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.102 2 4;;
("zes03") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.103 2 6;;
("zes04") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.104 2 8;;

("zes05") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.105 4 2;;
("zes06") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.106 4 4;;
("zes07") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.107 4 6;;
("zes08") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.108 4 8;;


#("zes01") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.101 40.4431325 -79.9423925;;
#("zes02") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.102 40.4431325 -79.9423375;;
#("zes03") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.103 40.4431325 -79.9422825;;
#("zes04") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.104 40.4431325 -79.9422275;;

#("zes05") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.105 40.4430775 -79.9423925;;
#("zes06") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.106 40.4430775 -79.9423375;;
#("zes07") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.107 40.4430775 -79.9422825;;
#("zes08") java -jar ~/DS-ZES/compiled_code/$FILENAME 192.168.2.108 40.4430775 -79.9422275;;

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