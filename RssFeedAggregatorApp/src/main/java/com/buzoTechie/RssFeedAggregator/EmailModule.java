package com.buzoTechie.RssFeedAggregator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage; 

public class EmailModule {
    final private String startOfAddressBookMarker = "***** START OF ADDRESS BOOK *****";
    final private String endOfAddressBookMarker = "***** END OF ADDRESS BOOK *****";

    private String emailAddress;
    private String username; 
    private String password; 
    private String smtpServer; 
    private int smptPort; 
   
    private Path addressBookPath; 


    public EmailModule(String credsFilePath, String addressBookFilePath) throws IOException{
        Path file = Path.of(credsFilePath); 
        List<String> creds = Files.readAllLines(file);  
        String[] credsLine = creds.get(0).split(","); 
        this.emailAddress = credsLine[0]; 
        this.username = credsLine[1]; 
        this.password = credsLine[2]; 
        this.smtpServer = credsLine[3]; 
        this.smptPort = Integer.parseInt(credsLine[4]); 

        this.addressBookPath = Path.of(addressBookFilePath); 
    } 

    public String listAddressBook(){
        StringBuilder sb = new StringBuilder();
        ArrayList<EmailAddress> addressBook;
        try{
            addressBook = this.loadAddressBook(); 
        }
        catch(IOException e){
            e.printStackTrace(); 
            Utils.printError("Unable to list addressbook");
            return null;
        }

        sb.append("\n" + this.startOfAddressBookMarker + "\n");
        for(int i = 0; i < addressBook.size(); i++){
            sb.append("\t" + addressBook.get(i)); 
            sb.append("\n");
        } 
        sb.append(this.endOfAddressBookMarker);

        return sb.toString();

    }

    public void addAddressToAddressBook(String name, String emailAddress){
        EmailAddress address = new EmailAddress(name, emailAddress); 
        ArrayList<EmailAddress> addressBook;

        try{
            addressBook = this.loadAddressBook(); 
        }
        catch(IOException e){
            e.printStackTrace(); 
            Utils.printError("Unable to add (" + name + ", " + emailAddress + ") entry to address book");
            return;
        }

        addressBook.add(address); 

        try{
            this.writeAddressBookToAddressBookFile(addressBook);
        }
        catch(IOException e){
            e.printStackTrace(); 
            Utils.printError("Unable to add (" + name + ", " + emailAddress + ") entry to address book");
            return;
        }
    }

    public void test(){
        sendEmailFromAppEmail("thebuzotechieburner1@gmail.com", "Epiosde III Revenge of the Buzo!!!", "data\ndata1\ndata2");
    } 

    public void sendEmailToAddressesInAddressBook(String subject, String msg) throws IOException{
        ArrayList<EmailAddress> addressBook = this.loadAddressBook(); 
        for(int i = 0; i < addressBook.size(); i++){
            this.sendEmailFromAppEmail(addressBook.get(i).emailAddress, subject, msg);
        }
    }

    public void removeAddressFromAddressBook(String name){
        ArrayList<EmailAddress> addressBook;  
        try{
            addressBook = this.loadAddressBook(); 
            ArrayList<EmailAddress> newAddressBook = new ArrayList<EmailAddress>();
            for(int i = 0; i < addressBook.size(); i++){
                if(!addressBook.get(i).name.equals(name)){
                    newAddressBook.add(addressBook.get(i));
                }
            } 
            this.writeAddressBookToAddressBookFile(newAddressBook);
        }
        catch(IOException e){
            System.err.println(e);
            Utils.printError("addressbook file can't be modified"); 
            return;
        }

        Utils.printMsg("Successfully strucketh thy foul being " + name + " from address book");
    }

    private void sendEmailFromAppEmail(String reciver, String subject, String msg){
        Properties prop = new Properties(); 
        prop.put("mail.smtp.host", this.smtpServer); 
        prop.put("mail.smtp.port", this.smptPort); 
        prop.put("mail.smtp.auth", "true"); 
        prop.put("mail.smtp.socketFactory.port", this.smptPort); 
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 

        Session session = Session.getInstance(prop,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.emailAddress));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(reciver)
            );
            message.setSubject(subject);
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    } 

    private ArrayList<EmailAddress> loadAddressBook() throws IOException{
        ArrayList<EmailAddress> emailAddressList = new ArrayList<EmailAddress>();

        try(BufferedReader reader = Files.newBufferedReader(this.addressBookPath)){ 
            String line = reader.readLine(); 
            while(line != null){
                String[] splitedLine = line.split(",");
                emailAddressList.add(new EmailAddress(splitedLine[0].trim(), splitedLine[1].trim()));
                line = reader.readLine();
            }  
        } catch (IOException e) {
            throw new IOException("Unable to open addressbook file", e);
        }

        return emailAddressList;
    } 


    private void writeAddressBookToAddressBookFile(ArrayList<EmailAddress> addressBook) throws IOException{ 
        try(BufferedWriter writer = Files.newBufferedWriter(this.addressBookPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            for(int i = 0; i < addressBook.size(); i++){
                String line = addressBook.get(i).name + "," + addressBook.get(i).emailAddress; 
                if(i != addressBook.size() -1){
                    line += "\n";
                } 
                writer.write(line, 0, line.length());
            }
        }
    }



    class EmailAddress{
        public String name; 
        public String emailAddress;
        public EmailAddress(String name, String email){
            this.name = name; 
            this.emailAddress = email;
        } 

        public String toString(){
            StringBuilder sb = new StringBuilder(); 
            sb.append("Name: "); 
            sb.append(this.name); 
            sb.append(", email: "); 
            sb.append(this.emailAddress); 
            return sb.toString();
        }
    }
}
