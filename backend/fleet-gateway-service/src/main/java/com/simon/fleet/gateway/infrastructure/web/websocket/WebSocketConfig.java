package com.simon.fleet.gateway.infrastructure.web.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del canal STOMP hacia el dashboard: un único endpoint con SockJS de respaldo
 * (para navegadores o redes que bloqueen WebSocket puro) y un broker simple en memoria habilitado
 * solo en el prefijo {@code /topic}. No se habilita un prefijo {@code /app} porque el flujo es
 * unidireccional servidor→dashboard: no hay mensajes cliente→servidor que enrutar hacia un
 * {@code @MessageMapping}. El broker en memoria alcanza para una única instancia del gateway; si
 * en el futuro se escala horizontalmente, cada instancia tendría su propio conjunto de sesiones y
 * habría que migrar a un broker relay real (ej. contra RabbitMQ con STOMP habilitado).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    static final String STOMP_ENDPOINT = "/ws";
    static final String BROKER_PREFIX = "/topic";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(STOMP_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(BROKER_PREFIX);
    }
}
