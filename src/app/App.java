package app;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger log = Logger.getLogger("app.App");

    public static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".lara-chipmunk";
    public static final String CONF_FILE_NAME = "conf.properties";

    private final Properties conf = new Properties();

    private final TextField host = new TextField();
    private final TextField port = new TextField();
    private final TextField user = new TextField();
    private final TextField password = new PasswordField();
    private final CheckBox auth = new CheckBox("Auth");
    private final CheckBox startTls = new CheckBox("Start TLS");
    private final Button testConnectionBtn = new Button("Test");
    private final Button saveSettingsBtn = new Button("Salva");

    private final Button setAttachmentsDirBtn = new Button("Scegli la directory che contiene gli allegati");
    private final ListView<File> files = new ListView<>();
    private final Label selectedFile = new Label("Nessun file selezionato");

    private final TextField to = new TextField();
    private final TextField subject = new TextField();
    private final Button sendBtn = new Button("Invia");
    private final TextArea message = new TextArea();

    private Stage mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        ensureConfFileExists();
        InputStream in = new FileInputStream(new File(HOME_DIR, CONF_FILE_NAME));
        conf.load(in);

        mainWindow = stage;
        VBox root = layout();
        setValuesFrom(conf);
        setUpEventsHandling();

        Scene scene = new Scene(root);
        stage.setTitle("Invia i file per email");
        stage.setScene(scene);
        stage.setWidth(700);
        stage.setHeight(600);
        stage.show();
    }

    private void setValuesFrom(Properties conf) {
        host.setText(conf.getProperty("mail.smtp.host"));
        port.setText(conf.getProperty("mail.smtp.port"));
        user.setText(conf.getProperty("user"));
        // password is not stored for security reasons
        auth.selectedProperty().setValue(Boolean.parseBoolean(conf.getProperty("mail.smtp.auth")));
        startTls.selectedProperty().setValue(Boolean.parseBoolean(conf.getProperty("mail.smtp.starttls.enable")));
    }

    private void ensureConfFileExists() throws IOException {
        File base = new File(HOME_DIR);

        if (!base.exists()) {
            if (!base.mkdirs()) {
                throw new IOException("Could not create " + HOME_DIR);
            }
        }

        File conf = new File(base, CONF_FILE_NAME);

        if (!conf.exists()) {
            InputStream in = getClass().getResourceAsStream("/default.properties");
            Properties p = new Properties();
            p.load(in);
            in.close();
            storeProperties(p);
        }
    }

    private void storeProperties(Properties p) throws IOException {
        // Use default platform encoding
        FileWriter out = new FileWriter(new File(HOME_DIR, CONF_FILE_NAME));
        p.store(out, "Edit this file to change app settings");
        out.close();
    }

    private void setUpEventsHandling() {

        setAttachmentsDirBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Scegli la directory");
                File dir = chooser.showDialog(mainWindow);
                if (dir != null && dir.exists()) {
                    showDirContents(dir);
                }
            }
        });

        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    MailSpec spec = getSnapshot();
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
        });

        files.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File old, File current) {
                selectedFile.setText(current != null ? current.getAbsolutePath() : "Nessun file selezionato");
            }
        });

        saveSettingsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                conf.setProperty("user", user.getText());
                // Password is not stored
                conf.setProperty("mail.smtp.host", host.getText());
                conf.setProperty("mail.smtp.port", port.getText());
                conf.setProperty("mail.smtp.auth", Boolean.toString(auth.selectedProperty().get()));
                conf.setProperty("mail.smtp.starttls.enable", Boolean.toString(startTls.selectedProperty().get()));
                try{
                    storeProperties(conf);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Could not save config file", e);
                }
            }
        });
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

    private void chk(String str, String desc, List<String> errors) {
        String msg = desc + " non può essere vuoto";
        if (str == null || str.isEmpty()) {
            errors.add(msg);
        }
    }

    private MailSpec getSnapshot() {
        MailSpec s = new MailSpec();
        s.host = host.getText();
        s.port = port.getText();
        s.user = user.getText();
        s.password = password.getText();
        s.to = to.getText();
        s.subject = subject.getText();
        s.message = message.getText();
        s.file = files.getSelectionModel().getSelectedItem();
        // TODO add checkboxes
        s.startTls = "true";
        s.smtpAuth = "true";
        return s;
    }

    private void send(MailSpec src, MessagingConsole console) throws MessagingException {
        SendMailRunnable r = new SendMailRunnable(src, console);
        Thread t = new Thread(r);
        t.start();
    }

    private void showDirContents(File dir) {
        files.setItems(FXCollections.observableArrayList(dir.listFiles()));
    }

    public static void main(String... args) {
        launch(args);
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

        to.setPromptText("Destinatario");
        subject.setPromptText("Oggetto della mail");
        message.setPromptText("Corpo del testo");

        root.getChildren().setAll(
                HBoxBuilder.create().children(host, port, user, password, auth, startTls, testConnectionBtn, saveSettingsBtn)
                        .prefWidth(Double.MAX_VALUE)
                        .spacing(10)
                        .build()
                , setAttachmentsDirBtn
                , files
                , selectedFile
                , subject
                , message
                , settings
        );

        HBox.setHgrow(host, Priority.ALWAYS);
        host.setPrefWidth(100);
        password.setPrefWidth(100);

        files.setPrefHeight(100);
        message.setPrefHeight(60);
        VBox.setVgrow(files, Priority.SOMETIMES);
        VBox.setVgrow(message, Priority.SOMETIMES);

        return root;
    }
}
