#!/bin/bash
PROJROOTDIR=$(dirname $0); 
cd $PROJROOTDIR/RssFeedAggregatorApp 
echo -e "sendall\nquit\n" | mvn exec:java -Dexec.mainClass="com.buzoTechie.RssFeedAggregator.App"

