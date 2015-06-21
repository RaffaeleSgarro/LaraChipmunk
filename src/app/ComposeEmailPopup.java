package app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.logging.Logger;

public class ComposeEmailPopup extends Stage {

    private static final Logger log = Logger.getLogger(ComposeEmailPopup.class.getName());

    private final App app;
    private final File attachmentFile;
    private final ContactsController contactsController;

    private final TextField to = new TextField();
    private final TextField subject = new TextField();
    private final Button sendBtn = new Button("Invia");
    private final TextArea message = new TextArea();

    public ComposeEmailPopup(App app, File attachmentFile, ContactsService contactsService) {
        this.app = app;
        this.attachmentFile = attachmentFile;

        contactsController = new ContactsController(contactsService);

        setScene(new Scene(layout()));

        setUpEventsHandling();

        setWidth(850);
        setHeight(450);

        setTitle("Compose: " + attachmentFile.getName());
    }

    private void setUpEventsHandling() {

        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Email email = prepareEmailMessage();
                schedule(email);
            }
        });
    }

    private void schedule(Email email) {
        app.scheduleEmail(email);
        close();
    }

    public void onBeforeShow() {
        subject.setText(app.getLastUsedSubject());
        message.setText(app.getLastUsedMessageBody());
    }

    public void onBeforeShow(FailedSendMailAttempt failedSendMailAttempt) {
        to.setText(failedSendMailAttempt.getEmail().to);
        subject.setText(failedSendMailAttempt.getEmail().subject);
        message.setText(failedSendMailAttempt.getEmail().message);
    }

    private BorderPane layout() {
        BorderPane root = new BorderPane();

        // Contacts
        root.setLeft(contactsController);
        BorderPane.setMargin(contactsController, new Insets(10));
        contactsController.setPrefWidth(300);

        // Compose
        final VBox compose = new VBox();
        root.setCenter(compose);
        BorderPane.setMargin(compose, new Insets(10));
        compose.setSpacing(10);

        compose.getChildren().addAll(
                to
                , subject
                , message
        );

        // Send
        root.setBottom(sendBtn);
        BorderPane.setMargin(sendBtn, new Insets(10));
        BorderPane.setAlignment(sendBtn, Pos.BOTTOM_RIGHT);

        to.setPromptText("Destinatario");
        subject.setPromptText("Oggetto della mail");
        message.setPromptText("Corpo del testo");
        message.setPrefHeight(60);
        VBox.setVgrow(message, Priority.SOMETIMES);

        contactsController.selectedContactProperty().addListener(new ChangeListener<Contact>() {
            @Override
            public void changed(ObservableValue<? extends Contact> observable, Contact oldValue, Contact newValue) {
                if (newValue != null) {
                    to.textProperty().setValue(newValue.getEmail());
                }
            }
        });

        contactsController.setOnContactDoubleClicked(new ContactsController.OnContactDoubleClicked() {
            @Override
            public void onContactDoubleClicked(Contact contact) {
                Email email = prepareEmailMessage();
                email.to = contact.getEmail();
                schedule(email);
            }
        });

        return root;
    }

    private Email prepareEmailMessage() {
        Email email = app.prepareEmail();
        email.file = attachmentFile;
        email.to = to.getText();
        email.subject = subject.getText();
        email.message = message.getText();

        return email;
    }
}
