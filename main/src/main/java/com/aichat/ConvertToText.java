package com.aichat;

import java.io.*;
import java.util.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class ConvertToText {

    private static final String INPUT_FOLDER = "input";
    private static final String OUTPUT_FOLDER = "output";
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(
        Arrays.asList("doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx")
    );

    public static void main(String[] args) {
        try {
            File inputDir = initializeInputDirectory();
            File outputDir = initializeOutputDirectory();
            File[] files = getInputFiles(inputDir);

            displayAvailableFiles(files);
            String selectedFileName = getUserSelection();

            clearOutputDirectory(outputDir);
            processSelectedFile(files, selectedFileName, outputDir);
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize and verify the input directory
     * @return File object representing the input directory
     * @throws IOException if directory doesn't exist or is invalid
     */
    private static File initializeInputDirectory() throws IOException {
        File inputDir = new File(INPUT_FOLDER);
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IOException(
                "Input folder does not exist or is not a directory"
            );
        }
        return inputDir;
    }

    /**
     * Initialize and create the output directory if it doesn't exist
     * @return File object representing the output directory
     */
    private static File initializeOutputDirectory() {
        File outputDir = new File(OUTPUT_FOLDER);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return outputDir;
    }

    /**
     * Get list of files from input directory
     * @param inputDir Input directory
     * @return Array of files in the input directory
     * @throws IOException if no files are found
     */
    private static File[] getInputFiles(File inputDir) throws IOException {
        File[] files = inputDir.listFiles();
        if (files == null || files.length == 0) {
            throw new IOException("No files found in the input folder");
        }
        return files;
    }

    /**
     * Display all available files in the input directory
     * @param files Array of files to display
     */
    private static void displayAvailableFiles(File[] files) {
        System.out.println("Files in input folder:");
        for (File file : files) {
            if (file.isFile()) {
                System.out.println(file.getName());
            }
        }
    }

    /**
     * Get file selection from user
     * @return Selected file name
     */
    private static String getUserSelection() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select the file to input (type 'exit' to quit)");
        return scanner.nextLine();
    }

    /**
     * Clear all files in the output directory
     * @param outputDir Output directory to clear
     */
    public static void clearOutputDirectory(File outputDir) {
        File[] outputFiles = outputDir.listFiles();
        if (outputFiles != null) {
            for (File file : outputFiles) {
                file.delete();
            }
        }
    }

    /**
     * Process the selected file and convert it to text
     * @param files Array of available files
     * @param selectedFileName Name of the selected file
     * @param outputDir Output directory for converted file
     */
    public static void processSelectedFile(
        File[] files,
        String selectedFileName,
        File outputDir
    ) {
        for (File file : files) {
            if (file.isFile() && file.getName().equals(selectedFileName)) {
                processFile(file, outputDir);
                return;
            }
        }
        System.err.println("Selected file not found: " + selectedFileName);
    }

    /**
     * Process individual file and convert it to text
     * @param file File to process
     * @param outputDir Output directory for converted file
     */
    public static void processFile(File file, File outputDir) {
        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);

        if (SUPPORTED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            try {
                String text = extractText(file);
                text = skipRepeatedSentence(text);

                String outputFileName =
                    fileName.substring(0, fileName.lastIndexOf('.')) + ".txt";
                File outputFile = new File(outputDir, outputFileName);

                writeTextToFile(text, outputFile);
                System.out.println(
                    "Converted " + fileName + " to " + outputFileName
                );
            } catch (IOException | TikaException | SAXException e) {
                System.err.println("Error processing file: " + fileName);
                e.printStackTrace();
            }
        } else {
            System.err.println("Unsupported file format: " + fileName);
        }
    }

    /**
     * Get file extension from filename
     * @param fileName Name of the file
     * @return File extension
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * Extract text content from file using Apache Tika
     * @param file File to extract text from
     * @return Extracted text content
     */
    public static String extractText(File file)
        throws IOException, TikaException, SAXException {
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata, context);
            return handler.toString();
        }
    }

    /**
     * Write text content to output file
     * @param text Text content to write
     * @param file Output file
     */
    public static void writeTextToFile(String text, File file)
        throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            stream.write(text.getBytes());
        }
    }

    /**
     * Remove repeated sentences from text content
     * @param text Input text content
     * @return Processed text without repetitions
     */
    public static String skipRepeatedSentence(String text) {
        String[] sentences = text.split("[.\\n]");
        StringBuilder sb = new StringBuilder();
        Set<String> addedSentences = new HashSet<>();

        for (String sentence : sentences) {
            String cleanSentence = sentence.trim();

            if (cleanSentence.length() <= 20 || cleanSentence.isEmpty()) {
                continue;
            }

            if (addedSentences.add(cleanSentence)) {
                if (sb.length() > 0) {
                    sb.append(". ");
                }
                sb.append(cleanSentence);
            }
        }

        if (sb.length() > 0) {
            sb.append(".");
        }

        return sb.toString();
    }
}
