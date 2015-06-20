package app;

public class SMTPSettings {

    private final App app;

    public String user;
    public String host;
    public String password;
    public String port;
    public String smtpAuth;
    public String startTls;
    public String from;

    public SMTPSettings(App app) {
        this.app = app;
    }

    public void refresh() {
        host = app.getConfigurationProperty("mail.smtp.host");
        port = app.getConfigurationProperty("mail.smtp.port");
        user = app.getConfigurationProperty("user");
        smtpAuth = app.getConfigurationProperty("mail.smtp.auth");
        startTls = app.getConfigurationProperty("mail.smtp.starttls.enable");
        from = app.getConfigurationProperty("from");

        password = app.getSMTPPassword();
    }

    public void save() {
        app.setConfigurationProperty("user", user);
        app.setConfigurationProperty("from", from);
        app.setConfigurationProperty("mail.smtp.host", host);
        app.setConfigurationProperty("mail.smtp.port", port);
        app.setConfigurationProperty("mail.smtp.auth", smtpAuth);
        app.setConfigurationProperty("mail.smtp.starttls.enable", startTls);
        app.saveConfiguration();

        app.setSMTPPassword(password);
    }
}