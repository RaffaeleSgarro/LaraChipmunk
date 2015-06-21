package app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ContactsController extends VBox {

    private final TextField search = new TextField();
    private final ListView<Contact> hits = new ListView<>();
    private final ContactsService contactsService;

    private OnContactDoubleClicked onContactDoubleClicked;

    public ContactsController(final ContactsService contactsService) {
        this.contactsService = contactsService;

        setSpacing(10);
        getChildren().addAll(search, hits);
        setVgrow(hits, Priority.ALWAYS);

        search.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                search(newValue);
            }
        });

        hits.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2 && onContactDoubleClicked != null) {
                    onContactDoubleClicked.onContactDoubleClicked(hits.getSelectionModel().getSelectedItem());
                }
            }
        });
    }

    private void search(String q) {
        if (q == null || q.isEmpty()) {
            hits.getItems().clear();
        } else {
            try {
                hits.getItems().setAll(contactsService.search(q));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ObservableValue<Contact> selectedContactProperty() {
        return hits.getSelectionModel().selectedItemProperty();
    }

    public void setOnContactDoubleClicked(OnContactDoubleClicked onContactDoubleClicked) {
        this.onContactDoubleClicked = onContactDoubleClicked;
    }

    public interface OnContactDoubleClicked {
        void onContactDoubleClicked(Contact contact);
    }

}
