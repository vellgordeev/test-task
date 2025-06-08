package ru.gordeev.core.config;

import org.aeonbits.owner.Config;

/**
 * Application configuration with support for environment variables.
 * Priority: System properties > Environment variables > config.properties
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:config.properties"
})
public interface AppConfig extends Config {

    @Key("base.uri")
    @DefaultValue("http://localhost")
    String baseUri();

    @Key("base.port")
    @DefaultValue("8080")
    int basePort();

    @Key("websocket.uri")
    @DefaultValue("ws://localhost:4242/ws")
    String websocketUri();

    @Key("websocket.connection.timeout.seconds")
    @DefaultValue("10")
    int websocketConnectionTimeout();

    @Key("websocket.notification.timeout.seconds")
    @DefaultValue("5")
    int websocketNotificationTimeout();

    @Key("websocket.reconnect.attempts")
    @DefaultValue("3")
    int websocketReconnectAttempts();

    @Key("admin.username")
    String adminUsername();

    @Key("admin.password")
    String adminPassword();

    @Key("performance.profile")
    @DefaultValue("smoke")
    String performanceProfile();

    @Key("performance.test.duration.seconds")
    @DefaultValue("60")
    int performanceTestDuration();

    @Key("performance.warmup.duration.seconds")
    @DefaultValue("10")
    int performanceWarmupDuration();

    @Key("performance.target.rps")
    @DefaultValue("100")
    int performanceTargetRps();
}
