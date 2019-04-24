package org.iota.ecsim.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StringLongMap {

    private final Map<String, Long> implementation = new HashMap<>();

    public void removeNegativeEntries() {
        for(String key : implementation.keySet())
            if(implementation.get(key) < 0)
                implementation.put(key, 0L);
    }

    public void add(StringLongMap toAdd) {
        for(String key : toAdd.implementation.keySet())
            add(key, toAdd.implementation.get(key));
    }

    public boolean anyNegativeEntries() {
        for(long value : implementation.values())
            if(value < 0)
                return true;
        return false;
    }

    public long sum() {
        long sum = 0;
        for(long value : implementation.values())
            sum += value;
        return sum;
    }

    public void add(String key, long value) {
        implementation.put(key, implementation.getOrDefault(key, 0L) + value);
    }

    public long get(String key) {
        return implementation.getOrDefault(key, 0L);
    }

    public Set<String> keySet() {
        return implementation.keySet();
    }

    public void print() {
        for(String key : implementation.keySet())
            System.out.println(key + ": " + implementation.get(key));
        System.out.println();
    }
}
