package org.iota.ecsim.ict.model;

import org.iota.ecsim.utils.StringLongMap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Transfer {

    private Transfer() { }

    public static List<Transaction> build(StringLongMap changes) {
        if(changes.sum() != 0)
            throw new IllegalArgumentException("sum of values in transfer must be zero!");
        return build(changes, Collections.singletonList(Transaction.NULL_TRANSACTION.hash));
    }

    public static List<Transaction> build(StringLongMap balances, List<String> tips) {
        LinkedList<Transaction> transactionsT2H = new LinkedList<>();
        List<String> addresses = new LinkedList<>(balances.keySet());
        for(int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            long value = balances.get(address);
            boolean isTail = i == 0;
            boolean isHead = i == addresses.size()-1;
            String trunk = isTail ? pickRandomTip(tips) : transactionsT2H.get(i-1).hash;
            String branch = pickRandomTip(tips);
            transactionsT2H.add(new Transaction(address, trunk, branch, value, isHead, isTail));
        }
        assert transactionsT2H.getFirst().isBundleTail;
        assert transactionsT2H.getLast().isBundleHead;
        return transactionsT2H;
    }

    private static String pickRandomTip(List<String> tips) {
        return tips.get((int)(tips.size() * Math.random()));
    }
}