package app;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Dialog extends Stage {

    private final Label msg;

    public Dialog() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        msg = new Label();
        root.setCenter(msg);
        setScene(scene);
        setWidth(400);
        setHeight(300);
    }

    public void setText(String text) {
        msg.setText(text);
    }
}
