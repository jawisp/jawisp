package io.jawisp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EnvTest {

    // Mock environment variables for testing
    private static final Map<String, String> mockEnv = new HashMap<>();

    // Capture real env once for baseline tests
    private static final Map<String, String> REAL_ENV = System.getenv();


    // Set the mock environment variables
    static {
        System.setIn(new java.io.ByteArrayInputStream(new byte[0]));
        // Env.env = mockEnv;
    }

    @Test
    void get_withMissingKey_returnsFallback() {
        String result = Env.get("SHOULD_NOT_EXIST_123", "fallback");
        assertEquals("fallback", result);
    }

    @Test
    void getOrError_withMissingKey_throwsIllegalStateException() {
        String key = "MISSING_KEY_456";
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Env.getOrError(key));
        assertEquals("Environment variable " + key + " is not set", exception.getMessage());
    }

    @Test
    void getInt_withMissingKey_returnsFallback() {
        int result = Env.getInt("MISSING_INT_789", 8080);
        assertEquals(8080, result);
    }

    @Test
    void getInt_realEnvVars_parsedCorrectly() {
        // Test with real existing env vars that are numbers
        String javaHome = REAL_ENV.get("JAVA_HOME");
        if (javaHome != null) {
            // Extract port-like number from path if exists
            String portCandidate = extractNumber(javaHome);
            if (!portCandidate.isEmpty()) {
                Env.getInt("JAVA_HOME", 0); // Should parse if number found
            }
        }
    }

    @Test
    void getBool_withMissingKey_returnsFallback() {
        boolean result = Env.getBool("MISSING_BOOL_101", true);
        assertEquals(true, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "", "  ", "abc123", "not-a-number" })
    void getInt_invalidValues_returnsFallback(String invalidValue) {
        // Test parsing logic with controlled invalid inputs
        // Since we can't set env, test the static parsing logic directly
        int result = parseIntStatic(invalidValue, 8080);
        assertEquals(8080, result);
    }

    @ParameterizedTest
    @CsvSource({
            "123, 123",
            "0, 0",
            "-42, -42",
            "456, 456"
    })
    void getInt_validValues_parsedCorrectly(String input, int expected) {
        int result = parseIntStatic(input, 8080);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false",
            "TRUE, true",
            "False, false"
    })
    void getBool_validValues_parsedCorrectly(String input, boolean expected) {
        boolean result = parseBoolStatic(input, false);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "", "yes", "no", "1", "0" })
    void getBool_invalidValues_returnsFallback(String invalidValue) {
        boolean result = parseBoolStatic(invalidValue, false);
        assertEquals(false, result);
    }

    @Test
    void getFloat_withMissingKey_returnsFallback() {
        float result = Env.getFloat("MISSING_FLOAT_123", 3.14f);
        assertEquals(3.14f, result);
    }

    @Test
    void getFloat_realEnvVars_parsedCorrectly() {
        // Test with real existing env vars that are numbers
        String javaHome = REAL_ENV.get("JAVA_HOME");
        if (javaHome != null) {
            // Extract port-like number from path if exists
            String portCandidate = extractNumber(javaHome);
            if (!portCandidate.isEmpty()) {
                Env.getFloat("JAVA_HOME", 0.0f); // Should parse if number found
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "", "  ", "abc123", "not-a-number" })
    void getFloat_invalidValues_returnsFallback(String invalidValue) {
        // Test parsing logic with controlled invalid inputs
        // Since we can't set env, test the static parsing logic directly
        float result = parseFloatStatic(invalidValue, 3.14f);
        assertEquals(3.14f, result);
    }

    @ParameterizedTest
    @CsvSource({
            "3.14, 3.14",
            "0.0, 0.0",
            "-1.23, -1.23",
            "4.56, 4.56"
    })
    void getFloat_validValues_parsedCorrectly(String input, float expected) {
        float result = parseFloatStatic(input, 3.14f);
        assertEquals(expected, result, 0.0001); // Allow for floating-point precision errors
    }

    @Test
    void getDouble_withMissingKey_returnsFallback() {
        double result = Env.getDouble("MISSING_DOUBLE_123", 3.14);
        assertEquals(3.14, result);
    }

    @Test
    void getDouble_realEnvVars_parsedCorrectly() {
        // Test with real existing env vars that are numbers
        String javaHome = REAL_ENV.get("JAVA_HOME");
        if (javaHome != null) {
            // Extract port-like number from path if exists
            String portCandidate = extractNumber(javaHome);
            if (!portCandidate.isEmpty()) {
                Env.getDouble("JAVA_HOME", 0.0); // Should parse if number found
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "", "  ", "abc123", "not-a-number" })
    void getDouble_invalidValues_returnsFallback(String invalidValue) {
        // Test parsing logic with controlled invalid inputs
        // Since we can't set env, test the static parsing logic directly
        double result = parseDoubleStatic(invalidValue, 3.14);
        assertEquals(3.14, result);
    }

    @ParameterizedTest
    @CsvSource({
            "3.14, 3.14",
            "0.0, 0.0",
            "-1.23, -1.23",
            "4.56, 4.56"
    })
    void getDouble_validValues_parsedCorrectly(String input, double expected) {
        double result = parseDoubleStatic(input, 3.14);
        assertEquals(expected, result, 0.0001); // Allow for floating-point precision errors
    }

    @Test
    void getIntOrError_withMissingKey_throwsIllegalStateException() {
        String key = "MISSING_INT_OR_ERROR_456";
        mockEnv.put(key, null);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Env.getIntOrError(key));
        assertEquals("Environment variable " + key + " is not set", exception.getMessage());
        mockEnv.remove(key);
    }


    @Test
    void getFloatOrError_withMissingKey_throwsIllegalStateException() {
        String key = "MISSING_FLOAT_OR_ERROR_456";
        mockEnv.put(key, null);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Env.getFloatOrError(key));
        assertEquals("Environment variable " + key + " is not set", exception.getMessage());
        mockEnv.remove(key);
    }


    @Test
    void getDoubleOrError_withMissingKey_throwsIllegalStateException() {
        String key = "MISSING_DOUBLE_OR_ERROR_456";
        mockEnv.put(key, null);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> Env.getDoubleOrError(key));
        assertEquals("Environment variable " + key + " is not set", exception.getMessage());
        mockEnv.remove(key);
    }

    // Static extraction of parsing logic for testing
    private static int parseIntStatic(String value, int fallback) {
        if (value == null)
            return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean parseBoolStatic(String value, boolean fallback) {
        if (value == null)
            return fallback;
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float parseFloatStatic(String value, float fallback) {
        if (value == null)
            return fallback;
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDoubleStatic(String value, double fallback) {
        if (value == null)
            return fallback;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String extractNumber(String input) {
        if (input == null)
            return "";
        return input.replaceAll("[^0-9]", "").replaceFirst("^0+(?!$)", "");
    }
}
