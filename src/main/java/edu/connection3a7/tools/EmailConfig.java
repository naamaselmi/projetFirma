package edu.connection3a7.tools;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailConfig {

    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final String USERNAME = "molkaajengui@gmail.com"; // À CHANGER
    private static final String PASSWORD = "vqwvpgsaiypduclm"; // À CHANGER

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.trust", HOST);
        return props;
    }

    public static Session getSession() {
        return Session.getInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }
}