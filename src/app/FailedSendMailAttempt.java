package app;

public class FailedSendMailAttempt {

    private final String error;
    private final Email email;

    public FailedSendMailAttempt(String error, Email email) {
        this.error = error;
        this.email = email;
    }

    public String getError() {
        return error;
    }

    public Email getEmail() {
        return email;
    }
}
