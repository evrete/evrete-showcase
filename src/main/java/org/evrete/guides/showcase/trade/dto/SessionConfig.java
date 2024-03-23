package org.evrete.guides.showcase.trade.dto;

import java.util.HashMap;
import java.util.Map;

public class SessionConfig {
    private final Map<String, Object> data = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object o = this.data.get(key);
        if (o == null) {
            throw new IllegalStateException("No value associated with key '" + key + "' is found in " + this);
        } else {
            return (T) this.data.get(key);
        }
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

}
