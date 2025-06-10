package com.buzoTechie.RssFeedAggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ArticlesModule { 
    String rssLink = "https://hackernoon.com/feed";  

    public ArrayList<Article> getLatestArticles(){ 
        return null;
        
    } 

    public void test() throws IOException{
        String output = makeRssFeedRequest(rssLink);
        System.out.println(output.substring(0, 500));
    }

    private String makeRssFeedRequest(String rssLink) throws IOException{
        URL url = new URL(rssLink); 
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection(); 
        con.setRequestMethod("GET"); 
        con.setInstanceFollowRedirects(true);  
        int responseCode = con.getResponseCode();
        if(responseCode != HttpsURLConnection.HTTP_OK){
            throw new IOException("Request to RSS Feed link " + rssLink + " failed with error number " + responseCode);
        } 

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader( new InputStreamReader(con.getInputStream()))){
            String inputLine; 
            while((inputLine = reader.readLine()) != null){
                sb.append(inputLine);
            }
        } 

        return sb.toString();
    }





    public static class Article{
        public Article(String title, String channel, String description, String link){
            this.title = title; 
            this.channel = channel; 
            this.description = description; 
            this.link = link;
        } 

        String title; 
        String channel; 
        String description; 
        String link; 
    }



}
