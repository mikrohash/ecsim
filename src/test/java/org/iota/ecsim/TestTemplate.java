package org.iota.ecsim;

import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.utils.Trytes;
import org.iota.ecsim.ict.tangle.Vertex;
import org.junit.After;

import java.util.LinkedList;
import java.util.List;

public class TestTemplate {

    protected Node a, b, c;
    protected List<Node> runningNodes = new LinkedList<>();

    protected static void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sleepUntilCommunicationEnds() {
        boolean stillCommunicating;
        do {
            stillCommunicating = false;
            for(Node node : runningNodes)
                if(!node.finishedProcessing()) {
                    stillCommunicating = true;
                    safeSleep(1);
                    break;
                }
        } while (stillCommunicating);
    }

    protected void initNodes(int amount) {
        if(amount > 0) a = newNode();
        if(amount > 0) b = newNode();
        if(amount > 0) c = newNode();
    }

    public Node newNode() {
        Node node = new Node();
        runningNodes.add(node);
        return node;
    }

    @After
    public void cleanUp() {
        for(Node node : runningNodes)
            node.terminate();
        runningNodes = new LinkedList<>();
        a = null;
        b = null;
        c = null;
    }

    protected static Transaction createReferenceOf(Vertex vertex) {
        return createReferenceOf(vertex.transaction.hash);
    }

    protected static Transaction createReferenceOf(Transaction transaction) {
        return createReferenceOf(transaction.hash);
    }

    protected static Transaction createReferenceOf(String reference) {
        return createReferenceOf(reference, Transaction.NULL_TRANSACTION.hash);
    }

    protected static Transaction createReferenceOf(String reference1, String reference2) {
        boolean Reference1ByTrunk = Math.random() < 0.5;
        String trunk = Reference1ByTrunk ? reference1 : reference2;
        String branch = Reference1ByTrunk ? reference2 : reference1;
        return new Transaction(Trytes.randomHash(), trunk, branch);
    }
}
