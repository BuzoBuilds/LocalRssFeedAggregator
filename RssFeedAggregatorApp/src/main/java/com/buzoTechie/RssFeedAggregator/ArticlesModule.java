package com.buzoTechie.RssFeedAggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 
import java.io.StringReader;

public class ArticlesModule { 
    String rssLink = "https://hackernoon.com/feed";  

    public String getFeed() throws IOException, ParserConfigurationException, SAXException{
        String output = makeRssFeedRequest(rssLink); 
        ArrayList<Article> feed = parseRssFeed(output);
        return feed.toString();
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

    private ArrayList<Article> parseRssFeed(String xmlRssFeed) throws ParserConfigurationException, SAXException, IOException{
        InputSource is = new InputSource(new StringReader(xmlRssFeed));  
        RssParser rssParser = new RssParser();
        SAXParserFactory factory = SAXParserFactory.newInstance(); 
        SAXParser saxParser = factory.newSAXParser(); 
        
        saxParser.parse(is, rssParser);
        return rssParser.getArticles(); 
    }



    public static class Article{
        public Article(String title, String channel, String description, String link){
            this.title = title; 
            this.channel = channel; 
            this.description = description; 
            this.link = link;
        }  
        public Article(){};

        String title; 
        String channel; 
        String description; 
        String link;  

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder(); 
            sb.append("[\n"); 
            sb.append(" Channel: " + this.channel + "\n"); 
            sb.append(" Title: " + this.title + "\n"); 
            sb.append(" Desc: " + this.description + "\n"); 
            sb.append(" Link: " + this.link + "\n"); 
            sb.append("]"); 
            
            return sb.toString();

        }
    } 

    private static class RssParser extends DefaultHandler{
        ArrayList<Article> articles;
        String channel; 
        boolean inItem;  

        String currArticleTitle; 
        String currArticleDesc; 
        String currArticleLink;

        StringBuilder sb; 
        

        @Override
        public void startDocument(){
            articles = new ArrayList<Article>(); 
            inItem = false; 
            sb = new StringBuilder();
        } 

        @Override 
        public void startElement(String uri, String localName, String qName, Attributes attributes){
            sb = new StringBuilder(); //clear sb 

            if(qName.equalsIgnoreCase("channel")){
                inItem = false;
            }
            else if(qName.equalsIgnoreCase("item")){
                inItem = true;
                currArticleTitle = ""; 
                currArticleDesc = ""; 
                currArticleLink = "";
            } 
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(qName.equalsIgnoreCase("title")){
                if(!inItem){
                    channel = sb.toString();
                }
                else{
                    currArticleTitle = sb.toString();
                }
            }
            else if(qName.equalsIgnoreCase("link")){
                if(inItem){
                    currArticleLink = sb.toString();
                }
            } 
            else if(qName.equalsIgnoreCase("description")){
                if(inItem){
                    currArticleDesc = sb.toString();
                }
            }
            else if(qName.equalsIgnoreCase("item")){
                Article currArticle = new Article(currArticleTitle, channel,
                currArticleDesc, currArticleLink); 
                articles.add(currArticle);  
            } 
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            for(int i = start; i < start + length; i++){
                sb.append(ch[i]);
            }            
        }  

        public ArrayList<Article> getArticles(){
            return this.articles;
        }
    }



}
