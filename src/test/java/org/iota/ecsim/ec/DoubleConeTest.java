package org.iota.ecsim.ec;

import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.utils.StringLongMap;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.model.Transfer;
import org.iota.ecsim.ict.tangle.DoubleCone;
import org.iota.ecsim.ict.tangle.Tangle;
import org.iota.ecsim.ict.tangle.Vertex;
import org.iota.ecsim.utils.Trytes;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DoubleConeTest extends TestTemplate {

    @Test
    public void testPastBuilding() {
        Tangle tangle = new Tangle();

        Transaction t4 = createReferenceOf(Transaction.NULL_TRANSACTION.hash);
        Transaction t3 = createReferenceOf(t4.hash);
        Transaction t2 = createReferenceOf(t3.hash);
        Transaction t1 = createReferenceOf(t2.hash);
        Transaction t0 = createReferenceOf(t1.hash);

        tangle.addTransaction(t2);
        tangle.addTransaction(t1);
        tangle.addTransaction(t0);

        Vertex apex = tangle.findVertexByHash(t0.hash);
        DoubleCone doubleCone = new DoubleCone(apex,3, 0);

        assertTrue("Double cone misses transaction", doubleCone.pastContains(t0.hash));
        assertTrue("Double cone misses transaction", doubleCone.pastContains(t1.hash));
        assertTrue("Double cone misses transaction", doubleCone.pastContains(t2.hash));
        assertTrue("Double cone misses transaction", doubleCone.pastContains(Transaction.NULL_TRANSACTION.hash));
        assertFalse("Double cone states it is complete but transactions are missing.", doubleCone.isComplete());

        // add these a little later
        tangle.addTransaction(t4);
        tangle.addTransaction(t3);
        doubleCone.process(tangle.findVertexByHash(t3.hash));

        assertTrue("Double cone misses transaction", doubleCone.pastContains(t3.hash));
        assertFalse("Double cone exceeds depth.", doubleCone.pastContains(t4.hash));
        assertTrue("Double cone states it is incomplete but all transactions were submitted.", doubleCone.isComplete());
    }

    @Test
    public void testContainsEntireBundle() {

        Tangle tangle = new Tangle();

        StringLongMap balances = new StringLongMap();
        for(int i = 0; i < 10; i++)
            balances.add(Trytes.randomHash(), (long)0);

        List<Transaction> transactionsT2H = Transfer.build(balances);
        for(Transaction transaction : transactionsT2H)
            tangle.addTransaction(transaction);

        Transaction head = transactionsT2H.get(transactionsT2H.size()-1);
        DoubleCone doubleCone = new DoubleCone(tangle.findVertexByHash(head.hash), 3, 0);

        for(Transaction transaction : transactionsT2H)
            assertTrue("Double cone misses transaction", doubleCone.pastContains(transaction.hash));
    }
}