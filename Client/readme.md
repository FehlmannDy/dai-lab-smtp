# DAI Lab4 : SMTP


```bash
 ######  ########     ###    ##     ##    ###     ######  ##     ## #### ##    ## ######## 
##    ## ##     ##   ## ##   ###   ###   ## ##   ##    ## ##     ##  ##  ###   ## ##       
##       ##     ##  ##   ##  #### ####  ##   ##  ##       ##     ##  ##  ####  ## ##       
 ######  ########  ##     ## ## ### ## ##     ## ##       #########  ##  ## ## ## ######   
      ## ##        ######### ##     ## ######### ##       ##     ##  ##  ##  #### ##       
##    ## ##        ##     ## ##     ## ##     ## ##    ## ##     ##  ##  ##   ### ##       
 ######  ##        ##     ## ##     ## ##     ##  ######  ##     ## #### ##    ## ########
 ```

---

 ## Description

Develop a client application that automatically plays e-mail pranks on a list of victims.

Usage :

```bash
java -jar Client-*.jar <victimFile> <messageFile> <groupCount>
```

---

## Setup the lab environment

First init the docker compose file

This is the [MailDev](https://github.com/maildev/maildev) fake SMTP server

```bash
docker compose start
```

You can build the project with [Maven](https://maven.apache.org/index.html) by the pom.xml file
```bash
mvn -f Client/pom.xml clean package
```

You can run the spamachine with this following command

```bash
java -jar Client-1.0-SNAPSHOT.jar <victimFile> <messageFile> <groupCount>
```

---

## Implementation

The program is divided into several modules, each responsible for specific tasks:

### Main
- Entry point of the program.
- Reads input files, processes arguments and initializes the email-sending process.

### MailHandler
- Manages communication with the SMTP server.
- Sends commands and reads responses from the server.

### MailWorker
- Handles SMTP protocol logic.
- Ensures proper sequencing of commands like EHLO, MAIL, RCPT, etc.

### Mail
- Represents an email with sender, recipients, and message content.

### Enums SmtpCommand and SmtpStatus
- Define the commands and expected server responses for the SMTP protocol.


![Diagramme des classes](./SmtpClassDiagram.jpg)


## How It Works

### Reading Input
- Victim emails and prank messages are read from files and validated.
- Victims are grouped based on the specified number of groups.

### SMTP Communication
- The program connects to the MailDev SMTP server (via localhost:1025).
- Commands are sent sequentially (EHLO, MAIL, RCPT, DATA, etc.) to send each prank email.

### Error Handling
- If an unexpected server response is received, the program attempts to exit with a QUIT command.


## Screenshots


