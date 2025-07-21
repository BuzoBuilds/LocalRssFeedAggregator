package com.buzoTechie.RssFeedAggregator;

import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Hello world!
 */
public class App { 

    ArticlesModule articlesModule; 
    EmailModule emailModule;
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        App rssApp = new App(); 
        rssApp.runCLI();
        //EmailModule emailModule = new EmailModule()
        // String feed = articlesModule.getFeed(); 
        // System.out.println(feed);
    } 


    public App() throws ParserConfigurationException, SAXException{
        articlesModule = new ArticlesModule("/home/amby/reposTheBuzoTechie/rssFeedAggregator/res/articleSources", 50, 500); 
    }

    private void runCLI(){
        StringBuilder helperSb = new StringBuilder(); 
        helperSb.append("***** Rss Feed Aggregator CLI Commands *****\n"); 
        helperSb.append("help: view commands\n");
        helperSb.append("lssrc: list currently stored rss source links and their indexes\n"); 
        helperSb.append("addsrc rssLink: add new rss source link to rss source file\n"); 
        helperSb.append("quit: exit program\n"); 
        String helperMsg = helperSb.toString();  

        String errMsg = "Invalid command\n";
        String prompt = "RssFeedAgg :> ";  

        try (Scanner scanner = new Scanner(System.in)) {
            
            while(true){
                //prompt user  
                System.out.print(prompt);
                String inputLine = scanner.nextLine(); 

                //preprocess
                inputLine = inputLine.trim();
                String[] splitedInputLine = inputLine.split("\\s+");  

                if(splitedInputLine[0].equalsIgnoreCase("help")){
                    System.out.println(helperMsg);
                }
                else if(splitedInputLine[0].equalsIgnoreCase("lssrc")){
                    this.listSrcs();
                }
                else if(splitedInputLine[0].equalsIgnoreCase("addsrc")){
                    if(splitedInputLine.length < 2){
                        Utils.printError("Invalid args for addsrc command");
                        System.out.println(helperMsg); 
                        continue;
                    }
                    this.addSrc(splitedInputLine[1]);
                }
                else if(splitedInputLine[0].equalsIgnoreCase("quit")){
                    return;
                }
                else{
                    System.out.println(errMsg);  
                    System.out.println(helperMsg);
                }
                
                
            }

        }
    } 

    private void listSrcs(){
        System.out.println(articlesModule.listSrcs());
    } 

    private void addSrc(String rssLink){
        articlesModule.addSrc(rssLink); 
        Utils.printMsg("Sucsessfully added rss source link: " + rssLink);
    }
}
