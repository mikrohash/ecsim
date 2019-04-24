package org.iota.ecsim.ec;

import org.iota.ecsim.Constants;
import org.iota.ecsim.ict.network.Node;
import org.iota.ecsim.ict.tangle.DoubleCone;
import org.iota.ecsim.utils.Trytes;
import org.iota.ecsim.ict.tangle.Vertex;

public class Actor {

    public final String address = Trytes.randomHash();
    private final Node node;

    public Actor(Node node) {
        this.node = node;
    }

    /**
     * Does a random walk to find a tip and issues a marker to that tip if the past cone of that tip can be validated.
     * */
    public boolean issueMarker() {
        String tip = node.getCluster().doRandomWalk(node.getTangle());
        System.out.println(tip);
        boolean ispastConeValid = isPastConeValid(tip);
        if(ispastConeValid)
            issueMarker(tip);
        return ispastConeValid;
    }

    /**
     * Validates a Tangle to a certain depth
     * @return false if past cone is invalid or incomplete.
     * */
    public boolean isPastConeValid(String tip) {
        Vertex apex = node.getTangle().findVertexByHash(tip);
        // depth-1 to account for marker
        DoubleCone doubleCone = new DoubleCone(apex, Constants.DOUBLE_CONE_DEPTH-1, Constants.DOUBLE_CONE_HEIGHT);
        return doubleCone.isComplete() && doubleCone.isConfident(node.getCluster());
    }

    public Marker issueMarker(String ref1) {
        return issueMarker(ref1, ref1);
    }

    public Marker issueMarker(String ref1, String ref2) {
        Marker marker = new Marker(this, ref1, ref2);
        node.submit(marker);
        return marker;
    }
}
