package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".lara-chipmunk";
    public static final String CONF_FILE_NAME = "conf.properties";

    private final Properties conf = new Properties();

    private final Button setAttachmentsDirBtn = new Button("Scegli la directory che contiene gli allegati");
    private final ListView<File> files = new ListView<>();

    private Stage mainWindow;
    private boolean mockMailService;

    @Override
    public void start(Stage stage) throws Exception {
        ensureConfFileExists();
        InputStream in = new FileInputStream(new File(HOME_DIR, CONF_FILE_NAME));
        conf.load(in);
        in.close();

        mockMailService = getParameters().getUnnamed().contains("--mock-mail");

        mainWindow = stage;
        VBox root = layout();
        setUpEventsHandling();

        Scene scene = new Scene(root);
        stage.setTitle("Invia i file per email");
        stage.setScene(scene);
        stage.setWidth(750);
        stage.setHeight(500);
        stage.show();
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

        files.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    File attachmentFile = files.selectionModelProperty().get().getSelectedItem();
                    SendMailStage stage = new SendMailStage(App.this, attachmentFile);
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

    public void storeProperties(Properties p) throws IOException {
        // Use default platform encoding
        FileWriter out = new FileWriter(new File(HOME_DIR, CONF_FILE_NAME));
        p.store(out, "Edit this file to change app settings");
        out.close();
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

        root.getChildren().setAll(
                setAttachmentsDirBtn
                , files
        );

        files.setPrefHeight(100);
        VBox.setVgrow(files, Priority.SOMETIMES);

        return root;
    }

    public boolean isMockMailService() {
        return mockMailService;
    }

    public Properties getConfiguration() {
        return conf;
    }
}
