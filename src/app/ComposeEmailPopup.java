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
import java.util.logging.Logger;

public class ComposeEmailPopup extends Stage {

    private static final Logger log = Logger.getLogger(ComposeEmailPopup.class.getName());

    private final App app;
    private final File attachmentFile;

    private final TextField host = new TextField();
    private final TextField port = new TextField();
    private final TextField user = new TextField();
    private final TextField password = new PasswordField();
    private final CheckBox auth = new CheckBox("Auth");
    private final CheckBox startTls = new CheckBox("Start TLS");
    private final Button testOutgoingMailBtn = new Button("Test");
    private final Button saveSettingsBtn = new Button("Salva");

    private final TextField from = new TextField();
    private final TextField to = new TextField();
    private final TextField subject = new TextField();
    private final Button sendBtn = new Button("Invia");
    private final TextArea message = new TextArea();

    public ComposeEmailPopup(App app, File attachmentFile) {
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
                app.scheduleEmail(prepareEmailMessage());
                close();
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

        testOutgoingMailBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sendTestEmail();
            }
        });
    }

    private void sendTestEmail() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Email email = prepareEmailMessage();
                email.to = email.from;
                email.subject = "Test from Lara Chipmunk";
                email.message = "It works!";
                Console console = new Console();
                try {
                    console.append("Sending message to test SMTP...");
                    email.send();
                    console.append("It works!");
                } catch (MessagingException e) {
                    console.append("ERROR: " + e.getMessage());
                }
            }
        });

        t.setDaemon(true);
        t.setName("test-smtp");
        t.start();
    }

    public void onBeforeShow() {
        host.setText(app.getConfigurationProperty("mail.smtp.host"));
        port.setText(app.getConfigurationProperty("mail.smtp.port"));
        user.setText(app.getConfigurationProperty("user"));
        auth.selectedProperty().setValue(Boolean.parseBoolean(app.getConfigurationProperty("mail.smtp.auth")));
        startTls.selectedProperty().setValue(Boolean.parseBoolean(app.getConfigurationProperty("mail.smtp.starttls.enable")));
        from.setText(app.getConfigurationProperty("from"));

        password.setText(app.getLastUsedPassword());

        subject.setText(app.getLastUsedSubject());
        message.setText(app.getLastUsedMessageBody());
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
                , HBoxBuilder.create().children(host, port, user, password, auth, startTls, testOutgoingMailBtn, saveSettingsBtn)
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

    private Email prepareEmailMessage() {
        Email email = new Email();
        email.file = attachmentFile;
        email.host = host.getText();
        email.port = port.getText();
        email.user = user.getText();
        email.password = password.getText();
        email.from = from.getText();
        email.to = to.getText();
        email.subject = subject.getText();
        email.message = message.getText();
        email.startTls = Boolean.toString(startTls.selectedProperty().get());
        email.smtpAuth = Boolean.toString(auth.selectedProperty().get());
        return email;
    }
}
