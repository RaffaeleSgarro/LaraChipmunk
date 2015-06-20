package app;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

public class Email {

    private static final Logger log = Logger.getLogger(Email.class.getName());

    public String user;
    public String host;
    public String password;
    public String port;
    public String to;
    public String subject;
    public String message;
    public File file;
    public String smtpAuth ;
    public String startTls;
    public String from;

    public void send() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, authenticator());

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();
        message.setContent(multipart);

        MimeBodyPart text = new MimeBodyPart();
        multipart.addBodyPart(text);
        text.setText(this.message, "UTF-8");

        MimeBodyPart attachment = new MimeBodyPart();
        multipart.addBodyPart(attachment);
        DataSource ds = new FileDataSource(file);
        attachment.setDataHandler(new DataHandler(ds));
        attachment.setDisposition(Part.ATTACHMENT);
        attachment.setFileName(file.getName());

        Transport.send(message);
    }

    private Authenticator authenticator() {
        return new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
    }
}
