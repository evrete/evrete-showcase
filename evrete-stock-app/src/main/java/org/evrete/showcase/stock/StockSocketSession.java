package org.evrete.showcase.stock;

import org.evrete.api.*;
import org.evrete.runtime.RuleDescriptor;
import org.evrete.showcase.shared.AbstractSocketSession;
import org.evrete.showcase.stock.json.ConfigMessage;
import org.evrete.showcase.stock.json.JsonCandle;
import org.evrete.showcase.stock.json.JsonRule;
import org.evrete.showcase.stock.model.*;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StockSocketSession extends AbstractSocketSession {
    static final int MAX_DATA_SIZE = 128;

    private final AtomicInteger counter = new AtomicInteger(0);
    private StatefulSession knowledgeSession;
    private final Map<String, String> ruleIdMapping = new HashMap<>();

    StockSocketSession(Session session) {
        super(session);
        reportConfiguration();
    }

    /**
     * <p>
     * Informs the frontend about rules and provides default trading dataset.
     * </p>
     */
    private void reportConfiguration() {
        ConfigMessage configMessage = new ConfigMessage(AppContext.DEFAULT_STOCK_HISTORY);
        Knowledge knowledge = AppContext.knowledge();

        List<JsonRule> jsonRules = new LinkedList<>();
        for (RuleDescriptor rule : knowledge.getRules()) {
            String name = rule.getName();
            // Assigning an id for use in the frontend.
            String id = UUID.randomUUID().toString();
            ruleIdMapping.put(name, id);
            jsonRules.add(new JsonRule(id, name));
        }

        configMessage.rules.addAll(jsonRules);
        send("CONFIG", configMessage);
        send("LOG", "Maximum entries: " + MAX_DATA_SIZE);
    }

    @Override
    public void process(String type, String payload) {
        switch (type) {
            case "START":
                onSessionStart();
                break;
            case "STOP":
                onSessionStop();
                break;
            case "OHLC":
                // Processing a new price data from frontend
                onNewCandle(fromJson(payload, JsonCandle.class));
                break;
            default:
                sendError("Unknown command " + type);
        }
    }

    private void onNewCandle(JsonCandle candle) {
        int idx = counter.getAndIncrement();
        if (idx < MAX_DATA_SIZE) {
            if(knowledgeSession != null) {
                knowledgeSession.insertAndFire(new Candle(idx, candle));
                send("READY");
            }
        } else {
            // No more data allowed (showcase restriction)
            send("LOG", "Max data size reached, stopping the session");
            onSessionStop();
        }
    }

    private void onSessionStop() {
        closeSession();
        send("STOPPED");
    }

    public synchronized void closeSession() {
        // Resets OHLC counter
        counter.set(0);
        if (knowledgeSession != null) {
            knowledgeSession.close();
            knowledgeSession = null;
        }
    }

    synchronized void onSessionStart() {
        // Close previous session (if any)
        closeSession();

        // 1. Create new session
        this.knowledgeSession = AppContext.knowledge().newStatefulSession(ActivationMode.CONTINUOUS);

        // 2. Use the Activation Manager API to report currently activated rules
        this.knowledgeSession.setActivationManager(new ActivationManager() {
            @Override
            public void onActivation(RuntimeRule rule, long count) {
                for (int i = 0; i < count; i++) {
                    send("RULE_ACTIVATION", ruleIdMapping.get(rule.getName()));
                }
            }
        });

        // 3. Insert a new configuration instance (required by rules)
        this.knowledgeSession.insertAndFire(buildConfig());


        send("READY");
    }

    private SessionConfig buildConfig() {
        SessionConfig conf = new SessionConfig();
        conf.set("MESSAGING-GW", new MessagingGateway() {

            @Override
            public void onPriceIndicator(PriceIndicator indicator) {
                send("PRICE_INDICATOR", indicator);
            }

            @Override
            public void onTradeMessage(TradeMessage message) {
                send("TREND_INFO", message);
            }
        });
        return conf;
    }

}
