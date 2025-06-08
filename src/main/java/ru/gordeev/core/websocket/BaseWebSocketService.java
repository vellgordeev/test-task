package ru.gordeev.core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base service for WebSocket client implementations with generic notification support.
 * Provides thread-safe connection management, message parsing, and metrics tracking.
 *
 * @param <T> The type of notification messages this service will handle
 */
@Slf4j
public abstract class BaseWebSocketService<T> {

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final BlockingQueue<T> notifications = new LinkedBlockingQueue<>();

    private final AtomicInteger messagesReceived = new AtomicInteger(0);
    private final AtomicInteger errorsCount = new AtomicInteger(0);
    private final Object connectionLock = new Object();

    private WebSocketClient client;
    private CountDownLatch connectionLatch;

    protected abstract URI getWebSocketUri();

    protected abstract Class<T> getNotificationClass();

    protected abstract int getConnectionTimeout();

    /**
     * Establishes WebSocket connection.
     * Thread-safe implementation that prevents multiple simultaneous connections.
     *
     * @throws Exception if connection fails or times out
     */
    public void connect() throws Exception {
        synchronized (connectionLock) {
            if (isConnected()) {
                return;
            }

            if (client != null) {
                client.close();
            }

            connectionLatch = new CountDownLatch(1);
            URI uri = getWebSocketUri();

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connectionLatch.countDown();
                    BaseWebSocketService.this.onConnect();
                }

                @Override
                public void onMessage(String message) {
                    messagesReceived.incrementAndGet();
                    try {
                        T notification = objectMapper.readValue(message, getNotificationClass());
                        notifications.offer(notification);
                        BaseWebSocketService.this.onNotification(notification);
                    } catch (Exception e) {
                        errorsCount.incrementAndGet();
                        log.error("Failed to parse WebSocket message", e);
                        BaseWebSocketService.this.onError(e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    BaseWebSocketService.this.onDisconnect(code, reason);
                }

                @Override
                public void onError(Exception e) {
                    errorsCount.incrementAndGet();
                    log.error("WebSocket error", e);
                    BaseWebSocketService.this.onError(e);
                }
            };

            client.connect();

            if (!connectionLatch.await(getConnectionTimeout(), TimeUnit.SECONDS)) {
                client.close();
                throw new TimeoutException("WebSocket connection timeout after " + getConnectionTimeout() + " seconds");
            }
        }
    }

    /**
     * Waits for next notification with specified timeout.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the notification, or null if timeout occurs
     * @throws InterruptedException if interrupted while waiting
     */
    public T waitForNotification(long timeout, TimeUnit unit) throws InterruptedException {
        return notifications.poll(timeout, unit);
    }

    /**
     * Clears all pending notifications from the queue.
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * Closes the WebSocket connection.
     */
    public void disconnect() {
        WebSocketClient localClient;
        synchronized (connectionLock) {
            localClient = client;
            client = null;
        }
        if (localClient != null) {
            localClient.close();
        }
    }

    /**
     * @return true if WebSocket is currently connected
     */
    public boolean isConnected() {
        synchronized (connectionLock) {
            return client != null && client.isOpen();
        }
    }

    public int getMessagesReceivedCount() {
        return messagesReceived.get();
    }

    public int getErrorsCount() {
        return errorsCount.get();
    }

    public int getQueueSize() {
        return notifications.size();
    }

    /**
     * Resets all metrics and clears notification queue.
     */
    public void resetMetrics() {
        messagesReceived.set(0);
        errorsCount.set(0);
        notifications.clear();
    }

    protected void onConnect() {
    }

    protected void onNotification(T notification) {
    }

    protected void onDisconnect(int code, String reason) {
    }

    protected void onError(Exception e) {
    }
}
