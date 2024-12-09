package ch.spamachine;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void testEmailParserValidEmails() {
        String filePath = "src/test/resources/valid-victims.txt"; // Test with valid victim files
        List<String> emails = Main.EmailParser(filePath);
        assertNotNull(emails);
        assertFalse(emails.isEmpty());
        assertTrue(emails.contains("john.doe@example.com"));
    }

    @Test
    void testEmailParserInvalidEmails() {
        String filePath = "src/test/resources/invalid-victims.txt"; // Test with invalid victim files
        List<String> emails = Main.EmailParser(filePath);
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    void testSplitIntoGroups() {
        List<String> victims = List.of(
                "a@example.com", "b@example.com", "c@example.com", "d@example.com", "e@example.com"
        );
        List<List<String>> groups = Main.splitIntoGroups(victims, 2);

        assertEquals(3, groups.size());
        assertEquals(2, groups.get(0).size());
        assertEquals(2, groups.get(1).size());
        assertEquals(1, groups.get(2).size());
    }

    @Test
    void testEmptyVictimFile() {
        String filePath = "src/test/resources/empty-victims.txt"; // Test with an empty file
        List<String> emails = Main.EmailParser(filePath);
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    void testValidMessageParsing() {
        String filePath = "src/test/resources/valid-messages.txt"; // Test with valid file
        List<String> messages = Main.MessageParser(filePath);
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertEquals(3, messages.size());
    }
}
