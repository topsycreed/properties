# Работа с properties

Этот README объясняет, как настроить и использовать properties для передачи в тесты переменные и конфиденциальные данные.
Такие, как логины, пароли. Также с помощью properties можно быстро переключаться между набором переменных в зависимости от тестового окружения.

## Чтение системных properties

Чтобы добавить чтение системных properties в задачу на запуск тестов внутри build.gradle нужно добавить: systemProperties(System.getProperties())
```groovy
test {
    useJUnitPlatform()
    systemProperties(System.getProperties())
}
```

Теперь если запустить тесты, можно через -D передавать системные переменные:
```bash
gradle test -DbaseUrl="test"
```

## Файл конфигураций
Для удобства работы с системными properties можно создать отдельный класс, например TestConfig:
```java
public class TestConfig {
    public String getBaseUrl() {
        return System.getProperty("baseUrl", BASE_URL);
    }
}
```
С помощью метода System.getProperty можно получать переменные по их имени, кроме того можно передать значение по умолчанию,
которое будет использоваться на случай отсутствия системной переменной. В нашем случае удобно взять такое значение из класса констант.

В самих тестах нужно инициализировать экземпляр класса TestConfig и использовать методы getBaseUrl() для получения значений.
```java
class SeleniumTests {
    WebDriver driver;
    TestConfig config = new TestConfig();

    @Test
    void openHomePageTest() {
        driver.get(config.getBaseUrl());

        assertEquals(config.getBaseUrl(), driver.getCurrentUrl());
        assertEquals("Hands-On Selenium WebDriver with Java", driver.getTitle());
    }
}
```

Альтернативный способ задания системных properties внутри build.gradle:
```groovy
test {
    useJUnitPlatform()
    systemProperties(System.getProperties())
    systemProperty "baseUrl", System.getProperty("baseUrl", "https://bonigarcia.dev/selenium-webdriver-java/")
}
```

Альтернативный способ задания системных properties внутри Maven Surefire плагина:
```xml
<properties>
    <baseUrl>https://bonigarcia.dev/selenium-webdriver-java/</baseUrl>
</properties>

<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M7</version>
        <configuration>
            <systemProperties>
                <property>
                    <name>baseUrl</name>
                    <value>${baseUrl}</value>
                </property>
            </systemProperties>
        </configuration>
    </plugin>
</plugins>
</build>
```

## Properties файлы

Properties-файлы (.properties) — это текстовые файлы, используемые для хранения конфигурационных параметров в формате ключ=значение.
Такой формат нам отлично подходит для хранения значения переменных. 
Можно иметь отдельный Properties-файл на каждое тестовое окружение, чтобы быстро переключаться между ними.

Обычно properties-файлы файлы хранятся в src/test/resources

Пример default.properties:
```properties
baseUrl=https://bonigarcia.dev/selenium-webdriver-java/
```
Можно использовать # для комментариев. Пробелы вокруг = игнорируются.

## Чтение из Properties файлов

Для чтения из Properties файлов нужно будет обновить наш конфигурационный класс:
```java
public class TestConfig {
    String env;
    Properties properties;

    public TestConfig() {
        env = System.getProperty("env", "default");
        properties = getPropertiesByEnv(env);
    }

    public String getBaseUrl() {
        String baseUrl = properties.getProperty("baseUrl");
        assertNotNull(baseUrl, String.format("BaseUrl is not found in %s.properties", env));
        System.out.println("Base URL: " + baseUrl);
        return baseUrl;
    }

    private Properties getPropertiesByEnv(String env) {
        Properties testProperties = new Properties();
        try {
            testProperties.load(getClass().getClassLoader().getResourceAsStream(env + ".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Cannot open %s.properties", env));
        }
        return testProperties;
    }
}
```

Так как у нас есть несколько properties файлов, то нам нужен способ переключаться между ними.
Для этого нужно добавить поля: env и properties и конструктор, который будет инициализировать эти поля.
```java
public class TestConfig {
    String env;
    Properties properties;

    public TestConfig() {
        env = System.getProperty("env", "default");
        properties = getPropertiesByEnv(env);
    }
}
```
В данном случае название переменной "env", значение по умолчанию "default" (можно тоже вынести в класс констант).
И уже по выбранному значению env нужно загружать значения из properties файла.

```java
public class TestConfig {
    private Properties getPropertiesByEnv(String env) {
        Properties testProperties = new Properties();
        try {
            testProperties.load(getClass().getClassLoader().getResourceAsStream(env + ".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Cannot open %s.properties", env));
        }
        return testProperties;
    }
}
```
Чтобы реализовать утилитарный getPropertiesByEnv метод нужно добавить импорт import java.util.Properties; также стоит реализовать обработку исключений.
Например, ситуации, если файла для такого тестового окружения по имени env не существует, либо внутри properties файла нет нужной переменной.

Цепочка методов load(getClass().getClassLoader().getResourceAsStream(env + ".properties")); служит для загрузки properties файла по имени env из resources.

Теперь когда у нас читается properties файл можно получить конкретное значение переменной, например, baseUrl:
```java
public class TestConfig {
    public String getBaseUrl() {
        String baseUrl = properties.getProperty("baseUrl");
        assertNotNull(baseUrl, String.format("BaseUrl is not found in %s.properties", env));
        System.out.println("Base URL: " + baseUrl);
        return baseUrl;
    }
}
```
Читать переменную из properties файла можно через метод getProperty() просто передавая имя переменной.
В методе также можно обработать исключения или логировать значение в консоль для дебаггинга.

