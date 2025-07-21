package com.buzoTechie.RssFeedAggregator;

public class Utils {
    public static void printError(String msg){
        StringBuilder sb = new StringBuilder(); 
        sb.append("RSSFeedAggregator > --ERR--: "); 
        sb.append(msg); 
        System.out.println(sb.toString());
    }

    public static void printWarning(String msg){
        StringBuilder sb = new StringBuilder(); 
        sb.append("RSSFeedAggregator > --WRN--: "); 
        sb.append(msg); 
        System.out.println(sb.toString());
    } 

    public static void printMsg(String msg){
        StringBuilder sb = new StringBuilder(); 
        sb.append("RSSFeedAggregator > --INFO--: "); 
        sb.append(msg); 
        System.out.println(sb.toString());
    }
}
