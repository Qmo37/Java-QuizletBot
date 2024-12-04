package com.aichat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class FlashcardPanel extends JPanel {

    private List<Flashcard> flashcards;
    private int currentIndex;
    private JLabel cardLabel;
    private JPanel cardPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel counterLabel;
    private JButton backToChatButton; // Add this
    private CardLayout parentCardLayout; // Add this
    private JPanel parentContentPanel; // Add this

    public FlashcardPanel() {
        flashcards = new ArrayList<>();
        currentIndex = 0;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Initialize all components
        initializeComponents();

        // Add components to the panel
        layoutComponents();

        // Create card panel
        cardPanel = new JPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create card label
        cardLabel = new JLabel("", SwingConstants.CENTER);
        cardLabel.setFont(new Font("Noto Sans CJK TC", Font.PLAIN, 18));
        cardPanel.add(cardLabel, BorderLayout.CENTER);

        // Create navigation buttons
        JPanel navigationPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        counterLabel = new JLabel("0/0");

        // Create back button
        backToChatButton = new JButton("Back to Chat");
        styleButton(backToChatButton);

        // Add back button to navigation panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backToChatButton);

        styleButton(prevButton);
        styleButton(nextButton);

        navigationPanel.add(prevButton);
        navigationPanel.add(counterLabel);
        navigationPanel.add(nextButton);

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);

        // Add listeners
        setupListeners();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Noto Sans CJK TC", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }

    private void setupListeners() {
        cardPanel.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!flashcards.isEmpty()) {
                        Flashcard current = flashcards.get(currentIndex);
                        current.toggleAnswer();
                        updateCard();
                    }
                }
            }
        );

        prevButton.addActionListener(e -> showPreviousCard());
        nextButton.addActionListener(e -> showNextCard());

        // Add keyboard navigation
        this.setFocusable(true);
        this.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            showPreviousCard();
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            showNextCard();
                        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                            if (!flashcards.isEmpty()) {
                                Flashcard current = flashcards.get(
                                    currentIndex
                                );
                                current.toggleAnswer();
                                updateCard();
                            }
                        }
                    }
                }
            );
    }

    private void initializeComponents() {
        // Initialize back button
        backToChatButton = new JButton("Back to Chat");
        styleButton(backToChatButton);

        // Initialize card panel and label
        cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cardLabel = new JLabel(
            "No flashcards available",
            SwingConstants.CENTER
        );
        cardLabel.setFont(new Font("Noto Sans CJK TC", Font.PLAIN, 18));

        // Initialize navigation buttons
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        counterLabel = new JLabel("0/0");

        styleButton(prevButton);
        styleButton(nextButton);
    }

    private void layoutComponents() {
        // Top panel with back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(backToChatButton);

        // Card panel
        cardPanel.add(cardLabel, BorderLayout.CENTER);

        // Navigation panel
        JPanel navigationPanel = new JPanel(new FlowLayout());
        navigationPanel.setBackground(Color.WHITE);
        navigationPanel.add(prevButton);
        navigationPanel.add(counterLabel);
        navigationPanel.add(nextButton);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    // Add method to set parent components for navigation
    public void setParentComponents(
        CardLayout cardLayout,
        JPanel contentPanel
    ) {
        this.parentCardLayout = cardLayout;
        this.parentContentPanel = contentPanel;
        backToChatButton.addActionListener(e ->
            parentCardLayout.show(parentContentPanel, "chat")
        );
    }

    // Add method to load flashcards directly from text
    public void loadFlashcardsFromText(String text) {
        flashcards.clear();
        String[] lines = text.split("\n");
        String question = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Q:")) {
                question = line.substring(2).trim();
            } else if (line.startsWith("A:") && question != null) {
                String answer = line.substring(2).trim();
                flashcards.add(new Flashcard(question, answer));
                question = null;
            }
        }

        currentIndex = 0;
        updateCard();
    }

    public void loadFlashcardsFromFile(File file) {
        flashcards.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String question = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Q:")) {
                    question = line.substring(2).trim();
                } else if (line.startsWith("A:") && question != null) {
                    String answer = line.substring(2).trim();
                    flashcards.add(new Flashcard(question, answer));
                    question = null;
                }
            }
            currentIndex = 0;
            updateCard();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error loading flashcards: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showPreviousCard() {
        if (!flashcards.isEmpty()) {
            currentIndex =
                (currentIndex - 1 + flashcards.size()) % flashcards.size();
            flashcards.get(currentIndex).isShowingAnswer();
            updateCard();
        }
    }

    private void showNextCard() {
        if (!flashcards.isEmpty()) {
            currentIndex = (currentIndex + 1) % flashcards.size();
            flashcards.get(currentIndex).isShowingAnswer();
            updateCard();
        }
    }

    public void addFlashcard(String question, String answer) {
        // Add the Q&A pair to your flashcards collection
        // Implementation depends on how you're storing flashcards
        flashcards.add(new Flashcard(question, answer));
        currentIndex = flashcards.size() - 1;
        updateCard();
    }

    public void clearFlashcards() {
        // Clear existing flashcards before loading new ones
        flashcards.clear();
        currentIndex = 0;
        updateCard();
    }

    private void updateCard() {
        if (flashcards.isEmpty()) {
            cardLabel.setText("No flashcards available");
            counterLabel.setText("0/0");
            return;
        }

        Flashcard current = flashcards.get(currentIndex);
        String text = current.isShowingAnswer()
            ? current.getAnswer()
            : current.getQuestion();
        cardLabel.setText(
            "<html><div style='text-align: center; padding: 20px;'>" +
            text +
            "</div></html>"
        );
        counterLabel.setText((currentIndex + 1) + "/" + flashcards.size());
    }
}
