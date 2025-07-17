package com.buzoTechie.RssFeedAggregator;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        ArticlesModule articlesModule = new ArticlesModule("/home/amby/reposTheBuzoTechie/rssFeedAggregator/res/articleSources", 50, 500); 
        String feed = articlesModule.getFeed(); 
        System.out.println(feed);
    }
}
