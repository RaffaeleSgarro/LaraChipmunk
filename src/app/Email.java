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

    public SMTPSettings settings;
    public String to;
    public String subject;
    public String message;
    public File file;

    public void send() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", settings.smtpAuth);
        props.put("mail.smtp.starttls.enable", settings.startTls);
        props.put("mail.smtp.host", settings.host);
        props.put("mail.smtp.port", settings.port);

        Session session = Session.getInstance(props, authenticator());

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(settings.from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();
        message.setContent(multipart);

        MimeBodyPart text = new MimeBodyPart();
        multipart.addBodyPart(text);
        text.setText(this.message, "UTF-8");

        if (file != null) {
            MimeBodyPart attachment = new MimeBodyPart();
            multipart.addBodyPart(attachment);
            DataSource ds = new FileDataSource(file);
            attachment.setDataHandler(new DataHandler(ds));
            attachment.setDisposition(Part.ATTACHMENT);
            attachment.setFileName(file.getName());
        }

        Transport.send(message);
    }

    private Authenticator authenticator() {
        return new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.user, settings.password);
            }
        };
    }
}
