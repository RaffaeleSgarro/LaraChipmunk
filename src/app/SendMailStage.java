package app;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendMailStage extends Stage {

    private static final Logger log = Logger.getLogger(SendMailStage.class.getName());

    private final App app;
    private final File attachmentFile;

    private final TextField host = new TextField();
    private final TextField port = new TextField();
    private final TextField user = new TextField();
    private final TextField password = new PasswordField();
    private final CheckBox auth = new CheckBox("Auth");
    private final CheckBox startTls = new CheckBox("Start TLS");
    private final Button testConnectionBtn = new Button("Test");
    private final Button saveSettingsBtn = new Button("Salva");

    private final TextField from = new TextField();
    private final TextField to = new TextField();
    private final TextField subject = new TextField();
    private final Button sendBtn = new Button("Invia");
    private final TextArea message = new TextArea();

    public SendMailStage(App app, File attachmentFile) {
        this.app = app;
        this.attachmentFile = attachmentFile;

        setScene(new Scene(layout()));

        setUpEventsHandling();

        setWidth(700);
        setHeight(450);

        setTitle("Compose: " + attachmentFile.getName());
    }

    private void setUpEventsHandling() {

        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sendMailSpec(getSnapshot());
            }
        });

        saveSettingsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                app.setConfigurationProperty("user", user.getText());
                app.setConfigurationProperty("from", from.getText());
                // Password is not stored
                app.setConfigurationProperty("mail.smtp.host", host.getText());
                app.setConfigurationProperty("mail.smtp.port", port.getText());
                app.setConfigurationProperty("mail.smtp.auth", Boolean.toString(auth.selectedProperty().get()));
                app.setConfigurationProperty("mail.smtp.starttls.enable", Boolean.toString(startTls.selectedProperty().get()));
                app.saveConfiguration();
            }
        });

        testConnectionBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                MailSpec spec = getSnapshot();
                spec.to = spec.from;
                spec.subject = "Test from Lara Chipmunk";
                spec.message = "It works!";
                sendMailSpec(spec);
            }
        });
    }

    public void onBeforeShow() {
        host.setText(app.getConfigurationProperty("mail.smtp.host"));
        port.setText(app.getConfigurationProperty("mail.smtp.port"));
        user.setText(app.getConfigurationProperty("user"));
        // password is not stored for security reasons
        auth.selectedProperty().setValue(Boolean.parseBoolean(app.getConfigurationProperty("mail.smtp.auth")));
        startTls.selectedProperty().setValue(Boolean.parseBoolean(app.getConfigurationProperty("mail.smtp.starttls.enable")));
        from.setText(app.getConfigurationProperty("from"));
    }

    private VBox layout() {
        VBox root = new VBox();
        root.setPadding(new Insets(10));

        root.setSpacing(10);

        HBox settings = new HBox();
        settings.setSpacing(10);
        settings.setMaxWidth(Double.MAX_VALUE);
        settings.getChildren().setAll(to, sendBtn);
        HBox.setHgrow(to, Priority.ALWAYS);

        host.setPromptText("Server");
        port.setPromptText("Porta");
        user.setPromptText("Utente");
        password.setPromptText("Password");
        port.setPrefWidth(50);

        from.setPromptText("Da");
        to.setPromptText("Destinatario");
        subject.setPromptText("Oggetto della mail");
        message.setPromptText("Corpo del testo");

        root.getChildren().setAll(
                  from
                , HBoxBuilder.create().children(host, port, user, password, auth, startTls, testConnectionBtn, saveSettingsBtn)
                        .prefWidth(Double.MAX_VALUE)
                        .spacing(10)
                        .build()
                , subject
                , message
                , settings
        );

        HBox.setHgrow(host, Priority.ALWAYS);
        host.setPrefWidth(100);
        password.setPrefWidth(100);

        message.setPrefHeight(60);
        VBox.setVgrow(message, Priority.SOMETIMES);

        return root;
    }

    private void sendMailSpec(MailSpec spec) {
        try {
            validate(spec);
            MessagingConsole messagingConsole = new MessagingConsole();
            messagingConsole.clear();
            messagingConsole.show();
            send(spec, messagingConsole);
        } catch (ValidationException e) {
            showInvalidDataDialog(e);
        } catch (MessagingException e) {
            log.severe(e.getMessage());
            log.log(Level.SEVERE, "Could not send message", e);
            showErrorDialog(e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Errore");
        dialog.setText(message);
        dialog.show();
    }

    private void showInvalidDataDialog(ValidationException e) {
        DialogWithLengthyText dialog = new DialogWithLengthyText();
        dialog.setTitle("Errore");
        dialog.setShortText(e.getMessage());
        StringBuilder longText = new StringBuilder();
        for (String msg : e.errors) longText.append(msg).append("\n");
        dialog.setLongText(longText.toString());
        dialog.show();
    }

    private void validate(MailSpec spec) throws ValidationException {
        // TODO implement Just let it fail for now
    }

    private MailSpec getSnapshot() {
        MailSpec s = new MailSpec();
        s.file = attachmentFile;
        s.host = host.getText();
        s.port = port.getText();
        s.user = user.getText();
        s.password = password.getText();
        s.from = from.getText();
        s.to = to.getText();
        s.subject = subject.getText();
        s.message = message.getText();
        s.startTls = Boolean.toString(startTls.selectedProperty().get());
        s.smtpAuth = Boolean.toString(auth.selectedProperty().get());
        return s;
    }

    private void send(MailSpec src, MessagingConsole console) throws MessagingException {
        Runnable r;

        if (app.isMockMailService()) {
            r = new MockSendMailRunnable(src, console);
        } else {
            r = new SendMailRunnable(src, console);
        }

        Thread t = new Thread(r);
        t.setName("sendmail");
        t.setDaemon(true);
        t.start();
    }
}
