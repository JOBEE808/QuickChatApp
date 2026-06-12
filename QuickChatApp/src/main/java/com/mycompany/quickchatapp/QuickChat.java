 package com.mycompany.quickchatapp;
 
import java.util.Scanner;
 
/**
 * QuickChat — Main application.
 * Integrates Parts 1 (Login), 2 (Send Messages) and 3 (Store, Display, Report).
 */
public class QuickChat {
 
    // ---------------------------------------------------------------
    // Shared state
    // ---------------------------------------------------------------
    private static final MessageStore store = new MessageStore();
    private static String currentUser = "";
 
    // ----
    // Main Method
    // ----
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
 
        // Part 1 — Login gate
        if (!login(scanner)) {
            System.out.println("Login failed after maximum attempts. Exiting QuickChat.");
            scanner.close();
            return;
        }
 
        // Part 3 — Load previously stored messages from JSON at startup
        store.loadStoredFromJSON();
 
        System.out.println("\nWelcome to QuickChat, " + currentUser + ".");
 
        // Main menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter choice: ");
            String input = scanner.nextLine().trim();
 
            switch (input) {
                case "1":
                    sendMessagesFlow(scanner);
                    break;
                case "2":
                    System.out.println("\nComing Soon.");
                    break;
                case "3":
                    storedMessagesMenu(scanner);    // Part 3 — new option
                    break;
                case "4":
                    running = false;
                    System.out.println("Goodbye! Thank you for using QuickChat.");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1–4.");
            }
        }
        scanner.close();
    }
 
    // ---------------------------------------------------------------
    // Main menu
    // ---------------------------------------------------------------
    private static void printMainMenu() {
        System.out.println("\n╔═══════════════════════════╗");
        System.out.println("║     QuickChat  Menu        ║");
        System.out.println("╠═══════════════════════════╣");
        System.out.println("║  1) Send Messages          ║");
        System.out.println("║  2) Recently sent messages ║");
        System.out.println("║  3) Stored Messages        ║");
        System.out.println("║  4) Quit                   ║");
        System.out.println("╚═══════════════════════════╝");
    }
 
    // ---------------------------------------------------------------
    // Part 1 — Login
    // ---------------------------------------------------------------
    private static boolean login(Scanner scanner) {
        final int MAX_ATTEMPTS = 3;
        System.out.println("═══════════ QuickChat Login ═══════════");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
 
            if (!username.isEmpty() && !password.isEmpty()) {
                currentUser = username;
                System.out.println("Login successful! Welcome, " + username + ".");
                return true;
            }
            System.out.println("Username or password cannot be empty. Attempt "
                    + attempt + " of " + MAX_ATTEMPTS + ".");
        }
        return false;
    }
 
    // ---------------------------------------------------------------
    // Part 2 — Send Messages flow
    // ---------------------------------------------------------------
    private static void sendMessagesFlow(Scanner scanner) {
        int numMessages = 0;
        while (numMessages <= 0) {
            System.out.print("How many messages would you like to send? ");
            try {
                numMessages = Integer.parseInt(scanner.nextLine().trim());
                if (numMessages <= 0) System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input — please enter a whole number.");
            }
        }
 
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\n─── Message " + (i + 1) + " of " + numMessages + " ───");
            Message msg = new Message(store.returnTotalMessages() + i);
            msg.setSender(currentUser);
 
            // Validate ID (auto-generated)
            if (!msg.checkMessageID()) {
                System.out.println("ERROR: Message ID generation failed. Skipping.");
                continue;
            }
 
            // Recipient
            String recipientResult = "";
            while (!"Cell phone number successfully captured.".equals(recipientResult)) {
                System.out.print("Recipient cell (e.g. +27718693002): ");
                recipientResult = msg.checkRecipientCell(scanner.nextLine().trim());
                System.out.println(recipientResult);
            }
 
            // Message text
            String msgResult = "";
            while (!"Message ready to send.".equals(msgResult)) {
                System.out.print("Message (max 250 chars): ");
                msgResult = msg.setMessageText(scanner.nextLine());
                System.out.println(msgResult);
            }
 
            // Generate hash
            System.out.println("Message Hash: " + msg.createMessageHash());
 
            // Send / disregard / store
            System.out.println(msg.sentMessage(scanner));
 
            // Route to arrays
            store.addMessage(msg);
 
            // Print details + persist if sent or stored
            if ("Sent".equals(msg.getStatus()) || "Stored".equals(msg.getStatus())) {
                System.out.println(msg.printMessages());
                MessageStorage.storeMessage(msg);
            }
        }
 
        // Total after all messages
        System.out.println("\n══════════════════════════════════════");
        System.out.println("Total messages sent this session: " + store.returnTotalMessages());
        System.out.println("══════════════════════════════════════");
    }
 
    // ---------------------------------------------------------------
    // Part 3 — Stored Messages sub-menu
    // ---------------------------------------------------------------
    private static void storedMessagesMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║       Stored Messages Menu          ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║  a) Sender & recipient of all msgs  ║");
            System.out.println("║  b) Longest stored message          ║");
            System.out.println("║  c) Search by message ID            ║");
            System.out.println("║  d) Search by recipient             ║");
            System.out.println("║  e) Delete message by hash          ║");
            System.out.println("║  f) Full message report             ║");
            System.out.println("║  0) Back to main menu               ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.print("Enter choice: ");
 
            String choice = scanner.nextLine().trim().toLowerCase();
            System.out.println();
 
            switch (choice) {
                case "a":
                    System.out.println(store.displaySenderAndRecipient());
                    break;
                case "b":
                    System.out.println(store.displayLongestMessage());
                    break;
                case "c":
                    System.out.print("Enter Message ID to search: ");
                    System.out.println(store.searchByMessageID(scanner.nextLine().trim()));
                    break;
                case "d":
                    System.out.print("Enter recipient number to search: ");
                    System.out.println(store.searchByRecipient(scanner.nextLine().trim()));
                    break;
                case "e":
                    System.out.print("Enter message hash to delete: ");
                    System.out.println(store.deleteByHash(scanner.nextLine().trim()));
                    break;
                case "f":
                    System.out.println(store.displayReport());
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose a–f or 0.");
            }
        }
    }
}
