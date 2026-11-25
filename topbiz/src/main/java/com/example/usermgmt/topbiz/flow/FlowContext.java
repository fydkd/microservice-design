package com.example.usermgmt.topbiz.flow;

import java.util.HashMap;
import java.util.Map;

public class FlowContext {

    private final Map<String, Object> values = new HashMap<>();

    public FlowContext put(String key, Object value) {
        values.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }
}
