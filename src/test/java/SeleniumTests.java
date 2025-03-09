import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

class SeleniumTests {
    WebDriver driver;

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void openHomePageTest() {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");

        Assertions.assertEquals("https://bonigarcia.dev/selenium-webdriver-java/", driver.getCurrentUrl());
        Assertions.assertEquals("Hands-On Selenium WebDriver with Java", driver.getTitle());
    }
}
