package app;

import javafx.application.Platform;

public class MockSendMailRunnable implements Runnable {

    private final MailSpec src;
    private final MessagingConsole console;

    public MockSendMailRunnable(MailSpec src, MessagingConsole console) {
        this.src = src;
        this.console = console;
    }

    @Override
    public void run() {
        try {
            showMessage("Starting mock runnable. Sleeping 5 seconds...");
            Thread.sleep(5000);
            showMessage("Mock runnabled ended!");
        } catch (Exception e) {
            throw new RuntimeException("Exception while sleeping!", e);
        }
    }

    private void showMessage(final String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                console.appendLine(message);
            }
        });
    }
}
