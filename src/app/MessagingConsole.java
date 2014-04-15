package app;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MessagingConsole extends Stage {

    private ListView<String> messages;

    public MessagingConsole() {
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

    public void appendLine(String line) {
        messages.getItems().add(line);
    }

}
