#!/bin/bash
#Script para executar pela linha de comando

i=0
while [ $i -le `expr $1 - 1` ]; do
    echo "-------------------------------------------------------------"
    echo "Amostra $i"
    echo "-------------------------------------------------------------"   

#Para aumento da Java Heap e definicao da arquitetura
#    java -Xmx4196M -d64 -cp /usr/local/src/workspace/REALcloudSim/classes/:/usr/local/src/workspace/REALcloudSim/jars/mysql-connector-java-5.0.5-bin.jar org.cloudbus.cloudsim.A_PLI_AG_NS2 

    java -cp /usr/local/src/workspace/REALcloudSim/classes/:/usr/local/src/workspace/REALcloudSim/jars/mysql-connector-java-5.0.5-bin.jar org.cloudbus.cloudsim.A_GA_SA

    i=`expr $i + 1`
done

