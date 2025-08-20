#!/bin/bash
PROJROOTDIR=$(dirname $0); 
if [[ ! -d ./logs ]]; then 
	mkdir logs 
fi

if [[ ! -f ./userConfig ]]; then 
	echo "*** ERROR ***: Need to fill out userConfig"
	exit 1
fi

cd $PROJROOTDIR/RssFeedAggregatorApp/ > /dev/null
mvn -q exec:java -Dexec.mainClass="com.buzoTechie.RssFeedAggregator.App"
