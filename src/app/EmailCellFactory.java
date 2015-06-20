package app;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EmailCellFactory implements Callback<ListView<Email>, ListCell<Email>> {

    @Override
    public ListCell<Email> call(ListView<Email> param) {
        return new EmailListCell();
    }

    private static class EmailListCell extends ListCell<Email> {
        @Override
        public void updateItem(Email item, boolean isEmpty) {
            super.updateItem(item, isEmpty);

            if (isEmpty) {
                setText("");
            }

            if (item != null) {
                setText(item.file.getName());
            }
        }
    }
}
