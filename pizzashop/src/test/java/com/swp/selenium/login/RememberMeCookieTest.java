package com.swp.selenium.login;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.junit.jupiter.api.Assertions.*;

public class RememberMeCookieTest {
    private static WebDriver driver;

    private static final String BASE_URL = "http://localhost:8080/login";
    private static final String TEST_EMAIL = "harrynguyen610@gmail.com";
    private static final String TEST_PASSWORD = "1234";

    @BeforeAll
    public static void setUp() {
        // Set path to chromedriver if needed
        driver = new ChromeDriver();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testRememberMeCookieExists() throws InterruptedException {
        driver.get(BASE_URL);
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement rememberMeCheckbox = driver.findElement(By.name("remember-me"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys(TEST_EMAIL);
        passwordInput.sendKeys(TEST_PASSWORD);
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
        submitButton.click();

        // Wait for login to complete and redirect
        Thread.sleep(2000);

        Cookie rememberMeCookie = driver.manage().getCookieNamed("remember-me");
        assertNotNull(rememberMeCookie, "Remember-me cookie should exist after login with Remember Me checked.");
    }
}

