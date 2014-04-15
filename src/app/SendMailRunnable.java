package app;

import javafx.application.Platform;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendMailRunnable implements Runnable {

    private static final Logger log = Logger.getLogger("mail");

    private final MailSpec spec;
    private final MessagingConsole console;

    public SendMailRunnable(MailSpec spec, MessagingConsole console) {
        this.spec = spec;
        this.console = console;
    }

    private void append(final String line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                console.appendLine(line);
            }
        });
    }

    @Override
    public void run() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", spec.smtpAuth);
            props.put("mail.smtp.starttls.enable", spec.startTls);
            props.put("mail.smtp.host", spec.host);
            props.put("mail.smtp.port", spec.port);

            Session session = Session.getInstance(props, authenticator(spec));


            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(spec.user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(spec.to));
            message.setSubject(spec.subject);

            Multipart multipart = new MimeMultipart();
            message.setContent(multipart);

            MimeBodyPart text = new MimeBodyPart();
            multipart.addBodyPart(text);
            text.setText(spec.message, "UTF-8");

            // This allow using the same code for sending the test message, which does
            // not has a file. Not sure this is the best approach
            if (spec.file != null) {
                MimeBodyPart attachment = new MimeBodyPart();
                multipart.addBodyPart(attachment);
                DataSource ds = new FileDataSource(spec.file);
                attachment.setDataHandler(new DataHandler(ds));
                attachment.setDisposition(Part.ATTACHMENT);
                attachment.setFileName(spec.file.getName());
            }

            append("Invio in corso");
            append("Attendere...");
            append("(TODO aggiungere log)");
            Transport.send(message);
            append("Mail spedita con successo!");
        } catch (MessagingException e) {
            append("Invio fallito");
            log.log(Level.SEVERE, "Failed to send email", e);
        }
    }

    private Authenticator authenticator(final MailSpec spec) {
        return new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(spec.user, spec.password);
            }
        };
    }
}
