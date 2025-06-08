package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import ru.gordeev.core.api.ApiProvider;
import ru.gordeev.core.config.AppConfig;
import ru.gordeev.core.data.TestDataRegistry;

/**
 * Base class for all tests.
 * Provides common setup and utilities.
 */
@Slf4j
public abstract class BaseTest {

    protected static AppConfig config;
    protected ApiProvider api;
    protected TestDataRegistry testData;
    protected ObjectMapper objectMapper;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        config = ConfigFactory.create(AppConfig.class);

        RestAssured.baseURI = config.baseUri();
        RestAssured.port = config.basePort();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        objectMapper = new ObjectMapper();

        log.info("Test suite initialized. Base URI: {}:{}", config.baseUri(), config.basePort());
    }

    @BeforeClass(alwaysRun = true)
    public void setupApiClient() {
        api = new ApiProvider();
        testData = new TestDataRegistry();

        configureServices();
        configureTestData();
    }

    /**
     * Configure API services needed for tests.
     * Override this method to register your API services.
     */
    protected abstract void configureServices();

    /**
     * Configure test data factories.
     * Override this method to register your test data builders.
     */
    protected abstract void configureTestData();
}