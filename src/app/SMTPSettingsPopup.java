package app;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.mail.MessagingException;

public class SMTPSettingsPopup extends Stage {

    private final App app;

    private final TextField host = new TextField();
    private final TextField port = new TextField();
    private final TextField user = new TextField();
    private final TextField password = new PasswordField();
    private final CheckBox auth = new CheckBox("Auth");
    private final CheckBox startTls = new CheckBox("Start TLS");
    private final Button testOutgoingMailBtn = new Button("Test");
    private final Button saveSettingsBtn = new Button("Salva");
    private final TextField from = new TextField();

    public SMTPSettingsPopup(App app) {
        this.app = app;

        VBox root = layout();
        setScene(new Scene(root));
        setUpHandlers();
        setWidth(400);
        setHeight(400);
        setTitle("Configurazione SMTP per l'invio email");
    }

    public VBox layout() {

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.BASELINE_RIGHT);
        buttons.getChildren().addAll(testOutgoingMailBtn, saveSettingsBtn);

        Region gap = new Region();

        root.getChildren().addAll(from, user, host, port, password, auth, startTls, gap, buttons);

        VBox.setVgrow(gap, Priority.ALWAYS);

        host.setPromptText("Server");
        port.setPromptText("Porta");
        user.setPromptText("Utente");
        password.setPromptText("Password");
        port.setPrefWidth(50);
        from.setPromptText("Da");

        return root;
    }

    public void setUpHandlers() {
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

                close();
            }
        });

        testOutgoingMailBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                sendTestEmail();
            }
        });
    }

    public void onBeforeShow() {
        SMTPSettings settings = new SMTPSettings();
        settings.loadFromConfiguration(app);

        host.setText(settings.host);
        port.setText(settings.port);
        user.setText(settings.user);
        auth.selectedProperty().setValue(Boolean.parseBoolean(settings.smtpAuth));
        startTls.selectedProperty().setValue(Boolean.parseBoolean(settings.startTls));
        from.setText(settings.from);

        password.setText(settings.password);
    }

    private void sendTestEmail() {

        final Console console = new Console();
        console.clear();
        console.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Email email = new Email();

                SMTPSettings settings = new SMTPSettings();
                settings.host = host.getText();
                settings.port = port.getText();
                settings.user = user.getText();
                settings.password = password.getText();
                settings.from = from.getText();
                settings.startTls = Boolean.toString(startTls.selectedProperty().get());
                settings.smtpAuth = Boolean.toString(auth.selectedProperty().get());

                email.settings = settings;
                email.to = settings.from;
                email.subject = "Test from Lara Chipmunk";
                email.message = "It works!";

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

}
