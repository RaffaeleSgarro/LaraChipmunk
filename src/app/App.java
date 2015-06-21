package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".lara-chipmunk";
    public static final String CONF_FILE_NAME = "conf.properties";

    private final Properties conf = new Properties();

    private final Button setAttachmentsDirBtn = new Button("Scegli la directory che contiene gli attestati");
    private final ListView<File> files = new ListView<>();
    private final ListView<Email> scheduledEmails = new ListView<>();
    private final ListView<FailedSendMailAttempt> failedEmails = new ListView<>();
    private final ListView<Email> sentEmails = new ListView<>();

    private final ContactsService contactsService = new ContactsService();

    private ExecutorService executor;
    private Stage mainWindow;
    private boolean mockMailService;
    private String SMTPPassword;
    private String lastUsedSubject;
    private String lastUsedMessageBody;

    @Override
    public void start(Stage stage) throws Exception {
        ensureConfFileExists();
        InputStream in = new FileInputStream(new File(HOME_DIR, CONF_FILE_NAME));
        conf.load(in);
        in.close();

        mockMailService = getParameters().getUnnamed().contains("--mock-mail");

        executor = Executors.newSingleThreadExecutor();

        mainWindow = stage;
        BorderPane root = layout();
        setUpEventsHandling();
        showContentsOfLastWorkingDirectory();

        Scene scene = new Scene(root);
        stage.setTitle("LaraChipmunk");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                executor.shutdownNow();
            }
        });
    }

    private void showContentsOfLastWorkingDirectory() {
        String lastWorkingDirectory = conf.getProperty("lastWorkingDirectory");
        if (lastWorkingDirectory != null) {
            try {
                File dir = new File(lastWorkingDirectory);
                if (dir.exists() && dir.isDirectory()) {
                    showDirContents(dir);
                }
            } catch (Exception e) {
                log.warning("Could not show content of last working directory: " + e.getMessage());
            }
        }
    }

    private void setUpEventsHandling() {

        setAttachmentsDirBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Scegli la directory con i documenti da inviare");
                File dir = chooser.showDialog(mainWindow);
                if (dir != null && dir.exists()) {
                    showDirContents(dir);
                    setConfigurationProperty("lastWorkingDirectory", dir.getAbsolutePath());
                    saveConfiguration();
                }
            }
        });

        files.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    File attachmentFile = files.selectionModelProperty().get().getSelectedItem();
                    ComposeEmailPopup stage = new ComposeEmailPopup(App.this, attachmentFile, contactsService);
                    stage.onBeforeShow();
                    stage.show();
                }
            }
        });
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

    private void showDirContents(final File dir) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final File[] fileList = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.canRead();
                    }
                });

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        files.setItems(FXCollections.observableArrayList(fileList));
                    }
                });
            }
        });

        thread.setDaemon(true);
        thread.start();

    }

    public static void main(String... args) {
        launch(args);
    }

    private BorderPane layout() {

        BorderPane root = new BorderPane();

        MenuBar menuBar = menuBar();
        root.setTop(menuBar);

        VBox filesLayout = new VBox();
        root.setCenter(filesLayout);

        Accordion operations = new Accordion();
        root.setLeft(operations);

        filesLayout.setPadding(new Insets(10));
        filesLayout.setSpacing(10);
        filesLayout.getChildren().setAll(
                  setAttachmentsDirBtn
                , files
        );

        files.setPrefHeight(100);
        setAttachmentsDirBtn.setPrefWidth(Double.MAX_VALUE);
        VBox.setVgrow(files, Priority.SOMETIMES);

        EmailCellFactory emailCellFactory = new EmailCellFactory();

        TitledPane scheduled = new TitledPane();
        scheduled.textProperty().bind(Bindings.concat("In attesa", " ", "(", Bindings.size(scheduledEmails.getItems()), ")"));
        scheduled.setContent(scheduledEmails);
        scheduledEmails.setCellFactory(emailCellFactory);

        TitledPane failed = new TitledPane();
        failed.textProperty().bind(Bindings.concat("Errori", " ", "(", Bindings.size(failedEmails.getItems()), ")"));
        failed.setContent(failedEmails);
        failedEmails.setCellFactory(new FailedEmailCellFactory());

        TitledPane sent = new TitledPane();
        sent.textProperty().bind(Bindings.concat("Inviati", " ", "(", Bindings.size(sentEmails.getItems()), ")"));
        sent.setContent(sentEmails);
        sentEmails.setCellFactory(emailCellFactory);

        operations.getPanes().addAll(scheduled, failed, sent);

        operations.setExpandedPane(scheduled);

        return root;
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();

        Menu settingsMenu = new Menu("Impostazioni");

        MenuItem smtpMenu = new MenuItem("Configura invio email");
        smtpMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SMTPSettingsPopup smtpSettingsPopup = new SMTPSettingsPopup(App.this);
                smtpSettingsPopup.onBeforeShow();
                smtpSettingsPopup.show();
            }
        });
        settingsMenu.getItems().add(smtpMenu);

        Menu contacts = new Menu("Contatti");

        MenuItem loadContacts = new MenuItem("Carica da file CSV (name, email)");
        loadContacts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onLoadContactsClicked();
            }
        });
        contacts.getItems().addAll(loadContacts);
        menuBar.getMenus().addAll(settingsMenu, contacts);

        return menuBar;
    }

    private void onLoadContactsClicked() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        final File csv = fileChooser.showOpenDialog(mainWindow);

        if (csv == null)
            return;

        final Console console = new Console();
        console.show();
        console.append("Carico i contatti...");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    contactsService.createIndexFromCsv(new FileReader(csv));
                    console.safeClose();
                } catch (Exception e) {
                    console.append("ERROR: " + e.getMessage());
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public boolean isMockMailService() {
        return mockMailService;
    }

    public String getConfigurationProperty(String key) {
        return conf.getProperty(key);
    }

    public void setConfigurationProperty(String key, String value) {
        conf.setProperty(key, value);
    }

    public void saveConfiguration() {
        try {
            storeProperties(conf);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not save config file", e);
        }
    }

    public void scheduleEmail(final Email email) {
        lastUsedSubject = email.subject;
        lastUsedMessageBody = email.message;

        scheduledEmails.getItems().add(email);

        executor.submit(new SendEmailJob(email));
    }

    public String getLastUsedSubject() {
        return lastUsedSubject;
    }

    public String getLastUsedMessageBody() {
        return lastUsedMessageBody;
    }

    public Email prepareEmail() {
        SMTPSettings settings = new SMTPSettings(this);
        settings.refresh();
        return new Email(settings);
    }

    public void setSMTPPassword(String SMTPPassword) {
        this.SMTPPassword = SMTPPassword;
    }

    public String getSMTPPassword() {
        return SMTPPassword;
    }

    private class SendEmailJob implements Runnable {

        private final Email email;

        public SendEmailJob(Email email) {
            this.email = email;
        }

        @Override
        public void run() {
            try {

                if (isMockMailService()) {
                    Thread.sleep(5000);
                } else {
                    email.send();
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sentEmails.getItems().add(email);
                    }
                });
            } catch (final Exception e) {
                log.severe(e.getMessage());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        failedEmails.getItems().add(new FailedSendMailAttempt(e.getMessage(), email));
                    }
                });
            } finally {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        scheduledEmails.getItems().remove(email);
                    }
                });
            }
        }
    }
}
