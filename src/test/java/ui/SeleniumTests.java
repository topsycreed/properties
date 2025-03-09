package ui;

import configs.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeleniumTests {
    WebDriver driver;
    TestConfig config = new TestConfig();

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void openHomePageTest() {
        driver.get(config.getBaseUrl());

        assertEquals(config.getBaseUrl(), driver.getCurrentUrl());
        assertEquals("Hands-On Selenium WebDriver with Java", driver.getTitle());
    }

    @Test
    void openLoginPageTest() {
        driver.get(config.getBaseUrl());

        driver.findElement(By.xpath("//a[@href = 'login-form.html']")).click();
        WebElement title = driver.findElement(By.className("display-6"));

        assertEquals(config.getBaseUrl() + "login-form.html", driver.getCurrentUrl());
        assertEquals("Login form", title.getText());
    }
}
