package org.evrete.guides.showcase;

import org.evrete.guides.showcase.town.TownWebsocketHandler;
import org.evrete.guides.showcase.trade.TradeWebsocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tradeWebsocketHandler(), "/ws/trade-app")
                .setAllowedOrigins("*");
        registry.addHandler(townWebsocketHandler(), "/ws/town-abm")
                .setAllowedOrigins("*");
    }

    public TradeWebsocketHandler tradeWebsocketHandler() {
        return new TradeWebsocketHandler();
    }
    public TownWebsocketHandler townWebsocketHandler() {
        return new TownWebsocketHandler();
    }
}
