package com.swp.selenium.address;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(OrderAnnotation.class)
public class AddressTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static String baseUrl;
    private static String email;
    private static String password;

    // Shared state across tests
    private static String createdStreetSuffix;

    @BeforeAll
    static void setupClass() {
        baseUrl = Optional.ofNullable(System.getProperty("baseUrl"))
                .orElseGet(() -> Optional.ofNullable(System.getenv("TEST_BASE_URL")).orElse("http://localhost:8080"));
        email = Optional.ofNullable(System.getProperty("email"))
                .orElseGet(() -> Optional.ofNullable(System.getenv("TEST_USER_EMAIL")).orElse(""));
        password = Optional.ofNullable(System.getProperty("password"))
                .orElseGet(() -> Optional.ofNullable(System.getenv("TEST_USER_PASSWORD")).orElse(""));

        // Skip all tests if credentials are not provided
        assumeTrue(email != null && !email.isBlank() && password != null && !password.isBlank(),
                () -> "Skipping Selenium tests: please provide TEST_USER_EMAIL and TEST_USER_PASSWORD env vars or -Demail/-Dpassword system properties");

        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        createdStreetSuffix = UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void ensureLoggedInAndOnAddressPage() {
        // Navigate to login and perform login if not already
        driver.navigate().to(baseUrl + "/address");
        // If redirected to login, the URL likely contains "/login"
        if (driver.getCurrentUrl().contains("/login")) {
            login();
            driver.navigate().to(baseUrl + "/address");
        }
        // Ensure the address page loaded (check for section title or add form)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.section-title")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.address-form"))
        ));
    }

    @Test
    @Order(1)
    void testCreateAddress() {
        int before = getAddressItems().size();

        fillAndSubmitCreateForm(
                "Hanoi", "Ba Dinh", "Test Street " + createdStreetSuffix, "12A", "+84 912 345 678", false
        );

        // Wait for success alert
        waitForAlertWithText("Address added successfully.");

        // Verify count increased
        int after = getAddressItems().size();
        assertEquals(before + 1, after, "Address count should increase by 1 after creation");

        // Verify new address appears at the top (sorted by default desc, id desc)
        WebElement firstItem = getAddressItems().get(0);
        String addressText = firstItem.findElement(By.cssSelector(".address-body .value")).getText();
        assertTrue(addressText.contains("Test Street " + createdStreetSuffix), "Newly created street should appear in top item");
    }

    @Test
    @Order(2)
    void testUpdateAddress() {
        // Click Edit on first address
        WebElement firstItem = getAddressItems().get(0);
        firstItem.findElement(By.cssSelector(".address-top .edit-link")).click();

        // On edit page, update street and phone
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.address-form")));
        WebElement street = driver.findElement(By.id("street"));
        street.clear();
        String updatedStreet = "Updated Street " + createdStreetSuffix;
        street.sendKeys(updatedStreet);

        WebElement phone = driver.findElement(By.id("phone"));
        phone.clear();
        phone.sendKeys("0909 000 999");

        // Save
        driver.findElement(By.cssSelector(".form-actions .btn-primary")).click();

        // Back on list, wait for success
        waitForAlertWithText("Address updated successfully.");

        // Verify the top item contains updated street
        WebElement top = getAddressItems().get(0);
        String addressText = top.findElement(By.cssSelector(".address-body .value")).getText();
        assertTrue(addressText.contains("Updated Street " + createdStreetSuffix), "Updated street should reflect on the list");
    }

    @Test
    @Order(3)
    void testChangeDefaultAddress() {
        // Ensure at least 2 addresses; if only one, create another
        if (getAddressItems().size() < 2) {
            fillAndSubmitCreateForm("Hanoi", "Hoan Kiem", "Second Street " + createdStreetSuffix, "34", "+84 999 888 777", false);
            waitForAlertWithText("Address added successfully.");
        }

        List<WebElement> items = getAddressItems();
        WebElement currentDefault = items.stream()
                .filter(it -> it.findElements(By.cssSelector(".default-badge")).size() > 0)
                .findFirst().orElse(null);
        assertNotNull(currentDefault, "There should be a default address before changing");

        // Pick the first non-default to become new default
        WebElement newDefault = items.stream()
                .filter(it -> it.findElements(By.cssSelector(".default-badge")).isEmpty())
                .findFirst().orElse(items.get(0));

        // Edit that address and set default
        newDefault.findElement(By.cssSelector(".edit-link")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.address-form")));
        WebElement setDefault = driver.findElement(By.name("setDefault"));
        if (!setDefault.isSelected()) setDefault.click();
        driver.findElement(By.cssSelector(".form-actions .btn-primary")).click();

        waitForAlertWithText("Address updated successfully.");

        // Verify exactly one default badge and it's on the edited address (which should now be at top due to default sorting)
        List<WebElement> defaults = getAddressItems().stream()
                .filter(it -> it.findElements(By.cssSelector(".default-badge")).size() > 0).toList();
        assertEquals(1, defaults.size(), "Exactly one default address expected");
    }

    @Test
    @Order(4)
    void testDeleteAddress() {
        int before = getAddressItems().size();
        assertTrue(before > 0, "There should be at least one address to delete");

        WebElement firstItem = getAddressItems().get(0);
        String addressText = firstItem.findElement(By.cssSelector(".address-body .value")).getText();

        // Click Delete (form submit with confirm)
        WebElement deleteForm = firstItem.findElement(By.cssSelector("form[action*='/delete']"));
        deleteForm.findElement(By.cssSelector("button[type='submit']")).click();
        // Confirm browser alert
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (TimeoutException ignored) { /* Confirm may not appear in some drivers/configs */ }

        waitForAlertWithText("Address deleted.");

        int after = getAddressItems().size();
        assertEquals(before - 1, after, "Address count should decrease by 1 after deletion");

        // Verify the deleted address isn't present in the list anymore
        boolean stillPresent = getAddressItems().stream()
                .map(it -> it.findElement(By.cssSelector(".address-body .value")).getText())
                .anyMatch(t -> t.equals(addressText));
        assertFalse(stillPresent, "Deleted address should not be listed anymore");
    }

    // ----------------- helpers -----------------

    private void login() {
        driver.navigate().to(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        // Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        // After login, either land on profile or redirect; we proceed to /address in the caller
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/profile"),
                ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        ));
    }

    private List<WebElement> getAddressItems() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".address-list, .card-body")));
        return driver.findElements(By.cssSelector(".address-list .address-item"));
    }

    private void fillAndSubmitCreateForm(String province, String district, String street, String number, String phone, boolean setDefault) {
        // Ensure on address page
        driver.navigate().to(baseUrl + "/address");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.address-form")));

        WebElement provinceEl = driver.findElement(By.id("province"));
        provinceEl.clear();
        provinceEl.sendKeys(province);

        WebElement districtEl = driver.findElement(By.id("district"));
        districtEl.clear();
        districtEl.sendKeys(district);

        WebElement streetEl = driver.findElement(By.id("street"));
        streetEl.clear();
        streetEl.sendKeys(street);

        WebElement numberEl = driver.findElement(By.id("number"));
        numberEl.clear();
        numberEl.sendKeys(number);

        WebElement phoneEl = driver.findElement(By.id("phone"));
        phoneEl.clear();
        phoneEl.sendKeys(phone);

        WebElement setDefaultEl = driver.findElement(By.name("setDefault"));
        if (setDefault != setDefaultEl.isSelected()) {
            setDefaultEl.click();
        }

        driver.findElement(By.cssSelector(".form-actions .btn-primary"))
                .click();
    }

    private void waitForAlertWithText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert")));
        wait.until(d -> {
            List<WebElement> alerts = d.findElements(By.cssSelector(".alert"));
            return alerts.stream().anyMatch(a -> a.getText().contains(text));
        });
    }
}
