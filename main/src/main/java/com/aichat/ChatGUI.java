package com.aichat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class ChatGUI extends JFrame {

    private final JTextArea chatArea; // Chat area to display messages
    private final JTextField inputField; // Input field for user messages
    private final JButton sendButton; // Button to send messages
    private final JButton fileButton; // Button to upload a file
    private final AIClient aiClient;
    private final ExecutorService executorService; // Executor service for async tasks, async tasks are used to prevent blocking the UI
    private final JProgressBar progressBar; // Progress bar to show processing state
    private final Timer progressTimer;
    private final JPanel glassPane; // Glass pane to overlay the UI during processing
    private JButton switchToFlashcardsButton;
    private JButton createFlashcardsFromChatButton;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private FlashcardPanel flashcardPanel;
    private String input;

    public ChatGUI() {
        executorService = Executors.newCachedThreadPool(); // Create a cached thread pool for async tasks
        String API_KEY = System.getenv("ANTHROPIC_API_KEY");
        if (API_KEY == null || API_KEY.isEmpty()) {
            showError("ANTHROPIC_API_KEY environment variable is not set");
            System.exit(1);
        }
        aiClient = new AIClient(API_KEY, false);

        // Setup main frame
        setTitle("AI Quiz Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit the application when the window is closed
        setSize(800, 600); // Set the window size
        setLocationRelativeTo(null);

        // Initialize all components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);
        chatArea.setMargin(new Insets(20, 20, 20, 20));
        chatArea.setFont(new Font("Noto Sans CJK TC", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        );

        inputField = new JTextField();
        inputField.setFont(new Font("Noto Sans CJK TC", Font.PLAIN, 16));

        // Initialize buttons
        sendButton = createStyledButton("Send");
        fileButton = createStyledButton("Upload File");
        createFlashcardsFromChatButton = createStyledButton(
            "Create Flashcards from Chat"
        );
        switchToFlashcardsButton = createStyledButton("Flashcards");

        // Initialize progress components
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));

        progressTimer = new Timer(50, e -> progressBar.repaint());

        // Initialize glass pane
        glassPane = new JPanel(new GridBagLayout());
        glassPane.setOpaque(false);
        JLabel processingLabel = new JLabel("Processing...");
        processingLabel.setForeground(Color.BLACK);
        processingLabel.setFont(new Font("Noto Sans CJK TC", Font.BOLD, 16));
        glassPane.add(processingLabel);
        setGlassPane(glassPane);

        // Initialize flashcard components
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        flashcardPanel = new FlashcardPanel();

        // Layout setup
        setupLayout(scrollPane);
        setupListeners(); // Setup action listeners for buttons, text field, and window events

        // Initial message
        appendToChatArea("Chat started. Type your message or upload a file.\n");
    }

    private JButton createStyledButton(String text) { // Create a styled button with the specified text
        JButton button = new JButton(text);
        button.setFont(new Font("Noto Sans CJK TC", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(
            // Add mouse listeners to change the background color on hover
            new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(51, 102, 153));
                }

                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(70, 130, 180));
                }
            }
        );

        return button;
    }

    private void setupLayout(JScrollPane scrollPane) { // Setup the layout of the chat interface
        setLayout(new BorderLayout());

        // Create contentPanel with CardLayout first
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Create mainPanel for chat interface
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Chat area panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(progressBar, BorderLayout.SOUTH);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);

        // Add all buttons here
        JButton loadFlashcardsButton = createStyledButton("Load Flashcards");
        loadFlashcardsButton.addActionListener(e -> loadFlashcards());
        buttonPanel.add(createFlashcardsFromChatButton);
        buttonPanel.add(loadFlashcardsButton);
        buttonPanel.add(switchToFlashcardsButton);
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Add panels to contentPanel with CardLayout
        contentPanel.add(mainPanel, "chat");
        contentPanel.add(flashcardPanel, "flashcards");

        // Add contentPanel to frame
        add(contentPanel);
    }

    private void setupListeners() { // Setup action listeners for buttons, text field, and window events
        sendButton.addActionListener(e -> processInput()); // Add an action listener to the send button
        fileButton.addActionListener(e -> selectAndProcessFile()); // Add an action listener to the file button
        switchToFlashcardsButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "flashcards");
        });
        createFlashcardsFromChatButton.addActionListener(e ->
            createFlashcardsFromLastResponse()
        );

        inputField.addKeyListener(
            // Add a key listener to the input field to process the input on Enter key press
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        processInput();
                    }
                }
            }
        );

        addWindowListener(
            // Add a window listener to shutdown the executor service and stop the progress timer on window close
            new WindowAdapter() {
                @Override // Override the windowClosing method, which is called when the window is closing.
                public void windowClosing(WindowEvent e) {
                    executorService.shutdown();
                    progressTimer.stop();
                }
            }
        );
    }

    private void processInput() { // Process the user input
        input = inputField.getText().trim(); // Get the text from the input field and trim leading and trailing whitespace
        if (input.isEmpty()) return;

        inputField.setText(""); // Clear the input field
        showProgress(true); // Show the progress bar
        appendToChatArea("You: " + input + "\n");

        CompletableFuture.supplyAsync(
            // Create a CompletableFuture to process the input asynchronously
            () -> { // Supply a function that sends the input to the AI service
                try {
                    return aiClient.sendMessage(input); // Send the input to the AI service and get the response
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            },
            executorService // Use the executor service to run the task asynchronously
        )
            .thenAcceptAsync(
                // Handle the response asynchronously
                response -> { // Accept the response and append it to the chat area
                    appendToChatArea("AI: " + response + "\n\n"); // Append the response to the chat area
                    showProgress(false); // Hide the progress bar
                },
                SwingUtilities::invokeLater // Use SwingUtilities.invokeLater to update the UI on the event dispatch thread
            )
            .exceptionally(e -> {
                handleError(e.getCause());
                return null;
            });
    }

    private void selectAndProcessFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            processFile(selectedFile);
        }
    }

    // processFile is a method that use ConvertToText class to convert the file to text, and then process the text to String input, sends it to the AI service, and displays the response in the chat area.
    private void processFile(File file) {
        showProgress(true);
        appendToChatArea("Processing file: " + file.getName() + "...\n");

        CompletableFuture.supplyAsync(
            () -> {
                try {
                    File outputDir = new File("output"); // Create a new file object for the output directory
                    File selectedFile = file; // Get the selected file
                    ConvertToText.clearOutputDirectory(outputDir); // Clear the output directory
                    ConvertToText.processFile(selectedFile, outputDir); // Process the selected file
                    File[] outputFiles = new File("output").listFiles(); // Get the list of output files
                    if (outputFiles != null && outputFiles.length > 0) { // Check if the output files exist
                        File outputFile = outputFiles[0]; // Get the first output file
                        InputStream inputStream = new FileInputStream(
                            outputFile
                        ); // Create an input stream to read the output file
                        byte[] buffer = Files.readAllBytes(outputFile.toPath()); // Read the output file into a byte array
                        inputStream.read(buffer);
                        inputStream.close();
                        input = new String(buffer); // Convert the byte array to a string
                        return aiClient.sendMessage(input);
                        // Send the String input to AI

                    } else {
                        throw new IOException("No output file found");
                    }
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            },
            executorService
        )
            .thenAcceptAsync(
                // Handle the response asynchronously
                text -> {
                    appendToChatArea("You: [File: " + file.getName() + "]\n");
                    appendToChatArea("AI: " + text + "\n\n");
                    showProgress(false);
                },
                SwingUtilities::invokeLater
            )
            .exceptionally(e -> {
                handleError(e.getCause());
                return null;
            });
    }

    private void showProgress(boolean show) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(show);
            glassPane.setVisible(show);
            if (show) {
                progressTimer.start();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            } else {
                progressTimer.stop();
                setCursor(Cursor.getDefaultCursor());
            }
            // Keep components enabled but show processing state
            sendButton.setEnabled(!show);
            fileButton.setEnabled(!show);
            inputField.setEnabled(!show);
        });
    }

    private void appendToChatArea(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void loadFlashcards() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            flashcardPanel.loadFlashcardsFromFile(selectedFile);
        }
    }

    private void createFlashcardsFromLastResponse() {
        String chatText = chatArea.getText();
        String[] messages = chatText.split("\n");
        StringBuilder flashcardText = new StringBuilder();

        for (String message : messages) {
            if (message.startsWith("AI:")) {
                String aiResponse = message.substring(3).trim();
                // Try to find question-answer pairs in the AI response
                String[] lines = aiResponse.split("\n");
                for (String line : lines) {
                    if (line.startsWith("Q:") || line.startsWith("A:")) {
                        flashcardText.append(line).append("\n");
                    }
                }
            }
        }

        if (flashcardText.length() > 0) {
            flashcardPanel.loadFlashcardsFromText(flashcardText.toString());
            cardLayout.show(contentPanel, "flashcards");
        } else {
            JOptionPane.showMessageDialog(
                this,
                "No flashcard format content found in the chat.\n" +
                "Make sure the AI response contains lines starting with 'Q:' and 'A:'",
                "No Flashcards Found",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void handleError(Throwable e) {
        SwingUtilities.invokeLater(() -> {
            showError(e.getMessage());
            showProgress(false);
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ChatGUI().setVisible(true);
        });
    }
}
