package org.evrete.guides.showcase.trade.dto;

public interface MessagingGateway {

    void onPriceIndicator(PriceIndicator indicator);

    void onTradeMessage(TradeMessage message);

}
