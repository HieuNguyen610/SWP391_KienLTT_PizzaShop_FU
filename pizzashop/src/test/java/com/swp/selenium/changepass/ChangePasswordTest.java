package com.swp.selenium.changepass;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium tests for Change Password feature (profile page).
 * Pre-conditions:
 *  - Application running locally on http://localhost:8080
 *  - TEST_EMAIL user exists with ORIGINAL_PASSWORD value below.
 *  - profile.html includes form fields with ids: oldPassword, newPassword, confirmPassword and
 *    shows messages in elements with classes .alert-success or .alert-error exactly as implemented.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChangePasswordTest {
    private static WebDriver driver;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String PROFILE_URL = BASE_URL + "/profile";
    private static final String LOGIN_URL = BASE_URL + "/login";

    // IMPORTANT: Adjust to a stable test account present in seed data
    private static final String TEST_EMAIL = "harrynguyen610@gmail.com";
    private static final String ORIGINAL_PASSWORD = "123a123@A"; // current password before tests

    // New passwords used during tests (ensure they meet policy)
    private static final String STRONG_NEW_PASSWORD = "NewPassw0rd!"; // >=8 chars
    private static final String ANOTHER_STRONG_PASSWORD = "AnotherP@ss1"; // used to revert if needed

    // Expected messages (must match controller literals)
    private static final String MSG_SUCCESS = "Password changed successfully.";
    private static final String MSG_OLD_INVALID = "Old password is incorrect.";
    private static final String MSG_MISMATCH = "New password and confirmation do not match.";
    private static final String MSG_WEAK = "New password is too weak (minimum 8 characters).";
    private static final String MSG_SAME = "New password must be different from the old password.";

    @BeforeAll
    public static void setUp() {
        driver = new ChromeDriver();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------------- Helper Methods ---------------- */

    private void shortWait() {
        try { Thread.sleep(1200); } catch (InterruptedException ignored) { }
    }

    private void login(String email, String password) {
        driver.get(LOGIN_URL);
        shortWait();
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        submitButton.click();
        shortWait();
        // Basic assertion that we either stayed (failed) or moved to profile (success) is done in tests.
    }

    private String attemptChangePassword(String oldPwd, String newPwd, String confirmPwd) {
        // Assumes already logged in and on /profile (or redirect there)
        driver.get(PROFILE_URL); // ensure on page
        shortWait();
        WebElement oldPassword = driver.findElement(By.id("oldPassword"));
        WebElement newPassword = driver.findElement(By.id("newPassword"));
        WebElement confirmPassword = driver.findElement(By.id("confirmPassword"));
        WebElement submitBtn = driver.findElement(By.cssSelector("form.form-grid button[type='submit']"));

        oldPassword.clear();
        oldPassword.sendKeys(oldPwd);
        newPassword.clear();
        newPassword.sendKeys(newPwd);
        confirmPassword.clear();
        confirmPassword.sendKeys(confirmPwd);
        submitBtn.click();
        shortWait();

        // Capture message (prefer specific element)
        if (driver.findElements(By.cssSelector(".alert-success")) .size() > 0) {
            return driver.findElement(By.cssSelector(".alert-success")).getText().trim();
        }
        if (driver.findElements(By.cssSelector(".alert-error")) .size() > 0) {
            return driver.findElement(By.cssSelector(".alert-error")).getText().trim();
        }
        return null; // No message (unexpected)
    }

    private void clearSession() {
        driver.manage().deleteAllCookies();
        shortWait();
    }

    private void assertLoginSucceeds(String password) {
        clearSession();
        login(TEST_EMAIL, password);
        assertTrue(driver.getCurrentUrl().contains("/profile"), "Expected successful login with provided password.");
    }

    private void assertLoginFails(String password) {
        clearSession();
        login(TEST_EMAIL, password);
        // On failure we expect to remain on /login with error parameter or page content missing /profile
        assertTrue(driver.getCurrentUrl().contains("/login"), "Expected to remain on login page (failure). Current URL: " + driver.getCurrentUrl());
    }

    private void revertPasswordIfChanged() {
        // Try login with strong new password; if success, change back to original.
        try {
            clearSession();
            login(TEST_EMAIL, STRONG_NEW_PASSWORD);
            if (driver.getCurrentUrl().contains("/profile")) {
                String msg = attemptChangePassword(STRONG_NEW_PASSWORD, ORIGINAL_PASSWORD, ORIGINAL_PASSWORD);
                // Optionally assert revert success
                assertEquals(MSG_SUCCESS, msg, "Reverting password to original should succeed.");
            }
        } catch (Exception ignored) {
            // If login failed, assume password was never changed or already reverted.
        }
    }

    @AfterEach
    public void afterEach() { // Safety revert if a test left password changed unexpectedly
        revertPasswordIfChanged();
    }

    /* ---------------- Test Cases ---------------- */

    @Test
    @Order(1)
    public void testChangePasswordOldPasswordIncorrect() {
        login(TEST_EMAIL, ORIGINAL_PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("/profile"), "Precondition: login must succeed with original password.");
        String msg = attemptChangePassword("wrong123", STRONG_NEW_PASSWORD, STRONG_NEW_PASSWORD);
        assertEquals(MSG_OLD_INVALID, msg, "Should show old password invalid message.");
        // Ensure original password still works
        assertLoginSucceeds(ORIGINAL_PASSWORD);
    }

    @Test
    @Order(2)
    public void testChangePasswordMismatchConfirmation() {
        login(TEST_EMAIL, ORIGINAL_PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        String msg = attemptChangePassword(ORIGINAL_PASSWORD, STRONG_NEW_PASSWORD, STRONG_NEW_PASSWORD + "x");
        assertEquals(MSG_MISMATCH, msg, "Should show mismatch confirmation message.");
        assertLoginSucceeds(ORIGINAL_PASSWORD);
    }

    @Test
    @Order(3)
    public void testChangePasswordWeakPassword() {
        login(TEST_EMAIL, ORIGINAL_PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        String msg = attemptChangePassword(ORIGINAL_PASSWORD, "weak1", "weak1"); // <8 chars
        assertEquals(MSG_WEAK, msg, "Should show weak password message.");
        assertLoginSucceeds(ORIGINAL_PASSWORD);
    }

    @Test
    @Order(4)
    public void testChangePasswordSameAsOld() {
        login(TEST_EMAIL, ORIGINAL_PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        String msg = attemptChangePassword(ORIGINAL_PASSWORD, ORIGINAL_PASSWORD, ORIGINAL_PASSWORD);
        assertEquals(MSG_SAME, msg, "Should show same-as-old message.");
        assertLoginSucceeds(ORIGINAL_PASSWORD);
    }

    @Test
    @Order(5)
    public void testChangePasswordSuccessAndRevert() {
        login(TEST_EMAIL, ORIGINAL_PASSWORD);
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        String msg = attemptChangePassword(ORIGINAL_PASSWORD, STRONG_NEW_PASSWORD, STRONG_NEW_PASSWORD);
        assertEquals(MSG_SUCCESS, msg, "Expected success message.");

        // Verify old password no longer works
        assertLoginFails(ORIGINAL_PASSWORD);
        // Verify new password works
        assertLoginSucceeds(STRONG_NEW_PASSWORD);

        // Revert immediately to avoid impacting other suites
        String revertMsg = attemptChangePassword(STRONG_NEW_PASSWORD, ORIGINAL_PASSWORD, ORIGINAL_PASSWORD);
        assertEquals(MSG_SUCCESS, revertMsg, "Reverting to original password should succeed.");
        assertLoginSucceeds(ORIGINAL_PASSWORD);
    }
}
