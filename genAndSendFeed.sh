PROJROOTDIR=$(dirname $0); 
cd $PROJROOTDIR/RssFeedAggregatorApp 
mvn exec:java -Dexec.mainClass="com.buzoTechie.RssFeedAggregator.App"

