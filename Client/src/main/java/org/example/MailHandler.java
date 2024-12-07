package org.example;

import java.io.*;
import java.net.Socket;
import static java.nio.charset.StandardCharsets.UTF_8;

// ---Custom import---
import static org.example.utils.SmtpCommand.QUIT;

/**
 * MailHandler class handles the communication between the client and the SMTP server.
 * It processes commands sent to the server and sends appropriate responses back using a worker.
 * This class implements the {@link Runnable} interface to allow execution in a separate thread.
 * Authors : Stan Stelcher (hliosone) & Dylan Fehlmann (FehlmannDy)
 */
public class MailHandler implements Runnable {
    final Socket socket;            // The socket through which communication with the SMTP server happens
    final MailWorker mailWorker;    // The MailWorker that handles processing of SMTP commands

    /**
     * Constructs a MailHandler instance with the specified socket and MailWorker.
     *
     * @param socket The socket to use for communication with the SMTP server.
     * @param mailWorker The worker that processes SMTP commands.
     */
    public MailHandler(Socket socket, MailWorker mailWorker) {
        this.socket = socket;
        this.mailWorker = mailWorker;
    }

    /**
     * This method runs the main loop for handling the SMTP communication.
     * It reads the SMTP server responses, passes them to the {@link MailWorker} for processing,
     * and sends the results back to the SMTP server until the QUIT command is received.
     */
    public void run() {
        try(socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))){

            String line;
            while ((line = in.readLine()) != null && mailWorker.getCurrentCommand() != QUIT) {
                String response = mailWorker.work(line);
                if(!response.isEmpty()) {
                    out.write(response);
                    out.flush();
                }
            }

        }catch (IOException e) {
            System.err.println("Error : " + e.getMessage());
            System.exit(1);
        }
    }
}
