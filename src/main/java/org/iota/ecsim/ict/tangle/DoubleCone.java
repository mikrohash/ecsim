package org.iota.ecsim.ict.tangle;

import org.iota.ecsim.Constants;
import org.iota.ecsim.ec.Cluster;
import org.iota.ecsim.utils.StringLongMap;
import org.iota.ecsim.ict.model.Transaction;

import java.util.*;

public class DoubleCone {

    private final int depth, height;
    private final Set<String> future = new HashSet<>(), past = new HashSet<>();
    private final Set<Transaction> pastTransactions = new HashSet<>();
    private Map<String, Integer> pastMissingWithLowestDepth = new HashMap<>();

    public final Vertex apex;

    public DoubleCone(Vertex apex, int depth, int height) {
        this.apex = apex;
        this.depth = depth;
        this.height = height;
        reportMissing(apex.transaction.hash, 0);
        process(apex);
    }

    public boolean pastContains(String transactionHash) {
        return past.contains(transactionHash);
    }

    public void process(Vertex vertex) {
        Integer txDepth = pastMissingWithLowestDepth.remove(vertex.transaction.hash);
        if((txDepth != null && txDepth < depth) || !vertex.transaction.isBundleTail) {
            pastTransactions.add(vertex.transaction);
            reportMissing(vertex.transaction.branch, txDepth == null ? 99999 : txDepth+1);
            reportMissing(vertex.transaction.trunk, txDepth == null ? 99999 : txDepth+1);
            processIfNotNull(vertex.getBranch());
            processIfNotNull(vertex.getTrunk());
        }
    }

    private void processIfNotNull(Vertex vertex) {
        if(vertex != null)
            process(vertex);
    }

    private void reportMissing(String hash, int reportDepth) {
        past.add(hash);
        Integer knownDepth = pastMissingWithLowestDepth.get(hash);
        if(knownDepth == null || reportDepth < knownDepth) {
            pastMissingWithLowestDepth.put(hash, reportDepth);
        }
    }

    public Set<String> getMissing() {
        return pastMissingWithLowestDepth.keySet();
    }

    public boolean isComplete() {
        return pastMissingWithLowestDepth.size() == 0;
    }

    public boolean isConfident(Cluster cluster) {
        StringLongMap confidentBalances = new StringLongMap();
        StringLongMap newBalances = new StringLongMap();

        for(Transaction transaction : pastTransactions)
            (cluster.calcConfidence(transaction.hash) >= Constants.CONFIRMATION_CONFIDENCE ? confidentBalances : newBalances).add(transaction.address, transaction.value);

        // all bundles must have sum == 0 (for simplicity we do not check them individually in this simulation)
        if(newBalances.sum() + confidentBalances.sum() != 0)
            return false;

        confidentBalances.removeNegativeEntries();
        confidentBalances.add(newBalances);
        return !confidentBalances.anyNegativeEntries();
    }

    public String getRandomFromPast() {
        return new LinkedList<>(past).get((int)(Math.random() * past.size()));
    }
}