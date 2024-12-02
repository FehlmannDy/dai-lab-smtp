package org.example;

public record Mail(String sender, String[] receivers, String message) {
}
