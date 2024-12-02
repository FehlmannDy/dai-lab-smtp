package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java -jar smtpclient-*.jar <victimFile> <messageFile> <groupCount>");
            System.exit(1);
        }

        // Récupération des arguments
        List<String> victimFile = EmailParser(args[0]);
        List<String> messageFile = MessageParser(args[1]);
        int groupCount;

        try {
            // Conversion du troisième argument en entier
            groupCount = Integer.parseInt(args[2]);
            if (groupCount <= 0) {
                throw new NumberFormatException("Le nombre de groupes doit être supérieur à 0.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur : <groupCount> doit être un entier positif.");
            System.exit(1);
            return; // Ajouté pour que le compilateur comprenne que groupCount est défini
        }

        // Affichage des paramètres pour vérification
        System.out.println("Fichier des victimes : " + victimFile);
        System.out.println("Fichier des messages : " + messageFile);
        System.out.println("Nombre de groupes : " + groupCount);
    }

    public static List<String> EmailParser(String victimsPath){
        // La liste pour stocker les emails valides
        List<String> validEmails = new ArrayList<>();

        // Expression régulière pour valider les emails
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);

        try (BufferedReader br = new BufferedReader(new FileReader(victimsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    validEmails.add(line); // Ajout à la liste si l'email est valide
                }
            }
            return validEmails;
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return null;
        }
    }

    public static List<String> MessageParser(String messagesPath){
        List<String> messages = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(messagesPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                messages.add(line);
            }
            return messages;
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return null;
        }
    }
}