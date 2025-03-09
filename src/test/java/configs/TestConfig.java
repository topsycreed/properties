package configs;

import static constants.Constants.BASE_URL;

public class TestConfig {
    public String getBaseUrl() {
        return System.getProperty("baseUrl", BASE_URL);
    }
}
