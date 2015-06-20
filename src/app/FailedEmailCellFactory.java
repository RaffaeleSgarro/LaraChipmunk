package app;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class FailedEmailCellFactory implements Callback<ListView<FailedSendMailAttempt>, ListCell<FailedSendMailAttempt>> {

    @Override
    public ListCell<FailedSendMailAttempt> call(ListView<FailedSendMailAttempt> param) {
        return new FailedSendMailAttemptListCell();
    }

    private static class FailedSendMailAttemptListCell extends ListCell<FailedSendMailAttempt> {
        @Override
        public void updateItem(FailedSendMailAttempt item, boolean isEmpty) {
            super.updateItem(item, isEmpty);

            if (isEmpty) {
                setText("");
            }

            if (item != null) {
                setText(item.getEmail().file.getName());
            }
        }
    }
}
