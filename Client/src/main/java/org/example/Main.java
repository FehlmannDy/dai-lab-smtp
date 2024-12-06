package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static int SERVER_PORT = 1025;
    static String SERVER_ADDRESS = "localhost";

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

            // Affichage des paramètres pour vérification
            System.out.println("Fichier des victimes : " + victimFile);
            System.out.println("Fichier des messages : " + messageFile);
            System.out.println("Nombre de groupes : " + groupCount);

            //TODO USE mailhandler
            for (String email : victimFile) {
                System.out.println(email);
                try(Socket socket = new Socket(SERVER_ADDRESS,SERVER_PORT);) {
                    MailHandler mailHandler = new MailHandler(socket,new MailWorker(new Mail(email,victimFile.toArray(new String[0]),messageFile.getFirst())));
                    mailHandler.run();
                }catch (IOException e) {
                    System.err.println("Erreur de connexion des mails : " + e.getMessage());
                }
            }

            if (groupCount <= 0) {
                throw new NumberFormatException("Le nombre de groupes doit être supérieur à 0.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur : <groupCount> doit être un entier positif.");
            System.exit(1);
        }


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