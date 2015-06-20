package app;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ContactsController extends VBox {

    private final TextField search = new TextField();
    private final ListView<Contact> hits = new ListView<>();

    public ContactsController() {
        setSpacing(10);
        getChildren().addAll(search, hits);
        setVgrow(hits, Priority.ALWAYS);
    }

}
