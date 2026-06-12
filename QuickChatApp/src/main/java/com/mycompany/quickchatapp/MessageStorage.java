package com.mycompany.quickchatapp;
 
import java.io.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Handles persisting messages to and loading them from a JSON file.
 * Uses hand-rolled JSON to avoid external library dependencies.
 */
public class MessageStorage {
 
    static final String FILE_PATH = "stored_messages.json";
 
    // ---------------------------------------------------------------
    // WRITE — save a single message (append mode)
    // ---------------------------------------------------------------
    public static void storeMessage(Message msg) {
        // Load existing list, add new message, rewrite whole file
        List<Message> existing = readFromJSON();
        // Avoid duplicates
        for (Message m : existing) {
            if (m.getMessageID().equals(msg.getMessageID())) return;
        }
        existing.add(msg);
        writeAll(existing);
    }
 
    // ---------------------------------------------------------------
    // WRITE — overwrite file with full list
    // ---------------------------------------------------------------
    public static void writeAll(List<Message> messages) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < messages.size(); i++) {
            sb.append(toJson(messages.get(i)));
            if (i < messages.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            fw.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error writing JSON: " + e.getMessage());
        }
    }
 
    // ---------------------------------------------------------------
    // READ — parse JSON file back into Message objects
    // ---------------------------------------------------------------
    public static List<Message> readFromJSON() {
        List<Message> result = new ArrayList<>();
        File f = new File(FILE_PATH);
        if (!f.exists()) return result;
 
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            // Collect one object block at a time
            StringBuilder block = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("{") || line.equals("{")) { block.setLength(0); block.append("{"); }
                else if (line.startsWith("{"))             { block.setLength(0); block.append(line); }
                else if (line.equals("}") || line.equals("},")) {
                    block.append("}");
                    Message m = parseBlock(block.toString());
                    if (m != null) result.add(m);
                    block.setLength(0);
                } else {
                    block.append(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading JSON: " + e.getMessage());
        }
        return result;
    }
 
    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------
    private static Message parseBlock(String block) {
        try {
            String id          = jsonVal(block, "messageID");
            int    num         = Integer.parseInt(jsonVal(block, "messageNumber"));
            String sender      = jsonVal(block, "sender");
            String recipient   = jsonVal(block, "recipient");
            String text        = jsonVal(block, "messageText");
            String hash        = jsonVal(block, "messageHash");
            String status      = jsonVal(block, "status");
            return new Message(id, num, sender, recipient, text, hash, status);
        } catch (Exception e) {
            return null; // skip malformed blocks
        }
    }
 
    /** Extracts the string value for a given key from a JSON snippet. */
    static String jsonVal(String json, String key) {
        // Handles both quoted strings and bare numbers
        String quoted = "\"" + key + "\"";
        int k = json.indexOf(quoted);
        if (k < 0) return "";
        int colon = json.indexOf(":", k + quoted.length());
        if (colon < 0) return "";
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("\"")) {
            // String value
            int start = rest.indexOf('"') + 1;
            int end   = rest.indexOf('"', start);
            return (end > start) ? rest.substring(start, end) : "";
        } else {
            // Numeric value
            int end = 0;
            while (end < rest.length() && (Character.isDigit(rest.charAt(end)) || rest.charAt(end) == '-')) end++;
            return rest.substring(0, end).trim();
        }
    }
 
    private static String toJson(Message m) {
        return "  {\n"
             + "    \"messageID\": \""     + esc(m.getMessageID())    + "\",\n"
             + "    \"messageNumber\": "   + m.getMessageNumber()      + ",\n"
             + "    \"sender\": \""        + esc(m.getSender())        + "\",\n"
             + "    \"recipient\": \""     + esc(m.getRecipient())     + "\",\n"
             + "    \"messageText\": \""   + esc(m.getMessageText())   + "\",\n"
             + "    \"messageHash\": \""   + esc(m.getMessageHash())   + "\",\n"
             + "    \"status\": \""        + esc(m.getStatus())        + "\"\n"
             + "  }";
    }
 
    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
