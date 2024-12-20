package ch.spamachine.utils;

public enum SmtpCommand {
    /**
     * The WAIT step.
     */
    WAIT(""),
    /**
     * The HELO step.
     */
    EHLO("EHLO %s\r\n"),
    /**
     * The SMTP extension step.
     */
    EXT(""), // Don't answer anything
    /**
     * The MAIL step.
     */
    MAIL("MAIL FROM: <%s>\r\n"),
    /**
     * The RCPT step.
     */
    RCPT("RCPT TO: %s\r\n"),
    /**
     * The DATA step.
     */
    DATA("DATA\r\n"),
    /**
     * The MESSAGE step.
     */
    MESSAGE("""
            Date: %s\r
            From: %s\r
            Subject: %s\r
            To: %s\r
            Content-Type: text/plain; charset=utf-8\r
            \r
            %s\r
            .\r
            """),
    /**
     * The QUIT step.
     */
    QUIT("QUIT\r\n") {
        @Override
        public SmtpCommand next() {
            return this; // QUIT is the last step
        }
    };

    /**
     * The value of the enum
     */
    private final String value;

    /**
     * Constructor
     *
     * @param value The value
     */
    SmtpCommand(String value) {
        this.value = value;
    }

    /**
     * Get the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    public SmtpCommand next() {
        return values()[ordinal() + 1];
    }
}