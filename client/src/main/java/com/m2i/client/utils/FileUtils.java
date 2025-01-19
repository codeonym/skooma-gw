package com.m2i.client.utils;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Writes a byte array to a file
     * @param file The file to write to
     * @param data The byte array to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        try {
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to file: " + file.getPath(), e);
            throw new IOException("Failed to write file: " + e.getMessage(), e);
        }
    }

    /**
     * Opens a file with the system's default application
     * @param file The file to open
     * @throws IOException If the file cannot be opened
     */
    public static void openFile(File file) throws IOException {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                    return;
                }
            }
            // Fallback for systems where Desktop is not supported
            openFileWithSystem(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening file: " + file.getPath(), e);
            throw new IOException("Failed to open file: " + e.getMessage(), e);
        }
    }

    /**
     * Opens a file using system-specific commands
     * @param file The file to open
     * @throws IOException If the file cannot be opened
     */
    private static void openFileWithSystem(File file) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder = new ProcessBuilder();

        if (os.contains("win")) {
            processBuilder.command("cmd", "/c", file.getAbsolutePath());
        } else if (os.contains("mac")) {
            processBuilder.command("open", file.getAbsolutePath());
        } else if (os.contains("nix") || os.contains("nux")) {
            processBuilder.command("xdg-open", file.getAbsolutePath());
        } else {
            throw new IOException("Unsupported operating system");
        }

        try {
            processBuilder.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error using system command to open file: " + file.getPath(), e);
            throw new IOException("Failed to open file with system command: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a temporary file that will be deleted when the JVM exits
     * @param prefix The prefix string for the temporary file
     * @param suffix The suffix string for the temporary file
     * @return The created temporary file
     * @throws IOException If the file cannot be created
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating temporary file", e);
            throw new IOException("Failed to create temporary file: " + e.getMessage(), e);
        }
    }

    /**
     * Safely deletes a file if it exists
     * @param file The file to delete
     * @throws IOException If the file cannot be deleted
     */
    public static void deleteIfExists(File file) throws IOException {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error deleting file: " + file.getPath(), e);
            throw new IOException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates necessary parent directories for a file if they don't exist
     * @param file The file whose parent directories need to be created
     * @throws IOException If the directories cannot be created
     */
    public static void createParentDirectories(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            try {
                Files.createDirectories(parentFile.toPath());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error creating parent directories: " + parentFile.getPath(), e);
                throw new IOException("Failed to create parent directories: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if a file exists and is readable
     * @param file The file to check
     * @return true if the file exists and is readable
     */
    public static boolean isFileReadable(File file) {
        return file != null && file.exists() && file.canRead();
    }

    /**
     * Checks if a file exists and is writable
     * @param file The file to check
     * @return true if the file exists and is writable
     */
    public static boolean isFileWritable(File file) {
        return file != null && (!file.exists() || file.canWrite());
    }
}