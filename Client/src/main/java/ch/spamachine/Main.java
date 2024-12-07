package ch.spamachine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for sending emails to a list of recipients divided into groups.
 * The program reads victim emails and messages from files, then sends emails to recipients in groups using SMTP.
 * Authors : Stan Stelcher (hliosone) & Dylan Fehlmann (FehlmannDy)
 */
public class Main {

    static int SERVER_PORT = 1025;
    static String SERVER_ADDRESS = "localhost";
    static String WELCOME_MESSAGE = " ######  ########     ###    ##     ##    ###     ######  ##     ## #### ##    ## ######## \n" +
            "##    ## ##     ##   ## ##   ###   ###   ## ##   ##    ## ##     ##  ##  ###   ## ##       \n" +
            "##       ##     ##  ##   ##  #### ####  ##   ##  ##       ##     ##  ##  ####  ## ##       \n" +
            " ######  ########  ##     ## ## ### ## ##     ## ##       #########  ##  ## ## ## ######   \n" +
            "      ## ##        ######### ##     ## ######### ##       ##     ##  ##  ##  #### ##       \n" +
            "##    ## ##        ##     ## ##     ## ##     ## ##    ## ##     ##  ##  ##   ### ##       \n" +
            " ######  ##        ##     ## ##     ## ##     ##  ######  ##     ## #### ##    ## ######## \n";

    /**
     * Main entry point of the program. It parses command-line arguments and sends emails to victims in specified groups.
     *
     * @param args The command-line arguments:
     *             1. Victim file path
     *             2. Message file path
     *             3. Number of groups to divide victims into
     */
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
            System.out.println(WELCOME_MESSAGE);

            // Conversion du troisième argument en entier
            groupCount = Integer.parseInt(args[2]);

            List<List<String>> groups = splitIntoGroups(victimFile, groupCount);

            // Affichage des paramètres pour vérification
            System.out.println("Victims file : " + args[0]);
            System.out.println("Messages file : " + args[1]);
            System.out.println("Number of groups : " + groupCount);

            //TODO USE mailhandler
            for (List<String> group : groups) {
                System.out.println("Send to this group : " + group);

                // Prendre un message (le retirer pour qu'il ne soit pas réutilisé)
                String message = messageFile.isEmpty() ? "Default Message" : messageFile.removeFirst();

                // Créer et envoyer l'e-mail pour chaque groupe
                for (String email : group) {
                    try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
                        Mail mail = new Mail(email, group.toArray(new String[0]), message);
                        MailHandler mailHandler = new MailHandler(socket, new MailWorker(mail));
                        mailHandler.run();
                    } catch (IOException e) {
                        System.err.println("Error mail connection : " + e.getMessage());
                    }
                }
            }

            if (groupCount <= 0) {
                throw new NumberFormatException("Group need to be more than 0.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error : <groupCount> need to be a positive number.");
            System.exit(1);
        }


    }

    /**
     * Splits the given list of emails into groups of the specified size.
     *
     * @param emails The list of email addresses to split.
     * @param groupSize The size of each group.
     * @return A list of lists, where each inner list is a group of emails.
     */
    public static List<List<String>> splitIntoGroups(List<String> emails, int groupSize) {
        List<List<String>> groups = new ArrayList<>();
        for (int i = 0; i < emails.size(); i += groupSize) {
            groups.add(emails.subList(i, Math.min(i + groupSize, emails.size())));
        }
        return groups;
    }

    /**
     * Parses the email addresses from the given victim file and validates them using a regular expression.
     *
     * @param victimsPath The path to the file containing victim email addresses.
     * @return A list of valid email addresses.
     */
    public static List<String> EmailParser(String victimsPath) {
        // La liste pour stocker les emails valides
        List<String> validEmails = new ArrayList<>();

        // Expression régulière pour valider les emails (renforcée)
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);

        try (BufferedReader br = new BufferedReader(new FileReader(victimsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Supprime les espaces autour
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

    /**
     * Parses the messages from the given message file.
     *
     * @param messagesPath The path to the file containing the messages.
     * @return A list of message strings.
     */
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