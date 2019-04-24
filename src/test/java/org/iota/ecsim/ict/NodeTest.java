package org.iota.ecsim.ict;

import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.utils.Trytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTest extends TestTemplate {

    @Test
    public void testDirectCommunication() {

        initNodes(3);
        Node.neighbor(a, b);
        Node.neighbor(b, c);

        Transaction t1 = submitSimpleTransaction(a);
        Transaction t2 = submitSimpleTransaction(b);
        sleepUntilCommunicationEnds();

        assertTransactionReceived(b, t1);
        assertTransactionReceived(c, t1);
        assertTransactionReceived(a, t2);
        assertTransactionReceived(c, t2);
    }

    private static void assertTransactionReceived(Node node, Transaction transaction) {
        assertNotNull("Neighbor did not receive transaction.", node.getTangle().getVertex(transaction));
    }

    private static Transaction submitSimpleTransaction(Node node) {
        Transaction transaction = new Transaction(Trytes.randomHash(), Trytes.NULL_HASH, Trytes.NULL_HASH);
        node.submit(transaction);
        return transaction;
    }
}