package org.iota.ecsim.ict.tangle;

import org.iota.ecsim.Constants;
import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.utils.Trytes;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DoubleConeDagTest extends TestTemplate {

    @Test
    public void tesCountReferencing() {
        Tangle tangle = new Tangle();

        DoubleCone coneA = createDCRefOf(tangle, Transaction.NULL_TRANSACTION, 1);
        DoubleCone coneB = createDCRefOf(tangle, coneA.apex.transaction, 1);
        DoubleCone coneC = createDCRefOf(tangle, Transaction.NULL_TRANSACTION, 1);
        DoubleCone coneD = createDCRefOf(tangle, coneB.apex.transaction, coneC.apex.transaction, 1);
        DoubleCone coneE = createDCRefOf(tangle, Trytes.randomHash(), Trytes.randomHash(), 1);
        DoubleCone coneF = createDCRefOf(tangle, coneA.apex.transaction, coneD.apex.transaction, 5);

        DoubleConeDag dcDag = new DoubleConeDag(Arrays.asList(coneA, coneB, coneC, coneD, coneE, coneF));

        assertReferencingCount(dcDag, coneF, 1);
        assertReferencingCount(dcDag, coneE, 1);
        assertReferencingCount(dcDag, coneD, 2);
        assertReferencingCount(dcDag, coneC, 3);
        assertReferencingCount(dcDag, coneB, 3);
        assertReferencingCount(dcDag, coneA, 4);
        assertReferencingCount(dcDag, Transaction.NULL_TRANSACTION, 5);
    }

    private static void assertReferencingCount(DoubleConeDag dcDag, DoubleCone doubleCone, int expectedCount) {
        assertReferencingCount(dcDag, doubleCone.apex.transaction, expectedCount);
    }

    private static void assertReferencingCount(DoubleConeDag dcDag, Transaction transaction, int expectedCount) {
        assertEquals("Unexpected amount of double cones referencing transaction " + transaction.hash, expectedCount, dcDag.countReferencing(transaction.hash));
    }

    private static DoubleCone createDCRefOf(Tangle tangle, Transaction reference, int depth) {
        return createDCRefOf(tangle, reference.hash, depth);
    }

    private static DoubleCone createDCRefOf(Tangle tangle, String reference, int depth) {
        return createDCRefOf(tangle, reference, Transaction.NULL_TRANSACTION.hash, depth);
    }

    private static DoubleCone createDCRefOf(Tangle tangle, Transaction reference1, Transaction reference2, int depth) {
        return createDCRefOf(tangle, reference1.hash, reference2.hash, depth);
    }

    private static DoubleCone createDCRefOf(Tangle tangle, String reference1, String reference2, int depth) {
        Transaction referencing = createReferenceOf(reference1, reference2);
        tangle.addTransaction(referencing);
        Vertex apex = tangle.findVertexByHash(referencing.hash);
        return new DoubleCone(apex, depth, Constants.DOUBLE_CONE_HEIGHT);
    }
}