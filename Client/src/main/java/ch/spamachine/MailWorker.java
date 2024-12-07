package ch.spamachine;

import ch.spamachine.utils.SmtpCommand;
import ch.spamachine.utils.SmtpStatus;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * MailWorker class is responsible for processing SMTP commands related to email sending.
 * It handles the state transitions and generates responses for various SMTP commands
 * while processing a given email. It ensures proper command sequences are followed
 * during the SMTP communication.
 * Authors : Stan Stelcher (hliosone) & Dylan Fehlmann (FehlmannDy)
 */
public class MailWorker {
    private final Mail mail; // Email to be processed by this worker
    private SmtpCommand currentCommand; // Tracks the current state in SMTP exchange
    private int currentRecipientIndex = 0; // Tracks recipient being processed

    /**
     * Constructs a MailWorker instance with the specified email.
     *
     * @param mail The email to be processed.
     */
    MailWorker(Mail mail) {
        this.mail = mail;
        this.currentCommand = SmtpCommand.WAIT;
    }

    /**
     * Processes the given message and returns an appropriate SMTP response.
     * The method analyzes the received message, updates the state of the worker,
     * and generates the response based on the current state and command.
     *
     * @param message The message received from the SMTP server.
     * @return The SMTP response to be sent to the server.
     */
    public String work(String message) {
        try {
            analyzeRequest(message);
            return generateResponse();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return SmtpCommand.QUIT.getValue();
        }
    }

    /**
     * Analyzes the received request based on the current command state and updates
     * the state of the worker accordingly.
     *
     * @param request The request message received from the server.
     */
    private void analyzeRequest(String request) {
        currentCommand = switch (currentCommand) {
            case WAIT -> processServiceReady(request);
            case EHLO -> processOkResponse(request, SmtpCommand.MAIL);
            case EXT -> processOkResponse(request, currentCommand.next());
            case MAIL, MESSAGE -> processOkResponse(request, currentCommand.next());
            case RCPT -> processRecipient(request);
            case DATA -> processStartMailInput(request);
            case QUIT -> processServiceClosing(request);
        };
    }

    /**
     * Generates the appropriate response based on the current command state.
     *
     * @return The response message to be sent to the server.
     */
    private String generateResponse() {
        return switch (currentCommand) {
            case WAIT -> "";
            case EHLO -> String.format(SmtpCommand.EHLO.getValue(), "localhost");
            case EXT -> SmtpCommand.EXT.getValue();
            case MAIL -> String.format(SmtpCommand.MAIL.getValue(), mail.sender());
            case RCPT -> String.format(SmtpCommand.RCPT.getValue(), formatRecipient());
            case DATA -> SmtpCommand.DATA.getValue();
            case MESSAGE -> composeEmailMessage();
            case QUIT -> SmtpCommand.QUIT.getValue();
        };
    }

    /**
     * For all process we have the same @param, @return and @throws.
     * @param request The request message from the server.
     * @return The next command to be processed.
     * @throws IllegalStateException If the response is unexpected.
     */

    /**
     * Processes the service ready response, which is expected from the server
     * when initiating the SMTP conversation.
     */
    private SmtpCommand processServiceReady(String request) {
        if (!request.startsWith(SmtpStatus.SERVICE_READY.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.EHLO;
    }

    /**
     * Processes the "OK" response from the server and transitions to the next command.
     */
    private SmtpCommand processOkResponse(String request, SmtpCommand nextCommand) {
        if (!request.startsWith(SmtpStatus.OK.code()) & !request.startsWith(SmtpStatus.ACCEPT.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return nextCommand;
    }

    /**
     * Processes the recipient response and transitions to the next command based on
     * the number of recipients.
     */
    private SmtpCommand processRecipient(String request) {
        if (!request.startsWith(SmtpStatus.OK.code()) & !request.startsWith(SmtpStatus.ACCEPT.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return (++currentRecipientIndex < mail.receivers().length) ? SmtpCommand.RCPT : SmtpCommand.DATA;
    }

    /**
     * Processes the start of the mail input and transitions to the message command.
     */
    private SmtpCommand processStartMailInput(String request) {
        if (!request.startsWith(SmtpStatus.START_MAIL_INPUT.code()) & !request.startsWith(SmtpStatus.ACCEPT.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.MESSAGE;
    }

    /**
     * Processes the service closing response, signaling the end of the SMTP session.
     */
    private SmtpCommand processServiceClosing(String request) {
        if (!request.startsWith(SmtpStatus.SERVICE_CLOSING.code()) & !request.startsWith(SmtpStatus.ACCEPT.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.QUIT;
    }

    /**
     * Formats the recipient for inclusion in the SMTP message.
     *
     * @return The formatted recipient string.
     */
    private String formatRecipient() {
        return String.format("<%s>", mail.receivers()[currentRecipientIndex]);
    }

    /**
     * Composes the full email message including headers like date, sender, subject, etc.
     *
     * @return The composed email message.
     */
    private String composeEmailMessage() {
        String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date());
        String sender = String.format("%s <%s>", mail.sender(), mail.sender());
        String subject = mail.message().split("\\.")[0];
        String encodedSubject = "=?utf-8?B?" + Base64.getEncoder().encodeToString(subject.getBytes()) + "?=";
        String receivers = String.join(", ", mail.receivers());
        return String.format(SmtpCommand.MESSAGE.getValue(), date, sender, encodedSubject, receivers, mail.message());
    }

    /**
     * Returns the current command that the MailWorker is processing.
     *
     * @return The current SMTP command.
     */
    public SmtpCommand getCurrentCommand() {
        return currentCommand;
    }
}
