package org.example;

import org.example.utils.SmtpCommand;
import org.example.utils.SmtpStatus;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class MailWorker {
    private final Mail mail; // Email to be processed by this worker
    private SmtpCommand currentCommand; // Tracks the current state in SMTP exchange
    private int currentRecipientIndex = 0; // Tracks recipient being processed

    MailWorker(Mail mail) {
        this.mail = mail;
        this.currentCommand = SmtpCommand.WAIT;
    }

    public String work(String message) {
        try {
            analyzeRequest(message);
            return generateResponse();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return SmtpCommand.QUIT.getValue();
        }
    }

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

    private SmtpCommand processServiceReady(String request) {
        if (!request.startsWith(SmtpStatus.SERVICE_READY.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.EHLO;
    }

    private SmtpCommand processOkResponse(String request, SmtpCommand nextCommand) {
        if (!request.startsWith(SmtpStatus.OK.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return nextCommand;
    }

    private SmtpCommand processRecipient(String request) {
        if (!request.startsWith(SmtpStatus.OK.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return (++currentRecipientIndex < mail.receivers().length) ? SmtpCommand.RCPT : SmtpCommand.DATA;
    }

    private SmtpCommand processStartMailInput(String request) {
        if (!request.startsWith(SmtpStatus.START_MAIL_INPUT.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.MESSAGE;
    }

    private SmtpCommand processServiceClosing(String request) {
        if (!request.startsWith(SmtpStatus.SERVICE_CLOSING.code()))
            throw new IllegalStateException("Unexpected response: " + request);
        return SmtpCommand.QUIT;
    }

    private String formatRecipient() {
        return String.format("<%s>", mail.receivers()[currentRecipientIndex]);
    }

    private String composeEmailMessage() {
        String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date());
        String sender = String.format("%s <%s>", mail.sender(), mail.sender());
        String subject = mail.message().split("\\.")[0];
        String encodedSubject = "=?utf-8?B?" + Base64.getEncoder().encodeToString(subject.getBytes()) + "?=";
        String receivers = String.join(", ", mail.receivers());
        return String.format(SmtpCommand.MESSAGE.getValue(), date, sender, encodedSubject, receivers, mail.message());
    }

    public SmtpCommand getCurrentCommand() {
        return currentCommand;
    }
}
