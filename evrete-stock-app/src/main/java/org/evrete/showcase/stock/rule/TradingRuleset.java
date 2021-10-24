package org.evrete.showcase.stock.rule;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Fact;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
import org.evrete.showcase.stock.model.MessagingGateway;
import org.evrete.showcase.stock.model.PriceIndicator;
import org.evrete.showcase.stock.model.SessionConfig;
import org.evrete.showcase.stock.model.TradeMessage;

@RuleSet
public class TradingRuleset extends Constants {
    MessagingGateway messenger;

    /**
     * <p>
     *     This rule fires only once when an instance of {@link SessionConfig} is
     *     inserted into the session
     * </p>
     * @param conf session configuration
     */
    @Rule("MAIN-0: Initialization")
    public void init(SessionConfig conf) {
        this.messenger = conf.get("MESSAGING-GW");
    }

    /**
     * <p>
     *     Computes and injects into memory a new 'DIFF' {@link PriceIndicator} which is
     *     computed as a difference between EMA12 and EMA26 values. To get the indicators
     *     "warm up" and pick up the trend, the computation only starts
     *     after 12 candles.
     * </p>
     * @param ctx RHS context
     * @param ema12 EMA12 indicator
     * @param ema26 EMA26 indicator
     */
    @Rule("MAIN-1: DIFF = EMA26 - EMA12")
    @Where({"$ema12.id == $ema26.id", "$ema12.id > 11", "$ema12.name == EMA12", "$ema26.name == EMA26"})
    public void diff(RhsContext ctx,  @Fact("$ema12") PriceIndicator ema12,  @Fact("$ema26") PriceIndicator ema26) {
        PriceIndicator diff = new PriceIndicator(ema12, DIFF, ema26.value - ema12.value);
        ctx.insert(diff);
    }

    @Rule("MAIN-2: Bullish trend detection")
    @Where({"$now.id == $prev.id + 1", "$now.value < 0",  "$prev.value > 0",  "$now.name == DIFF", "$prev.name == DIFF"})
    public void trendUp(@Fact("$now") PriceIndicator now,  @Fact("$prev") PriceIndicator prev) {
        messenger.onTradeMessage(new TradeMessage(now, "Bullish"));
    }

    @Rule("MAIN-3: Bearish trend detection")
    @Where({"$now.id == $prev.id + 1", "$now.value > 0",  "$prev.value < 0",  "$now.name == DIFF", "$prev.name == DIFF"})
    public void trendDown(@Fact("$now") PriceIndicator now,  @Fact("$prev") PriceIndicator prev) {
        messenger.onTradeMessage(new TradeMessage(now, "Bearish"));
    }

}
