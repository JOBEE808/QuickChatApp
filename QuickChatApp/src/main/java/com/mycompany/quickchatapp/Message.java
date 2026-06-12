package com.mycompany.quickchatapp;
 
import java.util.Random;
import java.util.Scanner;
 
/**
 * Message class for QuickChat application (Parts 1, 2 & 3).
 * Handles message creation, validation, hashing, sending, and storage.
 */
public class Message {
 
    private String messageID;
    private int    messageNumber;
    private String sender;        // Part 3, logged-in username
    private String recipient;
    private String messageText;
    private String messageHash;
    private String status;        // "Sent" | "Stored" | "Disregarded"
 
    // ---
    // Constructors
    // ---
    public Message(int messageNumber) {
        this.messageNumber = messageNumber;
        this.messageID     = generateMessageID();
    }
 
    /** Full constructor used when rebuilding messages from JSON. */
    public Message(String messageID, int messageNumber, String sender,
                   String recipient, String messageText,
                   String messageHash, String status) {
        this.messageID     = messageID;
        this.messageNumber = messageNumber;
        this.sender        = sender;
        this.recipient     = recipient;
        this.messageText   = messageText;
        this.messageHash   = messageHash;
        this.status        = status;
    }
 
    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------
    private String generateMessageID() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(rand.nextInt(10));
        return sb.toString();
    }
 
    // ---------------------------------------------------------------
    // Required methods, Part 2
    // ---------------------------------------------------------------
 
    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }
 
    /**
     * Ensures the recipient cell number starts with an international
     * code (+) and is no more than 13 characters long.
     */
    public String checkRecipientCell(String cell) {
        if (cell != null && cell.startsWith("+") && cell.length() <= 13) {
            this.recipient = cell;
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
             + "international code. Please correct the number and try again.";
    }
 
    /** Validates the 250-character limit and stores the message text. */
    public String setMessageText(String text) {
        if (text == null || text.length() > 250) {
            int excess = (text != null) ? text.length() - 250 : 0;
            return "Message exceeds 250 characters by " + excess
                 + " [enter number here]; please reduce the size.";
        }
        this.messageText = text;
        return "Message ready to send.";
    }
 
    /**
     * Creates and returns the Message Hash.
     * Format: [first 2 chars of ID]:[messageNumber]:[FIRSTWORDLASTWORD]
     */
    public String createMessageHash() {
        if (messageText == null || messageText.trim().isEmpty()) return "";
        String firstTwo  = messageID.substring(0, 2);
        String[] words   = messageText.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^a-zA-Z]", "").toUpperCase();
        String lastWord  = words[words.length - 1].replaceAll("[^a-zA-Z]", "").toUpperCase();
        this.messageHash = firstTwo + ":" + messageNumber + ":" + firstWord + lastWord;
        return this.messageHash;
    }
 
    /** Lets the user choose: send / disregard / store. */
    public String sentMessage(Scanner scanner) {
        System.out.println("\nWhat would you like to do with this message?");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Enter choice: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            switch (choice) {
                case 1: this.status = "Sent";        return "Message successfully sent";
                case 2: this.status = "Disregarded"; return "Press 0 to delete the message";
                case 3: this.status = "Stored";      return "Message successfully stored";
                default: return "Invalid option selected.";
            }
        } catch (NumberFormatException e) {
            return "Invalid input. Please enter a number.";
        }
    }
 
    /** Returns formatted details: Message ID, Hash, Recipient, Message. */
    public String printMessages() {
        return "----------------------------------------\n"
             + "Sender       : " + safe(sender)      + "\n"
             + "Message ID   : " + safe(messageID)   + "\n"
             + "Message Hash : " + safe(messageHash) + "\n"
             + "Recipient    : " + safe(recipient)   + "\n"
             + "Message      : " + safe(messageText) + "\n"
             + "Status       : " + safe(status)      + "\n"
             + "----------------------------------------";
    }
 
    private String safe(String s) { return s == null ? "N/A" : s; }
 
    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public String getMessageID()    { return messageID; }
    public int    getMessageNumber(){ return messageNumber; }
    public String getSender()       { return sender; }
    public String getRecipient()    { return recipient; }
    public String getMessageText()  { return messageText; }
    public String getMessageHash()  { return messageHash; }
    public String getStatus()       { return status; }
 
    // ---------------------------------------------------------------
    // Setters (package-private — used by tests & JSON loader)
    // ---------------------------------------------------------------
    void setMessageID(String id)            { this.messageID = id; }
    void setSender(String s)                { this.sender = s; }
    void setRecipient(String r)             { this.recipient = r; }
    void setMessageTextDirect(String t)     { this.messageText = t; }
    void setMessageHashDirect(String h)     { this.messageHash = h; }
    void setStatus(String st)               { this.status = st; }
}
