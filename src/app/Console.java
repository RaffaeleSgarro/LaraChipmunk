package app;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Console extends Stage {

    private ListView<String> messages;

    public Console() {
        setTitle("Log");
        setWidth(600);
        setHeight(300);

        initModality(Modality.APPLICATION_MODAL);
        messages = new ListView<>();
        Scene scene = new Scene(messages);
        setScene(scene);
    }

    public void clear() {
        messages.getItems().clear();
    }

    public void append(final String line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messages.getItems().add(line);
            }
        });
    }

    public void safeClose() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

}
