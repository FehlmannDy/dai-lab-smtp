package ch.spamachine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    static String WELCOME_MESSAGE =
            " ######  ########     ###    ##     ##    ###     ######  ##     ## #### ##    ## ######## \n" +
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
            System.err.println("Usage: java -jar Client-*.jar <victimFile> <messageFile> <groupCount>");
            System.exit(1);
        }

        List<String> victimFile = EmailParser(args[0]);
        List<String> messageFile = MessageParser(args[1]);
        int groupCount;

        try {
            System.out.println(WELCOME_MESSAGE);

            // Conversion of the third argument to an integer (number of groups)
            groupCount = Integer.parseInt(args[2]);

            List<List<String>> groups = splitIntoGroups(victimFile, groupCount);

            // Print the parameters for verification
            System.out.println("Victims file : " + args[0]);
            System.out.println("Messages file : " + args[1]);
            System.out.println("Number of groups : " + groups.size());
            System.out.println("Number of victims by group : " + groupCount + "\n");


            for (List<String> group : groups) {
                System.out.println("Sending prank email(s) to group number : " + (groups.indexOf(group) + 1) + "...");

                // Take a message (remove it so it's not reused)
                String message = messageFile.isEmpty() ? "Default Message" : messageFile.removeFirst();

                // Create and send the email for each group
                for (String email : group) {
                    try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {

                        // Get a random sender from the group that is not the current recipient
                        String sender;
                        do {
                            sender = group.get(new Random().nextInt(group.size()));
                        } while (sender.equals(email));
                        Mail mail = new Mail(email, new String[]{email}, message);
                        MailHandler mailHandler = new MailHandler(socket, new MailWorker(mail));
                        mailHandler.run();
                    } catch (IOException e) {
                        System.err.println("Error mail connection : " + e.getMessage());
                    }
                }

                System.out.println("Prank email(s) sent to the following victim(s) : " + group);
            }

            System.out.println("\nAll prank emails have been sent successfully ! Thanks for using the SPAMACHINE !");

            if (groupCount <= 0) {
                throw new NumberFormatException("Group size needs to be at least 1.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error : <groupCount> need to be a positive number.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error : " + e.getMessage());
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

        // The list to store valid emails
        List<String> validEmails = new ArrayList<>();

        // Regex to validate emails (reinforced)
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);

        try (BufferedReader br = new BufferedReader(new FileReader(victimsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Remove spaces around
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    validEmails.add(line); // Add the email to the list if it is valid
                }
            }
            return validEmails;
        } catch (IOException e) {
            System.err.println("Error reading the file : " + e.getMessage());
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
            System.err.println("Error reading the file : " + e.getMessage());
            return null;
        }
    }
}