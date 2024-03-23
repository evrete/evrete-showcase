package org.evrete.guides.showcase.trade.dto;

import java.util.List;

public record ConfigMessage(String prices, List<JsonRule> rules) {

}
