package com.aichat;

import java.io.File;
import java.io.FileInputStream;
// import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
// import java.io.OutputStream;
import java.util.Scanner;

public class ConsoleChat extends ConvertToText {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Replace with your API key
        String API_KEY = System.getenv("ANTHROPIC_API_KEY"); // Use the passed API key instead of environment variable
        boolean useGPT = false; // true for GPT, false for Claude

        AIClient aiClient = new AIClient(API_KEY, useGPT);

        System.out.println("Chat started (type 'exit' to quit)");

        while (true) { // Loop to keep the chat running
            System.out.print("\nYou: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            // If user type 'file', call the ConvertToText class
            if (input.equalsIgnoreCase("file")) {
                ConvertToText.main(args);
                // Make the text file in the output folder as the input string for the AI
                try {
                    // Create a new file object and pass the only file in the output folder regardless of the file name
                    // This is because the file name is not known
                    File file = null; // File object to store the file in the output folder
                    try (
                        DirectoryStream<Path> stream = Files.newDirectoryStream(
                            // Get the only file in the output folder
                            Paths.get("output")
                        )
                    ) {
                        Iterator<Path> iterator = stream.iterator(); // Iterator to get the file
                        if (iterator.hasNext()) {
                            file = iterator.next().toFile(); // Get the file
                        } else {
                            System.out.println(
                                "No files found in output directory"
                            );
                            return;
                        }
                    }
                    InputStream inputStream = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    inputStream.read(buffer);
                    inputStream.close();
                    input = new String(buffer);

                    // Send the file content directly to AI
                    String response = aiClient.sendMessage(input);
                    System.out.println("\nAI: " + response);
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            } else {
                try {
                    String response = aiClient.sendMessage(input);
                    System.out.println("\nAI: " + response);
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
        scanner.close();
        System.out.println("Chat ended");
    }
}
