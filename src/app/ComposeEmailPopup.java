package app;

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

    private final ContactsController contactsController = new ContactsController();

    private final TextField to = new TextField();
    private final TextField subject = new TextField();
    private final Button sendBtn = new Button("Invia");
    private final TextArea message = new TextArea();

    public ComposeEmailPopup(App app, File attachmentFile) {
        this.app = app;
        this.attachmentFile = attachmentFile;

        setScene(new Scene(layout()));

        setUpEventsHandling();

        setWidth(800);
        setHeight(450);

        setTitle("Compose: " + attachmentFile.getName());
    }

    private void setUpEventsHandling() {

        sendBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                app.scheduleEmail(prepareEmailMessage());
                close();
            }
        });
    }

    public void onBeforeShow() {
        subject.setText(app.getLastUsedSubject());
        message.setText(app.getLastUsedMessageBody());
    }

    private BorderPane layout() {
        BorderPane root = new BorderPane();

        // Contacts
        root.setLeft(contactsController);
        BorderPane.setMargin(contactsController, new Insets(10));

        // Compose
        VBox compose = new VBox();
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
