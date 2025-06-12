
PROJROOTDIR=$(dirname $0); 
TARGETDIR=$PROJROOTDIR/RssFeedAggregatorApp/target
java -cp "$TARGETDIR/*" com.buzoTechie.RssFeedAggregator.App > $PROJROOTDIR/Output/RssFeedResult.txt
