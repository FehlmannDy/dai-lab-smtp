package ch.spamachine;

/**
 * The {@code Mail} record represents an email with a sender, recipients, and a message.
 * This class is used to encapsulate the data required for sending an email, including
 *
 * This record is immutable, meaning its fields cannot be modified once the object is created.
 * Authors : Stan Stelcher (hliosone) & Dylan Fehlmann (FehlmannDy)
 */
public record Mail(String sender, String[] receivers, String message) {}
