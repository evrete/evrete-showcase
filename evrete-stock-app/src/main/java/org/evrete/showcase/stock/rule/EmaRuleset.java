package org.evrete.showcase.stock.rule;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Fact;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
import org.evrete.showcase.stock.model.Candle;
import org.evrete.showcase.stock.model.MessagingGateway;
import org.evrete.showcase.stock.model.PriceIndicator;
import org.evrete.showcase.stock.model.SessionConfig;

/**
 * <p>
 *     This ruleset, when included in the distribution, automatically generates EMA indicators
 *     for every price candle (open, high, low, close). It requires an <code>'EMA-CONFIG'</code>
 *     configuration entry which must be an array of smoothing values.
 * </p>
 * <p>
 *     For example, for a configuration array of <code>[12, 26, 45]</code>, the ruleset
 *     will generate instances of {@link PriceIndicator} with the following names:
 *     <code>"EMA12", "EMA26", "EMA45"</code>.
 * </p>
 */
@RuleSet
public class EmaRuleset extends Constants {
    MessagingGateway messenger;


    /**
     * <p>
     *     This rule inserts into session EMA configurations entities
     *     in accordance with the {@link SessionConfig}'s <code>"EMA-CONFIG"</code>
     *     entry.
     * </p>
     * @param ctx RHS context
     * @param conf session configuration
     */
    @Rule("EMA-0: Initialization")
    public void init(RhsContext ctx,  SessionConfig conf) {
        this.messenger = conf.get("MESSAGING-GW");
        ctx.insert(new EmaConfig(EMA12, 12));
        ctx.insert(new EmaConfig(EMA26, 26));
    }

    /**
     * <p>
     *     Initialization of EMA indicators at time "zero" when there is no
     *     previous value available
     * </p>
     * @param ctx RHS context
     * @param event first candle
     */
    @Rule("EMA-1: Compute initial")
    @Where("$evt.id == 0")
    public void rule1(RhsContext ctx,  @Fact("$evt") Candle event, EmaConfig emaConfig) {
        PriceIndicator ind = new PriceIndicator(event, emaConfig.name, event.close);
        messenger.onPriceIndicator(ind);
        ctx.insert(ind);
    }

    /**
     * <p>
     *     Computing the next EMA based on previous values
     * </p>
     * @param ctx RHS context
     * @param event first candle
     */
    @Rule("EMA-2: Compute next")
    @Where({"$evt.id == $prev.id + 1", "$conf.name == $prev.name"})
    public void rule2(RhsContext ctx,  @Fact("$evt") Candle event,  @Fact("$prev") PriceIndicator prev,  @Fact("$conf") EmaConfig conf) {
        double k = 2.0 /(conf.smoothingRange + 1);
        double nextValue = k * event.close + (1.0 - k) * prev.value;
        PriceIndicator ind = new PriceIndicator(event, conf.name, nextValue);
        ctx.insert(ind);
        messenger.onPriceIndicator(ind);
    }


    public static class EmaConfig {
        public final int smoothingRange;
        public final String name;

        public EmaConfig(String name, int smoothingRange) {
            this.smoothingRange = smoothingRange;
            this.name = name;
        }

        @Override
        public String toString() {
            return "EmaConfig{" +
                    "smooth=" + smoothingRange +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

}
