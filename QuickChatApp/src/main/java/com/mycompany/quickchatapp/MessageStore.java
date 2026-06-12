package com.mycompany.quickchatapp;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * MessageStore — Part 3 core class.
 * 
 * Manages five arrays and exposes all the report / search operations
 * required by the assignment:
 *
 *   a. Display sender + recipient of all stored messages
 *   b. Display the longest stored message
 *   c. Search by message ID  → show recipient + message
 *   d. Search by recipient   → show all their messages
 *   e. Delete by message hash
 *   f. Full detail report of all stored messages
 */
public class MessageStore {
 
    // ---------------------------------------------------------------
    // The five required arrays  (Part 3 — no hard-coding)
    // ---------------------------------------------------------------
    private final ArrayList<Message> sentMessages       = new ArrayList<>();
    private final ArrayList<Message> disregardedMessages= new ArrayList<>();
    private final ArrayList<Message> storedMessages     = new ArrayList<>();   // "Stored" status
    private final ArrayList<String>  messageHashes      = new ArrayList<>();
    private final ArrayList<String>  messageIDs         = new ArrayList<>();
 
    // ---------------------------------------------------------------
    // Population — called from QuickChat after each message is handled
    // ---------------------------------------------------------------
 
    /**
     * Routes msg into the correct array(s) based on its status,
     * and always adds its hash + ID to the tracking arrays.
     */
    public void addMessage(Message msg) {
        if (msg == null) return;
 
        // Always track ID and hash
        if (msg.getMessageID() != null)   messageIDs.add(msg.getMessageID());
        if (msg.getMessageHash() != null) messageHashes.add(msg.getMessageHash());
 
        switch (msg.getStatus() == null ? "" : msg.getStatus()) {
            case "Sent":
                sentMessages.add(msg);
                break;
            case "Disregarded":
                disregardedMessages.add(msg);
                break;
            case "Stored":
                storedMessages.add(msg);
                break;
            default:
                break;
        }
    }
 
    /**
     * Loads messages that were previously saved to the JSON file and
     * populates the storedMessages array.  Replaces any existing entries.
     */
    public void loadStoredFromJSON() {
        List<Message> fromFile = MessageStorage.readFromJSON();
        for (Message m : fromFile) {
            // Avoid duplicates (e.g. if already added via addMessage this session)
            boolean exists = false;
            for (Message s : storedMessages) {
                if (s.getMessageID().equals(m.getMessageID())) { exists = true; break; }
            }
            if (!exists) {
                storedMessages.add(m);
                if (!messageIDs.contains(m.getMessageID()))
                    messageIDs.add(m.getMessageID());
                if (m.getMessageHash() != null && !messageHashes.contains(m.getMessageHash()))
                    messageHashes.add(m.getMessageHash());
            }
        }
    }
 
    // ---------------------------------------------------------------
    // a. Display sender and recipient of ALL stored messages
    // ---------------------------------------------------------------
    public String displaySenderAndRecipient() {
        List<Message> all = allMessages();
        if (all.isEmpty()) return "No messages found.";
 
        StringBuilder sb = new StringBuilder();
        sb.append("=== Sender & Recipient of All Messages ===\n");
        for (Message m : all) {
            sb.append(String.format("  Sender: %-20s  Recipient: %s%n",
                    safe(m.getSender()), safe(m.getRecipient())));
        }
        return sb.toString().trim();
    }
 
    // ---------------------------------------------------------------
    // b. Display the longest stored message
    // ---------------------------------------------------------------
    public String displayLongestMessage() {
        List<Message> all = allMessages();
        if (all.isEmpty()) return "No messages found.";
 
        Message longest = all.get(0);
        for (Message m : all) {
            String t = m.getMessageText();
            if (t != null && t.length() > safeLen(longest.getMessageText())) {
                longest = m;
            }
        }
        return "=== Longest Message ===\n"
             + "Length    : " + safeLen(longest.getMessageText()) + " characters\n"
             + longest.printMessages();
    }
 
    // ---------------------------------------------------------------
    // c. Search for a message by ID → return recipient + message
    // ---------------------------------------------------------------
    public String searchByMessageID(String id) {
        if (id == null || id.isBlank()) return "Please enter a valid message ID.";
 
        for (Message m : allMessages()) {
            if (id.trim().equalsIgnoreCase(m.getMessageID())) {
                return "=== Message Found ===\n"
                     + "Message ID : " + m.getMessageID()   + "\n"
                     + "Recipient  : " + safe(m.getRecipient()) + "\n"
                     + "Message    : " + safe(m.getMessageText());
            }
        }
        return "No message found with ID: " + id;
    }
 
