package org.iota.ecsim.ict.tangle;

import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.utils.MultiHashMap;

import java.util.*;

public class DoubleConeDag {

    private final MultiHashMap<String, String> childrenApexHashByApexHash = new MultiHashMap<>();
    private Set<DoubleCone> doubleCones;

    public DoubleConeDag(Collection<DoubleCone> doubleCones) {
        setDoubleCones(doubleCones);
    }

    public void setDoubleCones(Collection<DoubleCone> doubleCones) {
        this.doubleCones = new HashSet<>(doubleCones);
        updateDag();
    }

    public void updateDag() {
        childrenApexHashByApexHash.clear();
        for(DoubleCone cone : doubleCones)
            for(DoubleCone cone2 : doubleCones)
                if(cone.pastContains(cone2.apex.transaction.hash) && cone != cone2) {
                    childrenApexHashByApexHash.add(cone2.apex.transaction.hash, cone.apex.transaction.hash);
                }
    }

    public int countReferencing(String transactionHash) {
        Set<String> directlyReferencing = new HashSet<>();
        for(DoubleCone doubleCone : doubleCones)
            if (doubleCone.pastContains(transactionHash))
                directlyReferencing.add(doubleCone.apex.transaction.hash);
        Set<String> referencing = new HashSet<>(directlyReferencing);
        for(String dr : directlyReferencing)
            addSuccessors(dr, referencing);
        return referencing.size();
    }

    private void addSuccessors(String apexHash, Set<String> target) {
        List<String> children = childrenApexHashByApexHash.get(apexHash);
        for(String child : children)
            if(target.add(child))
                addSuccessors(child, target);
    }
}
