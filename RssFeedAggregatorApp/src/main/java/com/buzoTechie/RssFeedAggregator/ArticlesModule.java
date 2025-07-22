package com.buzoTechie.RssFeedAggregator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;

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
    private int numOfArticlesInFeed;
    private int maxSizeOfFeedPool; 
    private Path articleSourcesFilePath; 

    //parsers 
    SAXParser saxParser;
    RssTypeParser typeParser; 
    RssParser rssParser; 
    AtomParser atomParser;
    

    public ArticlesModule(String articlesourcesFilePath, int numOfArticlesInFeed, int maxSizeOfFeedPool) throws ParserConfigurationException, SAXException{ 
        this.articleSourcesFilePath =  Path.of(articlesourcesFilePath);
        this.numOfArticlesInFeed = numOfArticlesInFeed; 
        this.maxSizeOfFeedPool = maxSizeOfFeedPool; 

        SAXParserFactory factory = SAXParserFactory.newInstance(); 
        saxParser = factory.newSAXParser(); 
        typeParser = new RssTypeParser(); 
        rssParser = new RssParser(); 
        atomParser = new AtomParser();
    }

    public void test() throws IOException, ParserConfigurationException, SAXException{
        String output = makeRssFeedRequest("https://hackernoon.com/feed");  
        System.out.println("hacker news type: -> " + determineRssTypeOfXmlFeed(output));

        output = makeRssFeedRequest("https://www.reddit.com/r/ExperiencedDevs/top/.rss?t=month");  
        System.out.println("reddit exp devs type: -> " + determineRssTypeOfXmlFeed(output));

        output = makeRssFeedRequest("https://martinfowler.com/feed.atom");  
        System.out.println("martin fowler type: -> " + determineRssTypeOfXmlFeed(output));
    }

    public String getFeed() throws IOException, ParserConfigurationException, SAXException{ 
        ArrayList<ArticleSource> articleSources = loadArticleSourcesFile(); 
        Collections.shuffle(articleSources); 

        ArrayList<Article> feedPool = new ArrayList<Article>();  
        for(int i = 0; i < articleSources.size(); i++){
            //to prevent us from eating too much memeory when there's many sources
            if(feedPool.size() > maxSizeOfFeedPool){
                break; 
            } 

            String output;
            try{
                output = makeRssFeedRequest(articleSources.get(i).link); 
            }
            catch(IOException e){
                Utils.printWarning(e.getMessage() + " skipping...");
                continue;
            } 

            ArrayList<Article> articlesFromCurrSrc = parseRssFeed(output);  
            int exEndIdx = numOfArticlesInFeed > articlesFromCurrSrc.size()? articlesFromCurrSrc.size() : numOfArticlesInFeed; 
            feedPool.addAll(articlesFromCurrSrc.subList(0, exEndIdx)); 
        }

        Collections.shuffle(feedPool); 
        int exEndIdx = numOfArticlesInFeed > feedPool.size()? feedPool.size() : numOfArticlesInFeed; 
        ArrayList<Article> feed  = new ArrayList<Article>();
        feed.addAll(feedPool.subList(0, exEndIdx));
        return feed.toString();
    } 

    private ArrayList<ArticleSource> loadArticleSourcesFile() throws IOException{
        ArrayList<ArticleSource> articleSourceList = new ArrayList<ArticleSource>();

        try(BufferedReader reader = Files.newBufferedReader(this.articleSourcesFilePath)){ 
            String line = reader.readLine(); 
            while(line != null){
                String[] splitedLine = line.split(",");
                articleSourceList.add(new ArticleSource(Integer.parseInt(splitedLine[0].trim()), splitedLine[1].trim()));
                line = reader.readLine();
            }  
        }

        return articleSourceList;
    }

    private String makeRssFeedRequest(String rssLink) throws IOException{
        URL url = new URL(rssLink); 
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection(); 
        con.addRequestProperty("User-Agent", "Mozilla/4.0"); 
        con.setRequestMethod("GET"); 
        con.setInstanceFollowRedirects(true);  
        int responseCode = con.getResponseCode();
        if(responseCode != HttpsURLConnection.HTTP_OK){
            if(con.getErrorStream() != null){
                Utils.printError(con.getErrorStream().toString());
            }
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

    public String listSrcs(){
        ArrayList<ArticleSource> articleSources;
        try {
            articleSources = this.loadArticleSourcesFile();
        } catch (IOException e) {
            System.err.println(e); 
            Utils.printError("rss src file can't be found");
            return null;
        }  

        StringBuilder sb = new StringBuilder(); 
        for(int i = 0; i < articleSources.size(); i++){
            sb.append(articleSources.get(i) + "\n"); 
        } 
        return sb.toString();
    } 

    public void addSrc(String rssSrcLink){
        ArrayList<ArticleSource> articleSources;
        try {
            articleSources = this.loadArticleSourcesFile();
        } catch (IOException e) {
            System.err.println(e); 
            Utils.printError("rss src file can't be found; Didn't add src");
            return;
        }    

        ArticleSource newSrc = new ArticleSource(articleSources.size(), rssSrcLink); 
        articleSources.add(newSrc); 
        try {
            this.writeArticleSrcListToSrcFile(articleSources);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.printError("Can't write to rss src file, didn't add new rss src");
        }     

        Utils.printMsg("Successfully added rss src " + rssSrcLink);
    } 
    
    public void rmSrc(int srcIndex){
        ArrayList<ArticleSource> articleSources; 
        try{
            articleSources = this.loadArticleSourcesFile();
            if(srcIndex >= articleSources.size() || srcIndex < 0){
                Utils.printError("srcIndex doesn't exist in rss source file");
                return;
            } 

            articleSources.remove(srcIndex); 
            this.writeArticleSrcListToSrcFile(articleSources);
        }
        catch(IOException e){
            System.err.println(e); 
            Utils.printError("rss src file can't be found; Didn't remove src");
            return;
        }

        Utils.printMsg("Successfully removed rss src at index " + srcIndex);
    }

    private void writeArticleSrcListToSrcFile(ArrayList<ArticleSource> articleSources) throws IOException{ 
        try(BufferedWriter writer = Files.newBufferedWriter(articleSourcesFilePath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            for(int i = 0; i < articleSources.size(); i++){
                ArticleSource src = articleSources.get(i); 
                String line = "" + src.index + "," + src.link; 
                if(i != articleSources.size() -1){
                    line += "\n";
                } 
                writer.write(line, 0, line.length());
            }
        }
    }

    private ArrayList<Article> parseRssFeed(String xmlRssFeed) throws ParserConfigurationException, SAXException, IOException{
        RssType rssType = determineRssTypeOfXmlFeed(xmlRssFeed); 

        switch(rssType){
            case RSS: {
                return parseRssFeedType(xmlRssFeed);
            }
            case ATOM: {
                return parseAtomFeedType(xmlRssFeed);
            }
            default : {
                return null;
            }
        }
    }

    private ArrayList<Article> parseRssFeedType(String xmlRssFeed) throws ParserConfigurationException, SAXException, IOException{ 
        InputSource is = new InputSource(new StringReader(xmlRssFeed));  
        saxParser.parse(is, this.rssParser);
        saxParser.reset();
        return this.rssParser.getArticles(); 
    } 
    
    private ArrayList<Article> parseAtomFeedType(String xmlRssFeed) throws ParserConfigurationException, SAXException, IOException{ 
        InputSource is = new InputSource(new StringReader(xmlRssFeed));  
        saxParser.parse(is, this.atomParser);
        saxParser.reset();
        return atomParser.getArticles(); 
    } 

    private RssType determineRssTypeOfXmlFeed(String xmlRssFeed) throws ParserConfigurationException, SAXException, IOException{
        InputSource is = new InputSource(new StringReader(xmlRssFeed));  
        saxParser.parse(is, this.typeParser);
        saxParser.reset();
        return typeParser.getType(); 
    }

    private enum RssType{
        RSS, 
        ATOM
    }


    private static class ArticleSource{
        int index; 
        String link; 
        public ArticleSource(int idx, String link){
            this.index = idx; 
            this.link = link;
        } 

        public String toString(){
            return "" + index + " -> " + link;
        }
    }

    private static class Article{
        public Article(String title, String channel, String description, String link){
            this.title = title; 
            this.channel = channel; 
            this.description = description; 
            this.link = link;
        }  
        private Article(){};

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
                    int endIdx = sb.toString().length() > 500? 500 : sb.toString().length();
                    currArticleDesc = sb.toString().substring(0, endIdx);
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


    private static class AtomParser extends DefaultHandler{
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

            if(qName.equalsIgnoreCase("entry")){
                inItem = true;
                currArticleTitle = ""; 
                currArticleDesc = ""; 
                currArticleLink = "";
            } 
            else if(qName.equalsIgnoreCase("link") && inItem){
                String link = attributes.getValue("href"); 
                currArticleLink = link != null? link : "UNKOWN";
    
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
            else if(qName.equalsIgnoreCase("summary")){
                if(inItem){
                    int endIdx = sb.toString().length() > 500? 500 : sb.toString().length();
                    currArticleDesc = sb.toString().substring(0, endIdx);
                    currArticleDesc = sb.toString();
                }
            }
            else if(qName.equalsIgnoreCase("entry")){
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

    private static class RssTypeParser extends DefaultHandler{  
        boolean atRoot = true;
        RssType type;
        @Override 
        public void startDocument(){
            atRoot = true; 
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes){
            if(atRoot){
                if(qName.equalsIgnoreCase("rss")){
                    type = RssType.RSS;
                } 
                else if(qName.equalsIgnoreCase("feed")){
                    type = RssType.ATOM;
                }
                else{
                    type = null;
                } 
                atRoot = false;
            }
        } 


        public RssType getType(){
            return this.type;
        }
    }

}
