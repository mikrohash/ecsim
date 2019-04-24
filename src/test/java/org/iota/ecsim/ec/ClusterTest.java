package org.iota.ecsim.ec;

import org.iota.ecsim.TestTemplate;
import org.iota.ecsim.utils.StringLongMap;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.utils.Trytes;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ClusterTest extends TestTemplate {

    @Test
    public void testRandomWalk() {

        Node node = newNode();
        Cluster cluster = node.getCluster();
        Actor actor = new Actor(node);
        cluster.addActor(new TrustedActor(actor.address, 1));

        // confirms two transactions as starting points for random walk
        Transaction marker = actor.issueMarker(Transaction.NULL_TRANSACTION.hash);

        // all random walks must end at this tip.
        Transaction tip1 = new Transaction(Trytes.randomHash(), Transaction.NULL_TRANSACTION.hash, marker.hash);
        Transaction tip2 = new Transaction(Trytes.randomHash(), marker.hash, marker.hash);
        node.submit(tip1);
        node.submit(tip2);

        Set<String> allowedWalks = new HashSet<>();

        // walks to tip 1
        allowedWalks.add(walkToString(Transaction.NULL_TRANSACTION.hash, tip1.hash));
        allowedWalks.add(walkToString(Transaction.NULL_TRANSACTION.hash, marker.hash, tip1.hash));
        allowedWalks.add(walkToString(marker.hash, tip1.hash));

        // walks to tip 2
        allowedWalks.add(walkToString(Transaction.NULL_TRANSACTION.hash, marker.hash, tip2.hash));
        allowedWalks.add(walkToString(marker.hash, tip2.hash));

        StringLongMap walkCounter = new StringLongMap();

        for(int i = 0; i < 1000 && walkCounter.keySet().size() < allowedWalks.size(); i++) {
            LinkedList<String> walkLogger = new LinkedList<>();
            String tip = cluster.doRandomWalk(node.getTangle(), walkLogger);
            String walk = walkToString(walkLogger);
            walkCounter.add(walk, 1);
            assertEquals("Tip not last element of walk logger", tip, walkLogger.getLast());
            assertTrue("Unexpected walk: " + walk, allowedWalks.contains(walk));
        }

        for(String walk : allowedWalks) {
            assertNotEquals("Walk " + walk + " was never walked.", 0, walkCounter.get(walk));
        }
    }

    private static String walkToString(String ... walk) {
        return walkToString(Arrays.asList(walk));
    }

    private static String walkToString(Iterable<String> walk) {
        StringJoiner stringJoiner = new StringJoiner("->");
        for(String particle : walk)
            stringJoiner.add(particle);
        return stringJoiner.toString();
    }
}