package com.buzoTechie.RssFeedAggregator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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
        emailModule = new EmailModule("/home/amby/ProjCreds/rssEmailCreds", "/home/amby/reposTheBuzoTechie/rssFeedAggregator/res/addressBook");
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
        helperSb.append("lsaddy: lists all email address entries in the address book\n");
        helperSb.append("addaddy: name(string) email(string): adds an email entry into the address book\n");
        helperSb.append("rmaddy: name(string): remove all email entries associated with name from address book\n");
        helperSb.append("setjob: hour(int) minute(int): set time for feed to be automatically generated and sent to members in address book\n");
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
                else if(splitedInputLine[0].equalsIgnoreCase("lsaddy")){
                    this.listAddressBook();
                }
                else if(splitedInputLine[0].equalsIgnoreCase("addaddy")){
                    if(splitedInputLine.length < 3){
                        Utils.printError("Invalid args for addaddy command");
                        System.out.println(helperMsg); 
                        continue;
                    }
                    this.addAddressToAddressBook(splitedInputLine[1], splitedInputLine[2]);
                }
                else if(splitedInputLine[0].equalsIgnoreCase("rmaddy")){
                    if(splitedInputLine.length < 2){
                        Utils.printError("Invalid args for rmaddy command");
                        System.out.println(helperMsg); 
                        continue;
                    }
                    this.removeAddressFromAddressBook(splitedInputLine[1]);
                }
                else if(splitedInputLine[0].equalsIgnoreCase("setjob")){ 
                    int hour; 
                    int minute;
                    if((splitedInputLine.length < 3)){
                        Utils.printError("Invalid args for setjob command");
                        System.out.println(helperMsg); 
                        continue;
                    }
                    try{
                        hour = Integer.parseInt(splitedInputLine[1]);
                        minute = Integer.parseInt(splitedInputLine[2]);
                        if(hour < 0 || hour > 23 || minute < 0 || minute > 59){
                            throw new NumberFormatException();
                        }
                    }
                    catch(NumberFormatException e){
                        e.printStackTrace(); 
                        Utils.printError("setjob arguments: hour = [0,23]; minute = [0,59]");
                        continue;
                    }
                    try {
                        this.setDailyJob(hour, minute);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Utils.printError("Unable to set cronjob");
                        continue;
                    }
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

    private void listAddressBook(){
        Utils.printMsg(this.emailModule.listAddressBook());
    }

    private void addAddressToAddressBook(String name, String emailAddress){
        this.emailModule.addAddressToAddressBook(name, emailAddress);
    }

    private void removeAddressFromAddressBook(String name){
        this.emailModule.removeAddressFromAddressBook(name);
    }

    private void setDailyJob(int hour, int minute) throws IOException{
        //get current crontab jobs
        File temp = new File("temp");  
        ProcessBuilder pb = new ProcessBuilder("crontab -l"); 
        pb.redirectOutput(temp);  
        pb.start();
        List<String> currCronJobs = Files.readAllLines(temp.toPath());  


        //remove any potential rssAggreator jobs
        List<String> newCronJobs = new ArrayList<String>(); 
        for(int i = 0; i < currCronJobs.size(); i++){
            if(!currCronJobs.get(i).contains("rssFeedAggregator/genAndSendFeed.sh")){
                newCronJobs.add(currCronJobs.get(i));
            }
        } 

        //add new rssAggreagtor job 
        StringBuilder sb = new StringBuilder(); 
        sb.append(hour); 
        sb.append(" "); 
        sb.append(minute); 
        sb.append(" * * * ~/reposTheBuzoTechie/rssFeedAggregator/genAndSendFeed.sh");
        newCronJobs.add(sb.toString());  

        StringBuilder allCronJobs = new StringBuilder();
        for(int i = 0; i < newCronJobs.size(); i++){
            allCronJobs.append(newCronJobs.get(i)); 
            if(i != newCronJobs.size() -1){
                allCronJobs.append("\n");
            }
        } 

        Files.write(temp.toPath(), allCronJobs.toString().getBytes(), StandardOpenOption.WRITE);

        //redirect temp file to set new cron jobs 
        ProcessBuilder pb2 = new ProcessBuilder("temp > crontab"); 
        pb2.start();
    }
}
