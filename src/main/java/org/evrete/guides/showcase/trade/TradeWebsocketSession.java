package org.evrete.guides.showcase.trade;

import org.evrete.api.*;
import org.evrete.guides.showcase.Message;
import org.evrete.guides.showcase.MessageWriter;
import org.evrete.guides.showcase.AbstractWebsocketSession;
import org.evrete.guides.showcase.ShowcaseUtils;
import org.evrete.guides.showcase.trade.dto.*;
import org.evrete.runtime.RuleDescriptor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeWebsocketSession extends AbstractWebsocketSession {
    static final int MAX_DATA_SIZE = 128;

    private final Knowledge knowledge;
    private final String jsonPriceHistory;

    private final List<JsonRule> jsonRules = new LinkedList<>();

    private final Map<String, String> ruleIdMapping = new HashMap<>();

    private final AtomicInteger priceStreamCounter = new AtomicInteger(0);
    private StatefulSession knowledgeSession;

    public TradeWebsocketSession(MessageWriter writer, Knowledge knowledge, String jsonPriceHistory) {
        super(writer);
        this.knowledge = knowledge;
        this.jsonPriceHistory = jsonPriceHistory;

        for (RuleDescriptor rule : knowledge.getRules()) {
            String name = rule.getName();
            // Assigning each rule an id for use in the frontend.
            String id = UUID.randomUUID().toString();
            ruleIdMapping.put(name, id);
            jsonRules.add(new JsonRule(id, name));
        }
    }

    @Override
    protected void onMessage(String body) {
        Message message = ShowcaseUtils.fromJson(body, Message.class);
        switch (message.type()) {
            case "START" -> onSessionStart();
            case "OHLC" -> onNewCandle(message.readPayloadAs(JsonCandle.class));
            case "STOP" -> onSessionStop();
            default -> throw new IllegalStateException("Unknown message type:" + body);
        }
    }

    private void onSessionStart() {
        // Close previous session (if any)
        closeSession();

        // 1. Create new session
        this.knowledgeSession = knowledge.newStatefulSession(ActivationMode.CONTINUOUS);

        // 2. Use the Activation Manager API to report currently activated rules
        this.knowledgeSession.setActivationManager(new ActivationManager() {
            @Override
            public void onActivation(RuntimeRule rule, long count) {
            for (int i = 0; i < count; i++) {
                writer.writeMessage("RULE_ACTIVATION", ruleIdMapping.get(rule.getName()));
            }
            }
        });

        // 3. Insert a new configuration instance (required by rules)
        this.knowledgeSession.insertAndFire(buildConfig());

        // 4. Notify the client that the session is ready to accept prices
        writer.writeMessage("READY");
    }

    private void onNewCandle(JsonCandle candle) {
        int idx = priceStreamCounter.getAndIncrement();
        if (idx < MAX_DATA_SIZE) {
            if(knowledgeSession != null) {
                knowledgeSession.insertAndFire(new Candle(idx, candle));
                writer.writeMessage("READY");
            }
        } else {
            // No more data allowed (showcase restriction)
            writer.writeMessage("LOG", "Max data size reached, stopping the session");
            onSessionStop();
        }
    }

    private void onSessionStop() {
        closeSession();
        writer.writeMessage("STOPPED");
    }

    private SessionConfig buildConfig() {
        SessionConfig conf = new SessionConfig();
        conf.set("MESSAGING-GW", new MessagingGateway() {

            @Override
            public void onPriceIndicator(PriceIndicator indicator) {
                 writer.writeMessage("PRICE_INDICATOR", indicator);
            }

            @Override
            public void onTradeMessage(TradeMessage message) {
                writer.writeMessage("TREND_INFO", message);
            }
        });
        return conf;
    }

    private synchronized void closeSession() {
        // Resets OHLC counter
        priceStreamCounter.set(0);
        if (knowledgeSession != null) {
            knowledgeSession.close();
            knowledgeSession = null;
        }
    }


    @Override
    protected void onClose() {
        this.closeSession();
    }

    @Override
    protected void onCreate() {
        ConfigMessage configMessage = new ConfigMessage(jsonPriceHistory, jsonRules);
        writer.writeMessage("CONFIG", configMessage);
    }
}
