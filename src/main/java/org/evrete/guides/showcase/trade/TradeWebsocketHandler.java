package org.evrete.guides.showcase.trade;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.guides.showcase.MessageWriter;
import org.evrete.guides.showcase.ShowcaseUtils;
import org.evrete.guides.showcase.trade.rules.EmaRuleset;
import org.evrete.guides.showcase.trade.rules.TradingRuleset;
import org.evrete.guides.showcase.AbstractWebsocketHandler;

import java.io.IOException;

public class TradeWebsocketHandler extends AbstractWebsocketHandler<TradeWebsocketSession> {
    private final Knowledge knowledge;
    private final String jsonPriceHistory;
    public TradeWebsocketHandler() {
        try {
            KnowledgeService knowledgeService = new KnowledgeService();
            this.jsonPriceHistory = ShowcaseUtils.readResourceAsString("/META-INF/data/trade/stock-history.json");
            this.knowledge = knowledgeService.newKnowledge(
                    "JAVA-CLASS", EmaRuleset.class, TradingRuleset.class
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected TradeWebsocketSession newSession(MessageWriter writer) {
        return new TradeWebsocketSession(writer, knowledge, jsonPriceHistory);
    }
}
