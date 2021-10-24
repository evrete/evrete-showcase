package org.evrete.showcase.stock.json;

import java.util.LinkedList;
import java.util.List;

public class ConfigMessage {
    public String prices;

    public List<JsonRule> rules = new LinkedList<>();

    public ConfigMessage(String prices) {
        this.prices = prices;
    }

}
