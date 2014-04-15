package app;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class DialogWithLengthyText extends Stage {

    private final Label msg;
    private final ScrollPane scroller;
    private final Label scrollable = new Label();


    public DialogWithLengthyText() {
        VBox root = new VBox();
        Scene scene = new Scene(root);
        msg = new Label();

        scroller = new ScrollPane();
        scroller.setContent(scrollable);

        root.getChildren().setAll(msg, scroller);

        setScene(scene);
        setWidth(400);
        setHeight(400);
    }

    public void setShortText(String text) {
        msg.setText(text);
    }

    public void setLongText(String text) {
        scrollable.setText(text);
    }
}
