package org.iota.ecsim.ec;

import org.iota.ecsim.Constants;
import org.iota.ecsim.ict.tangle.DoubleCone;
import org.iota.ecsim.ict.tangle.Vertex;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TrustedActor {

    public final String address;
    public final double trust;

    private Map<Vertex, DoubleCone> markersWithDoubleCones = new HashMap<>();

    public TrustedActor(String address, double trust) {
        this.address = address;
        this.trust = trust;
    }

    public double calcConfidence(String transactionHash) {
        double confidence = 0;
        for(DoubleCone doubleCone : markersWithDoubleCones.values())
            if(doubleCone.pastContains(transactionHash))
                confidence++;
        return confidence / Math.max(markersWithDoubleCones.values().size(), 1E-80);
    }

    public void process(Vertex vertex) {
        if(vertex.transaction.address.equals(address) && vertex.transaction instanceof Marker) {
            processMarker(vertex);
        }
    }

    private void processMarker(Vertex markerVertex) {
        markersWithDoubleCones.put(markerVertex, new DoubleCone(markerVertex, Constants.DOUBLE_CONE_DEPTH, Constants.DOUBLE_CONE_HEIGHT));
    }

    public DoubleCone getDoubleConeOfRandomMarker() {
        if(markersWithDoubleCones.size() == 0)
            return null;
        Vertex randomMarker = new LinkedList<>(markersWithDoubleCones.keySet()).get((int)(Math.random() * markersWithDoubleCones.size()));
        return markersWithDoubleCones.get(randomMarker);
    }
}