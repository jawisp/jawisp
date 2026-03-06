package io.jawisp.utils;

import java.util.Map;

/**
 * Utility class for accessing environment variables.
 * This class should not be instantiated as it contains only static methods.
 * 
 * @author Taras Chornyi
 * @since 1.0.9
 */
public final class Env {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Env() {
        throw new AssertionError("Utility class");
    }

    /**
     * The map holding all environment variables.
     */
    private static final Map<String, String> env = System.getenv();

    /**
     * Retrieves the value of an environment variable, returning a fallback value if the variable is not set.
     *
     * @param key the name of the environment variable
     * @param fallback the value to return if the environment variable is not set
     * @return the value of the environment variable, or the fallback value if it is not set
     */
    public static String get(String key, String fallback) {
        String value = env.get(key);
        return value != null ? value : fallback;
    }

    /**
     * Retrieves the value of an environment variable, throwing an exception if the variable is not set.
     *
     * @param key the name of the environment variable
     * @return the value of the environment variable
     * @throws IllegalStateException if the environment variable is not set
     */
    public static String getOrError(String key) {
        String value = env.get(key);
        if (value == null) {
            throw new IllegalStateException("Environment variable " + key + " is not set");
        }
        return value;
    }

    /**
     * Retrieves the value of an environment variable as an integer, returning a fallback value if the variable is not set
     * or is not a valid integer.
     *
     * @param key the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or is not a valid integer
     * @return the value of the environment variable as an integer, or the fallback value if it is not set or is not valid
     */
    public static int getAsInt(String key, int fallback) {
        String value = env.get(key);
        if (value == null) return fallback;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Retrieves the value of an environment variable as a boolean, returning a fallback value if the variable is not set
     * or is not a valid boolean.
     *
     * @param key the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or is not a valid boolean
     * @return the value of the environment variable as a boolean, or the fallback value if it is not set or is not valid
     */
    public static boolean getAsBool(String key, boolean fallback) {
        String value = env.get(key);
        if (value == null) return fallback;

        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}