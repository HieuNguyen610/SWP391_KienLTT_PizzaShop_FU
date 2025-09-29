package com.swp.selenium.login;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSeleniumTest {
    private static WebDriver driver;
    private static final String BASE_URL = "http://localhost:8080/login";
    private static final String TEST_EMAIL = "harrynguyen610@gmail.com"; // Change to a valid user
    private static final String TEST_PASSWORD = "1234";      // Change to a valid password

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

    @Test
    public void testLoginSuccess() throws InterruptedException {
        driver.get(BASE_URL);
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys(TEST_EMAIL);
        passwordInput.sendKeys(TEST_PASSWORD);
        submitButton.click();

        // Wait for login to complete and redirect
        Thread.sleep(2000);

        // Check for successful login by looking for a known element on the landing page
        // For example, check for a logout button or user profile element
        boolean isLoggedIn = Objects.requireNonNull(driver.getPageSource()).contains("Logout") || Objects.requireNonNull(driver.getCurrentUrl()).contains("/profile");
        assertTrue(isLoggedIn, "Login should succeed and redirect to a logged-in page.");
    }
}

