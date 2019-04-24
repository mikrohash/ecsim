package org.iota.ecsim.ict.tangle;

import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.utils.MultiHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tangle {

    private MultiHashMap<String, Vertex> orphansByParent = new MultiHashMap<>();
    private Map<String, Vertex> verticesByHash = new HashMap<>();

    public Tangle() {
        addTransaction(Transaction.NULL_TRANSACTION);
    }

    public synchronized Vertex addTransaction(Transaction transaction) {
        if(verticesByHash.containsKey(transaction.hash))
            return null;
        Vertex vertex = new Vertex(transaction);
        buildReferences(vertex);
        verticesByHash.put(transaction.hash, vertex);
        return vertex;
    }

    private void buildReferences(Vertex vertex) {
        Vertex branch = findVertexByHash(vertex.transaction.branch);
        Vertex trunk = findVertexByHash(vertex.transaction.trunk);
        if(branch == null) { orphansByParent.add(vertex.transaction.branch, vertex); } else { vertex.setBranch(branch); }
        if(trunk == null) { orphansByParent.add(vertex.transaction.trunk, vertex); } else { vertex.setTrunk(trunk); }

        List<Vertex> children = orphansByParent.get(vertex.transaction.hash);
        for(Vertex child : children)
            vertex.addChild(child);
    }

    public Vertex getVertex(Transaction transaction) {
        return findVertexByHash(transaction.hash);
    }

    public Vertex findVertexByHash(String hash) {
        return verticesByHash.get(hash);
    }
}
