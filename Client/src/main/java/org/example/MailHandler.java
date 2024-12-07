package org.example;

import org.example.utils.SmtpCommand;

import java.io.*;
import java.net.Socket;
import static java.nio.charset.StandardCharsets.UTF_8;

// ---Custom import---
import static org.example.utils.SmtpCommand.QUIT;

public class MailHandler implements Runnable {
    final Socket socket;
    final MailWorker mailWorker;

    public MailHandler(Socket socket, MailWorker mailWorker) {
        this.socket = socket;
        this.mailWorker = mailWorker;
    }

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
