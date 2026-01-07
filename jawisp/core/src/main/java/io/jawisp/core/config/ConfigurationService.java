package io.jawisp.core.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private static ConfigurationService instance;
    private final Map<String, String> configMap = new HashMap<>();
    private static final String CONFIG_FILE = "application.yaml";
    // private static final String USER_CONFIG_FILE = ".application.yaml";
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\{([^:{}]+)(?::([^{}]*))?\\}");

    /**
     * Private constructor that initializes the configuration service by loading
     * the configuration file from the classpath.
     * 
     * @throws IOException if the configuration file cannot be found or read
     */
    private ConfigurationService() {
        loadConfig();
    }

    /**
     * Gets the singleton instance of the ConfigurationService.
     * 
     * This method ensures that only one instance of the configuration service
     * is created and provides thread-safe access to the configuration data.
     * 
     * @return The singleton instance of ConfigurationService
     */
    public static synchronized ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }
        return instance;
    }

    /**
     * Loads configuration data from the YAML file located in the classpath.
     * 
     * This method reads the configuration file and parses it to build a flat
     * key-value map. It handles nested structures by using dot notation for keys
     * and properly manages indentation to determine the hierarchy level of each
     * configuration property.
     */
    public void loadConfig() {
        // Clear existing config
        configMap.clear();

        // Load default configuration from classpath
        loadDefaultConfig();

        // Load user configuration and override defaults
        // loadUserConfig();

        if (logger.isDebugEnabled()) {
            for (String key : configMap.keySet()) {
                String value = configMap.get(key);
                logger.debug("Configuration Key: {}, Value: {}", key, value);
            }
        }
    }

    /**
     * Loads the default configuration from classpath
     */
    private void loadDefaultConfig() {
        getFileContent(CONFIG_FILE)
            .ifPresent(content -> parse(content));
    }

    /**
     * Loads user configuration from ~/.application.yaml if it exists
     * and overrides default values
     */
    // private void loadUserConfig() {
    //     try {
    //         // Get user home directory
    //         String userHome = System.getProperty("user.home");
    //         if (userHome == null) {
    //             return;
    //         }

    //         Path userConfigPath = Paths.get(userHome, USER_CONFIG_FILE);

    //         // Check if user config file exists
    //         if (Files.exists(userConfigPath) && Files.isReadable(userConfigPath)) {
    //             // Read user config content
    //             String fileContent = new String(Files.readAllBytes(userConfigPath));
    //             parse(fileContent);
    //         }
    //     } catch (IOException e) {
    //         // Log error but don't fail the application
    //         logger.error("Warning: Could not load user configuration file: {}", e.getMessage());
    //     }
    // }

    /**
     * Processes environment variables in configuration values
     * 
     * Looks for patterns like {ENV_VAR_NAME:DEFAULT_VALUE} and replaces them
     * with environment variable values or default values if env var is not set
     * 
     * @param value The configuration value that may contain environment variable
     *              placeholders
     * @return The processed string with environment variables resolved
     */

    private String processEnvironmentVariables(String value) {
        // Return early if input is null or does not contain any variable pattern
        if (value == null || !value.contains("{")) {
            return value;
        }

        var result = new StringBuffer();
        var matcher = ENV_VAR_PATTERN.matcher(value);
        boolean foundMatch = false;

        while (matcher.find()) {
            foundMatch = true;
            // Group 1: environment variable name
            var envVarName = matcher.group(1);
            // Group 2: optional default value (may be null)
            var defaultValue = matcher.group(2);

            // Retrieve environment variable value
            var envValue = System.getenv(envVarName);

            // Use the environment variable if set and not empty; otherwise, default value
            var replacement = (envValue != null && !envValue.isEmpty()) ? envValue
                    : (defaultValue != null ? defaultValue : "");

            // Escape replacement string to handle special characters correctly in regex
            // replacement
            var escapedReplacement = Matcher.quoteReplacement(replacement);

            matcher.appendReplacement(result, escapedReplacement);
        }

        // Append any remaining text after the last match
        if (foundMatch) {
            matcher.appendTail(result);
            return result.toString();
        }

        // Return original value if no pattern matched
        return value;
    }

    /**
     * Calculates the indentation level of a line by comparing its length with
     * the length after stripping leading whitespace.
     * 
     * This method helps determine the hierarchy level of configuration properties
     * in nested YAML structures.
     * 
     * @param line The line of text to analyze for indentation
     * @return The number of leading whitespace characters (indentation level)
     */
    private int getIndentLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ')
                count++;
            else
                break;
        }
        return count / 2; // Assuming 2 spaces per indent level
    }

    /**
     * Removes surrounding quotes from a string value if present.
     * 
     * This method handles both single and double quotes that may be present
     * in YAML configuration values.
     * 
     * @param value The string value that may contain surrounding quotes
     * @return The string value with quotes removed, or the original string if no
     *         quotes found
     */
    private String removeQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Retrieves a configuration value as a String, returning a default value if the
     * key is not found.
     * 
     * @param key The configuration key to look up
     * @return The configuration value as a String, or the default value if not
     *         found
     */
    public String getValue(String key) {
        return configMap.getOrDefault(key, "");
    }

    public String getValue(String key, String value) {
        return configMap.getOrDefault(key, value);
    }

    /**
     * Retrieves a configuration value as an Integer, returning a default value if
     * the key is not found
     * or cannot be parsed as an integer.
     * 
     * @param key The configuration key to look up
     * @return The configuration value as an Integer, or the default value if not
     *         found or parsing fails
     */
    public int getInt(String key) {
        var val = configMap.get(key);
        if (val == null) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getInt(String key, int value) {
        var val = configMap.get(key);
        if (val == null) {
            return value;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Retrieves a configuration value as a Boolean, returning a default value if
     * the key is not found
     * or cannot be parsed as a boolean.
     * 
     * @param key          The configuration key to look up
     * @param defaultValue The default value to return if the key is not found or
     *                     cannot be parsed
     * @return The configuration value as a Boolean, or the default value if not
     *         found or parsing fails
     */
    public boolean getBoolean(String key) {
        var val = configMap.get(key);
        if (val == null) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    /**
     * Gets a copy of all configuration values.
     * 
     * @return A new HashMap containing all configuration key-value pairs
     */
    public Map<String, String> getAll() {
        return new HashMap<>(configMap);
    }

    public Map<String, String> parse(String fileContent) {
        String[] lines = fileContent.split("\\r?\\n");
        Deque<String> keyStack = new ArrayDeque<>();
        int previousIndentLevel = 0;

        for (var line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }

            int indentLevel = getIndentLevel(line);
            line = line.trim();

            if (!line.contains(":")) {
                continue;
            }

            String[] parts = line.split(":", 2);
            var key = parts[0].trim();
            var value = parts[1].trim();

            // Pop keys when current indent is less than the previous line's indent
            while (indentLevel < previousIndentLevel && !keyStack.isEmpty()) {
                keyStack.pop();
                previousIndentLevel--;
            }

            if (value.isEmpty()) {
                // Nested key, push to stack
                keyStack.push(key);
                previousIndentLevel = indentLevel + 1;
            } else {
                // Compose full key path from stack plus current key (reversed stack)
                var fullKeyParts = new ArrayList<>(keyStack);
                Collections.reverse(fullKeyParts);
                fullKeyParts.add(key);
                var fullKey = String.join(".", fullKeyParts);

                String processedValue = processEnvironmentVariables(value);
                var unquotedValue = removeQuotes(processedValue);
                configMap.put(fullKey, unquotedValue);
            }
        }

        return configMap;
    }

    public Optional<String> getFileContent(String filePath) {
        // Load file content if not cached
        var inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            logger.debug("File not found: {}", filePath);
            return Optional.empty();
        }

        logger.debug("Configuration file found: {}", filePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            var stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            var content = stringBuilder.toString();

            return Optional.of(content);
        } catch (IOException e) {
            logger.error("Failed to read file: {}, {}", filePath, e);
        }
        return Optional.empty();
    }

}
