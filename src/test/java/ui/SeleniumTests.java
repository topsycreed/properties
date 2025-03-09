package ui;

import configs.TestPropertiesConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeleniumTests {
    WebDriver driver;
    TestPropertiesConfig config = ConfigFactory.create(TestPropertiesConfig.class, System.getProperties());

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
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

    @Test
    void signInTest() {
        driver.get(config.getBaseUrl());
        driver.findElement(By.xpath("//a[@href = 'login-form.html']")).click();

        driver.findElement(By.id("username")).sendKeys(config.getUsername());
        driver.findElement(By.id("password")).sendKeys(config.getPassword());
        driver.findElement(By.xpath("//button[@type = 'submit']")).click();
        WebElement message = driver.findElement(By.className("alert"));

        assertEquals("Login successful", message.getText());
    }
}
