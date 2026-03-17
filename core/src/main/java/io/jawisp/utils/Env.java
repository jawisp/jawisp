package io.jawisp.utils;

import java.util.Map;
import java.util.Optional;

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
     * Retrieves the value of an environment variable, returning a fallback value if
     * the variable is not set.
     *
     * @param key      the name of the environment variable
     * @param fallback the value to return if the environment variable is not set
     * @return the value of the environment variable, or the fallback value if it is
     *         not set
     */
    public static String get(String key, String fallback) {
        String value = env.get(key);
        return value != null ? value : fallback;
    }

    /**
     * Retrieves the value of an environment variable as an {@link Optional}.
     *
     * @param key the name of the environment variable
     * @return an {@link Optional} containing the value of the environment variable
     *         if it is set, or an empty {@link Optional} if it is not set
     */
    public static Optional<String> get(String key) {
        String value = env.get(key);
        return Optional.ofNullable(value);
    }

    /**
     * Retrieves the value of an environment variable, throwing an exception if the
     * variable is not set.
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
     * Retrieves the value of an environment variable as an integer, returning a
     * fallback value if the variable is not set or is not a valid integer.
     *
     * @param key      the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or
     *                 is not a valid integer
     * @return the value of the environment variable as an integer, or the fallback
     *         value if it is not set or is not valid
     */
    public static int getInt(String key, int fallback) {
        String value = env.get(key);
        if (value == null) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Retrieves the value of an environment variable as an {@link Optional<Integer>}.
     *
     * @param key the name of the environment variable
     * @return an {@link Optional<Integer>} containing the value of the environment
     *         variable as an integer if it is set and valid, or an empty
     *         {@link Optional<Integer>} if it is not set or is not valid
     */
    public static Optional<Integer> getInt(String key) {
        String value = env.get(key);
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the value of an environment variable as an integer, throwing an
     * exception if the variable is not set or is not a valid integer.
     *
     * @param key the name of the environment variable
     * @return the value of the environment variable as an integer
     * @throws IllegalStateException if the environment variable is not set or is not
     *                               a valid integer
     */
    public static int getIntOrError(String key) {
        String value = env.get(key);
        if (value == null) {
            throw new IllegalStateException("Environment variable " + key + " is not set");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Environment variable " + key + " is not a valid integer");
        }
    }

    /**
     * Retrieves the value of an environment variable as a boolean, returning a
     * fallback value if the variable is not set or is not a valid boolean.
     *
     * @param key      the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or
     *                 is not a valid boolean
     * @return the value of the environment variable as a boolean, or the fallback
     *         value if it is not set or is not valid
     */
    public static boolean getBool(String key, boolean fallback) {
        String value = env.get(key);
        if (value == null) {
            return fallback;
        }

        // Boolean.parseBoolean() already handles null and empty strings, so no need for a try-catch block here
        return Boolean.parseBoolean(value);
    }

    /**
     * Retrieves the value of an environment variable as an {@link Optional<Boolean>}.
     *
     * @param key the name of the environment variable
     * @return an {@link Optional<Boolean>} containing the value of the environment
     *         variable as a boolean if it is set and valid, or an empty
     *         {@link Optional<Boolean>} if it is not set or is not valid
     */
    public static Optional<Boolean> getBool(String key) {
        String value = env.get(key);
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        // Boolean.parseBoolean() already handles null and empty strings, so no need for a try-catch block here
        return Optional.of(Boolean.parseBoolean(value.trim()));
    }

    /**
     * Retrieves the value of an environment variable as a float, returning a
     * fallback value if the variable is not set or is not a valid float.
     *
     * @param key      the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or
     *                 is not a valid float
     * @return the value of the environment variable as a float, or the fallback
     *         value if it is not set or is not valid
     */
    public static float getFloat(String key, float fallback) {
        String value = env.get(key);
        if (value == null) {
            return fallback;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Retrieves the value of an environment variable as an {@link Optional<Float>}.
     *
     * @param key the name of the environment variable
     * @return an {@link Optional<Float>} containing the value of the environment
     *         variable as a float if it is set and valid, or an empty
     *         {@link Optional<Float>} if it is not set or is not valid
     */
    public static Optional<Float> getFloat(String key) {
        String value = env.get(key);
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Float.parseFloat(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the value of an environment variable as a float, throwing an
     * exception if the variable is not set or is not a valid float.
     *
     * @param key the name of the environment variable
     * @return the value of the environment variable as a float
     * @throws IllegalStateException if the environment variable is not set or is not
     *                               a valid float
     */
    public static float getFloatOrError(String key) {
        String value = env.get(key);
        if (value == null) {
            throw new IllegalStateException("Environment variable " + key + " is not set");
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Environment variable " + key + " is not a valid float");
        }
    }

    /**
     * Retrieves the value of an environment variable as a double, returning a
     * fallback value if the variable is not set or is not a valid double.
     *
     * @param key      the name of the environment variable
     * @param fallback the value to return if the environment variable is not set or
     *                 is not a valid double
     * @return the value of the environment variable as a double, or the fallback
     *         value if it is not set or is not valid
     */
    public static double getDouble(String key, double fallback) {
        String value = env.get(key);
        if (value == null) {
            return fallback;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Retrieves the value of an environment variable as an {@link Optional<Double>}.
     *
     * @param key the name of the environment variable
     * @return an {@link Optional<Double>} containing the value of the environment
     *         variable as a double if it is set and valid, or an empty
     *         {@link Optional<Double>} if it is not set or is not valid
     */
    public static Optional<Double> getDouble(String key) {
        String value = env.get(key);
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the value of an environment variable as a double, throwing an
     * exception if the variable is not set or is not a valid double.
     *
     * @param key the name of the environment variable
     * @return the value of the environment variable as a double
     * @throws IllegalStateException if the environment variable is not set or is not
     *                               a valid double
     */
    public static double getDoubleOrError(String key) {
        String value = env.get(key);
        if (value == null) {
            throw new IllegalStateException("Environment variable " + key + " is not set");
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Environment variable " + key + " is not a valid double");
        }
    }
}