package org.evrete.showcase.stock.model;

public interface MessagingGateway {

    void onPriceIndicator(PriceIndicator indicator);

    void onTradeMessage(TradeMessage message);

}