    // ---------------------------------------------------------------
    // d. Search for all messages for a particular recipient
    // ---------------------------------------------------------------
    public String searchByRecipient(String recipient) {
        if (recipient == null || recipient.isBlank())
            return "Please enter a valid recipient number.";
 
        List<Message> matches = new ArrayList<>();
        for (Message m : allMessages()) {
            if (recipient.trim().equalsIgnoreCase(safe(m.getRecipient()))) {
                matches.add(m);
            }
        }
        if (matches.isEmpty()) return "No messages found for recipient: " + recipient;
 
        StringBuilder sb = new StringBuilder();
        sb.append("=== Messages for ").append(recipient).append(" ===\n");
        for (Message m : matches) sb.append(m.printMessages()).append("\n");
        return sb.toString().trim();
    }
 
    // ---------------------------------------------------------------
    // e. Delete a message using the message hash
    // ---------------------------------------------------------------
    public String deleteByHash(String hash) {
        if (hash == null || hash.isBlank()) return "Please enter a valid message hash.";
 
        boolean deleted = removeFromList(sentMessages,        hash)
                       || removeFromList(disregardedMessages, hash)
                       || removeFromList(storedMessages,      hash);
 
        if (deleted) {
            messageHashes.remove(hash.trim().toUpperCase());
            // Persist the updated stored list
            MessageStorage.writeAll(storedMessages);
            return "Message with hash \"" + hash + "\" has been successfully deleted.";
        }
        return "No message found with hash: " + hash;
    }
 
    private boolean removeFromList(ArrayList<Message> list, String hash) {
        return list.removeIf(m ->
                m.getMessageHash() != null &&
                m.getMessageHash().equalsIgnoreCase(hash.trim()));
    }
 
    // ---------------------------------------------------------------
    // f. Full detail report of ALL stored messages
    // ---------------------------------------------------------------
    public String displayReport() {
        List<Message> all = allMessages();
        if (all.isEmpty()) return "No messages to report.";
 
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append("║         QUICKCHAT — MESSAGE REPORT        ║\n");
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║  Total messages     : %-20d║\n", all.size()));
        sb.append(String.format("║  Sent               : %-20d║\n", sentMessages.size()));
        sb.append(String.format("║  Stored             : %-20d║\n", storedMessages.size()));
        sb.append(String.format("║  Disregarded        : %-20d║\n", disregardedMessages.size()));
        sb.append("╚══════════════════════════════════════════╝\n\n");
 
        sb.append("--- SENT MESSAGES ---\n");
        appendList(sb, sentMessages);
 
        sb.append("\n--- STORED MESSAGES ---\n");
        appendList(sb, storedMessages);
 
        sb.append("\n--- DISREGARDED MESSAGES ---\n");
        appendList(sb, disregardedMessages);
 
        return sb.toString().trim();
    }
 
    private void appendList(StringBuilder sb, List<Message> list) {
        if (list.isEmpty()) { sb.append("  (none)\n"); return; }
        for (Message m : list) sb.append(m.printMessages()).append("\n");
    }
 
    // ---------------------------------------------------------------
    // Utility — returnTotalMessages (Part 2 requirement)
    // ---------------------------------------------------------------
    public int returnTotalMessages() {
        return sentMessages.size();
    }
 
    // ---------------------------------------------------------------
    // Getters for the arrays (used by tests)
    // ---------------------------------------------------------------
    public ArrayList<Message> getSentMessages()        { return sentMessages; }
    public ArrayList<Message> getDisregardedMessages() { return disregardedMessages; }
    public ArrayList<Message> getStoredMessages()      { return storedMessages; }
    public ArrayList<String>  getMessageHashes()       { return messageHashes; }
    public ArrayList<String>  getMessageIDs()          { return messageIDs; }
 
    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------
    /** Returns all messages across all three status arrays. */
    private List<Message> allMessages() {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all;
    }
 
    private String safe(String s)    { return s == null ? "N/A" : s; }
    private int safeLen(String s)    { return s == null ? 0 : s.length(); }
}
