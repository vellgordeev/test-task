package ru.gordeev.todo.api;

import lombok.extern.slf4j.Slf4j;
import ru.gordeev.core.config.AppConfig;
import ru.gordeev.core.websocket.BaseWebSocketService;
import ru.gordeev.todo.model.TodoNotification;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class TodoWebSocketService extends BaseWebSocketService<TodoNotification> {

    private final AppConfig config;

    public TodoWebSocketService(AppConfig config) {
        this.config = config;
    }

    @Override
    protected URI getWebSocketUri() {
        try {
            String wsUri = config.websocketUri();
            log.debug("WebSocket URI from config: {}", wsUri);
            return new URI(wsUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid WebSocket URI: " + config.websocketUri(), e);

        }
    }

    @Override
    protected Class<TodoNotification> getNotificationClass() {
        return TodoNotification.class;
    }

    @Override
    protected int getConnectionTimeout() {
        return config.websocketConnectionTimeout();
    }
}