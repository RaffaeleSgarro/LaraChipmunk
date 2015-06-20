package app;

public class SMTPSettings {

    public String user;
    public String host;
    public String password;
    public String port;
    public String smtpAuth;
    public String startTls;
    public String from;

    public void loadFromConfiguration(App app) {
        host = app.getConfigurationProperty("mail.smtp.host");
        port = app.getConfigurationProperty("mail.smtp.port");
        user = app.getConfigurationProperty("user");
        smtpAuth = app.getConfigurationProperty("mail.smtp.auth");
        startTls = app.getConfigurationProperty("mail.smtp.starttls.enable");
        from = app.getConfigurationProperty("from");

        password = app.getLastUsedPassword();
    }
}