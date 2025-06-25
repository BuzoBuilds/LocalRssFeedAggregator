package com.buzoTechie.RssFeedAggregator;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        ArticlesModule articlesModule = new ArticlesModule(); 
        EmailModule emailModule = new EmailModule("/home/amby/ProjCreds/rssEmailCreds"); 
    
        String feed = articlesModule.getFeed(); 
        emailModule.sendEmailToAddressesInAddressBook("FREAKED OUT GEEK OUT!!!", feed);
    }
}
