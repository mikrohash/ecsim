package org.iota.ecsim.ec;

import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.utils.StringLongMap;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.model.Transfer;
import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.utils.Trytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ActorTest extends TestTemplate {

    @Test
    public void testInterNodeSync() {
        initNodes(2);
        Node.neighbor(a, b);

        Actor actor = new Actor(a);
        TrustedActor trustedActor = new TrustedActor(actor.address, 1);
        b.getCluster().addActor(trustedActor);

        Marker marker = actor.issueMarker(Transaction.NULL_TRANSACTION.hash);
        sleepUntilCommunicationEnds();

        assertEquals("Unexpected confidence.", 1.0, b.getCluster().calcConfidence(marker.hash), 0.01);
        assertEquals("Unexpected confidence.", 1.0, b.getCluster().calcConfidence(Transaction.NULL_TRANSACTION.hash), 0.01);
    }

    @Test
    public void validateBalance() {
        Node node = newNode();
        Actor actor = new Actor(node);
        TrustedActor trustedActor = new TrustedActor(actor.address, 1);
        node.getCluster().addActor(trustedActor);

        String address = Trytes.randomHash();
        long balance = 1000;

        StringLongMap genesis = new StringLongMap();
        genesis.add(Trytes.randomHash(), -balance);
        genesis.add(address, balance);
        Transaction headGenesis = issueTransfer(node, genesis, Collections.singletonList(Transaction.NULL_TRANSACTION.hash));

        assertSpendValidity(node, actor, address, balance, headGenesis.hash,false);

        // fails because last assertSpendValidity() call replaced tip with tip spanning up an invalid sub-tangle TODO consider validity in random walk

        actor.issueMarker();
        assertEquals("Unexpected confidence.",1.0, node.getCluster().calcConfidence(headGenesis.hash), 1E-3);

        assertSpendValidity(node, actor, address, balance, headGenesis.hash,true);
        assertSpendValidity(node, actor, address, balance, Transaction.NULL_TRANSACTION.hash,false);
        assertSpendValidity(node, actor, address, balance+1, headGenesis.hash,false);
    }

    private void assertSpendValidity(Node node, Actor actor, String address, long fundsSpent, String tip, boolean expectedValidity) {
        StringLongMap spend = new StringLongMap();
        spend.add(address, -fundsSpent);
        spend.add(Trytes.randomHash(), fundsSpent);
        Transaction headValidSpend = issueTransfer(node, spend, Collections.singletonList(tip));
        sleepUntilCommunicationEnds();
        boolean actualValidity = actor.isPastConeValid(headValidSpend.hash);
        assertEquals("Unexpected validity of transfer.", expectedValidity, actualValidity);
    }

    /**
     * Submits a transfer to the network.
     * @return Head transaction.
     * */
    private Transaction issueTransfer(Node node, StringLongMap balances, List<String> tips) {
        List<Transaction> transactionsT2H = Transfer.build(balances, tips);
        for(Transaction transaction : transactionsT2H)
            node.submit(transaction);
        Transaction head = transactionsT2H.get(transactionsT2H.size()-1);
        Assert.assertTrue("Bundle head is not a head transaction.", head.isBundleHead);
        return head;
    }
}