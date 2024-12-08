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


![Diagramme des classes](./Client/Rendu/SmtpClassDiagram.jpg)


## How It Works

### Reading Input
- Victim emails and prank messages are read from files and validated.
- Victims are grouped based on the specified number of groups.

### SMTP Communication
- The program connects to the MailDev SMTP server (via localhost:1025).
- Commands are sent sequentially (EHLO, MAIL, RCPT, DATA, etc.) to send each prank email.

### Error Handling
- If an unexpected server response is received, the program attempts to exit with a QUIT command.


## Program Output

### MailDev SMTP Server

### Program Demo Output
![Diagramme des classes](./Client/Rendu/demo-screenshot.png)

In the screenshot above, the program sends prank emails to 5 groups of victims. We choosed to have groups of 2 victims but as we only have 9 victims, the last group has only one victim (handled by the program). The program sends a prank email to each victim, with the sender displayed being a random victim from the entire list.

### MailDev Web Interface Output
![Diagramme des classes](./Client/Rendu/maildev-demo-screenshot.png)

In the screenshot above, the MailDev web interface shows the prank emails received by the victims. Each email is displayed with the sender, recipient, and message content. The email content is a random prank message from the input file.

### Empty File Handling
![Diagramme des classes](./Client/Rendu/emptyfile-screenshot.png)

In the screenshot above, the program handles an empty victim file by displaying an error message and exiting.