Чтобы запустить тесты с нужным окружением, можно выполнить команду:
```bash
gradle test -Denv="test"
```

## Добавление конфиденциальных данных

При работе с проектами может возникнуть необходимость передавать не только baseUrl, но и логины, пароли, API-ключи. Ни в коем случае не храните такие данные в коде! Это может привести к утечке данных и компрометации учетных записей.

Одним из возможных вариантов является использование properties файлов, в которых значения переменных будет храниться только локально.
А в CI/CD данные поля можно будет передать как secret параметры на уровне самой джобы.

Алгоритм действий:
1. Создайте .properties файл с неконфиденциальными настройками и закоммитьте его
```properties
baseUrl=https://bonigarcia.dev/selenium-webdriver-java/
```
2. Добавьте ключи для секретных данных, но без значений, и закоммитьте
```properties
baseUrl=https://bonigarcia.dev/selenium-webdriver-java/
username=
password=
```
3. Добавьте .properties файлы в .gitignore, чтобы Git перестал их отслеживать
```gitignore
src/test/resources/*.properties
```
4. Добавьте секретные значения локально и убедитесь, что они не попадают в коммиты
```properties
baseUrl=https://bonigarcia.dev/selenium-webdriver-java/
username=secretUser
password=secretPassword
```
5. Дополнительно: Если нужно перестать отслеживать изменения в файле, можно выполнить комманду:
```bash
git update-index --skip-worktree src/test/resources/yourName.properties
```

### Исправления в конфигурационном классе

Если нужно читать несколько переменных, то будет удобно вынести логику чтения переменных в отдельный утилитарный метод.
Кроме того, для CI/CD нужно настроить чтение секретных переменных из системных properties.
Тогда методы для получения переменных будут выглядеть так:

```java
    public String getBaseUrl() {
        return getFieldByName("baseUrl");
    }

    private String getFieldByName(String fieldName) {
        String field = properties.getProperty(fieldName);
        if (field == null || field.isEmpty()) {
            field = System.getProperty(fieldName, field);
        }
        assertNotNull(field, String.format("%s is not found in %s.properties and not set by system properties", fieldName, env));
        System.out.printf("%s: %s%n", fieldName, field);
        return field;
    }
```
Если поле нашлось в properties - то оно используется. Если не нашлось, то проверяется, есть ли значение для него в системных properties.
Если их нет и в системных properties, то выбрасывается сообщение об ошибке.

Пример запуска тестов:
```bash
gradle test -Denv=test -Dusername="username" -Dpassword="password"
```

## Использование библиотеки Owner

Owner — это библиотека для удобной работы с .properties файлами в Java. 
Она позволяет автоматически загружать значения из конфигурационных файлов в интерфейсы, упрощая код и устраняя необходимость вручную работать с Properties.

Дополнительный функционал:

✅ Автоматическое соответствие значений из .properties в Java-интерфейсы

✅ Поддержка типов данных (не только String, но и int, boolean, double и т. д.)

✅ Значения по умолчанию

✅ Приоритет списку properties

Для работы с Owner нужно добавить зависимость:
```groovy
dependencies {
    testImplementation 'org.aeonbits.owner:owner:1.0.12'
}
```

После этого нужно добавить интерфейс для работы с конфигом (старый конфиг нам больше не нужен).
```java
@Config.Sources({
        "classpath:${env}.properties",
        "classpath:default.properties"
})
public interface TestPropertiesConfig extends Config {
    @Key("baseUrl")
    @DefaultValue(Constants.BASE_URL)
    String getBaseUrl();

    @Key("username")
    String getUsername();

    @Key("password")
    String getPassword();
}
```
@Config.Sources - позволяет указать все properties файлы в порядке их приоритета. 
Добавление "classpath:${env}.properties" первым в списке позволяет использовать пользовательский properties файл по тестовому окружению из переменной env.

Интерфейс обязательно должен расширять Config через: 'extends Config'.

Внутри интерфейса нужно перечислить список методов для каждой переменной. Нужно использовать аннотацию @Key("baseUrl") для мапинга метода и системной переменной или названия переменной из properties файла.

@DefaultValue(Constants.BASE_URL) - позволяет задать значение по умолчанию, если его не нашлось в системных переменных или properties.
Кроме того, можно использовать не только тип String, а например и числа или boolean.

Чтобы использовать новую конфигурацию ее нужно загрузить, например как это выглядит в классе с тестами:
```java
class SeleniumTests {
    WebDriver driver;
    TestPropertiesConfig config = ConfigFactory.create(TestPropertiesConfig.class, System.getProperties());

    @Test
    void openHomePageTest() {
        driver.get(config.getBaseUrl());

        assertEquals(config.getBaseUrl(), driver.getCurrentUrl());
        assertEquals("Hands-On Selenium WebDriver with Java", driver.getTitle());
    }
}
```

### Запуск тестов:
Запуск тестов с default.properties, потому что env не передан:
```bash
gradle test
```

Запуск тестов с test.properties, переопределение переменных для username и username:
```bash
gradle test -Denv=test -Dusername="username" -Dusername="password"
```

Как видно с помощью Owner можно значительно сократить время на обработку системных переменных и работу с properties файлами.
Из коробки доступно переопределение любой переменной через передачу системной переменной.

