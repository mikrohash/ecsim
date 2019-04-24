package org.iota.ecsim.ict;

import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.ict.tangle.Vertex;
import org.iota.ecsim.utils.Trytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class TangleTest extends TestTemplate {

    @Test
    public void testVertexBuilding() {
        Node node = newNode();

        Vertex nullVertex = node.getTangle().findVertexByHash(Trytes.NULL_HASH);
        assertNotNull("Could not find vertex of NULL transaction.", nullVertex);

        Transaction reference = createReferenceOf(nullVertex);
        Transaction metaReference = createReferenceOf(reference.hash);

        node.submit(metaReference);
        node.submit(reference);

        Vertex referenceVertex = node.getTangle().findVertexByHash(reference.hash);
        assertNotNull("Reference was not added to Tangle.", referenceVertex);

        Vertex metaReferenceVertex = node.getTangle().findVertexByHash(metaReference.hash);
        assertNotNull("Meta reference was not added to Tangle.", metaReferenceVertex);

        assertReference(referenceVertex, nullVertex);
        assertReference(metaReferenceVertex, referenceVertex);
    }

    private static void assertReference(Vertex referencing, Vertex referenced) {
        assertTrue("Referenced vertex does not show reference.", referenced.getChildren().contains(referencing));
        assertTrue("Referencing vertex does not show reference.", referencing.getBranch() == referenced || referencing.getTrunk() == referenced);
    }
}