#!/bin/sh


min=101
max=116

##############################
# Modify these parameters:
PWD=/Users/Sammy/Desktop/Launch_Codes
CONFIG_FILE_PATH=config.txt


mkdir /Users/Sammy/Desktop/Launch_Codes || echo "directory already exists";
cd $PWD


##############################
# generate launcher files
for i in $(seq $min $max); do
    echo \#\!/bin/sh > launch$i.sh
    echo cd $PWD >> launch$i.sh
    echo echo $i >> launch$i.sh
    echo sshpass -p 'raspberry' ssh pi@192.168.2.$i >> launch$i.sh
    chmod +rwx launch$i.sh
done

##############################
# Launch files

for i in $(seq $min $max); do
    echo launching $i
    open -a Terminal launch$i.sh
    sleep 1
done

