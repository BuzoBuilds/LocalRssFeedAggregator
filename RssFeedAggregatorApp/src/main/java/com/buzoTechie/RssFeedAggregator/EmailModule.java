package com.buzoTechie.RssFeedAggregator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage; 

public class EmailModule {
    private String emailAddress;
    private String username; 
    private String password; 
    private String smtpServer; 
    private int smptPort; 
    
    private ArrayList<String> addressBook;

    public EmailModule(String credsFilePath) throws IOException{
        Path file = Path.of(credsFilePath); 
        List<String> creds = Files.readAllLines(file);  
        String[] credsLine = creds.get(0).split(","); 
        this.emailAddress = credsLine[0]; 
        this.username = credsLine[1]; 
        this.password = credsLine[2]; 
        this.smtpServer = credsLine[3]; 
        this.smptPort = Integer.parseInt(credsLine[4]); 

        addressBook = new ArrayList<String>(); 
        addressBook.add("thebuzotechieburner1@gmail.com"); 
    } 

    public void test(){
        sendEmailFromAppEmail("thebuzotechieburner1@gmail.com", "Epiosde III Revenge of the Buzo!!!", "data\ndata1\ndata2");
    } 

    public void sendEmailToAddressesInAddressBook(String subject, String msg){
        for(int i = 0; i < this.addressBook.size(); i++){
            this.sendEmailFromAppEmail(addressBook.get(i), subject, msg);
        }
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
}
