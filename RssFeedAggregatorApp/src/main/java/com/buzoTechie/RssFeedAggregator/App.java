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
    } 


    public App() throws ParserConfigurationException, SAXException, IOException{
        articlesModule = new ArticlesModule("/home/amby/reposTheBuzoTechie/rssFeedAggregator/res/articleSources", 50, 500); 
        emailModule = new EmailModule("/home/amby/ProjCreds/rssEmailCreds");
    }

    private void runCLI(){
        StringBuilder helperSb = new StringBuilder(); 
        helperSb.append("***** Rss Feed Aggregator CLI Commands *****\n"); 
        helperSb.append("help: view commands\n");
        helperSb.append("lssrc: list currently stored rss source links and their indexes\n"); 
        helperSb.append("addsrc rssLink(string): add new rss source link to rss source file\n"); 
        helperSb.append("rmsrc index(int): removes rss source at the index from the rss source file\n"); 
        helperSb.append("genfeed: uses the rss src file to generate a feed and writes it to standard output\n");
        helperSb.append("sendall: generates feed and sends to all emails in address book immediately\n");
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
                else if(splitedInputLine[0].equalsIgnoreCase("rmsrc")){
                    if(splitedInputLine.length < 2){
                        Utils.printError("Invalid args for rmsrc command");
                        System.out.println(helperMsg); 
                        continue;
                    } 
                    int idx; 
                    try{
                        idx = Integer.parseInt(splitedInputLine[1]); 
                    }
                    catch(NumberFormatException e){
                        Utils.printError("rmsrc argument is supposed to be an integer");
                        System.out.println(helperMsg); 
                        continue;
                    }
                    this.rmSrc(idx);
                }
                else if(splitedInputLine[0].equalsIgnoreCase("genfeed")){
                    this.genFeed();
                }
                else if(splitedInputLine[0].equalsIgnoreCase("sendall")){
                    this.sendall();
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
    }

    private void rmSrc(int srcIndex){
        articlesModule.rmSrc(srcIndex);
    } 

    private void genFeed(){
        try {
            String feed = articlesModule.getFeed();
            Utils.printMsg("***** GENERATED FEED *****\n" + feed );
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            Utils.printError("Issue generating feed. Please try again later");
        }
    }

    private void sendall(){
        String feed;
        try {
            feed = articlesModule.getFeed();
            emailModule.sendEmailToAddressesInAddressBook("GEEK FEED", feed);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            Utils.printError("Unable to generate feed, please try again later");
        } 

    }
}
