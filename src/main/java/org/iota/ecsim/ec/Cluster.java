package org.iota.ecsim.ec;

import org.iota.ecsim.Constants;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.tangle.DoubleCone;
import org.iota.ecsim.ict.tangle.Tangle;
import org.iota.ecsim.ict.tangle.Vertex;
import org.iota.ecsim.utils.Trytes;

import java.util.*;

public class Cluster {

    private Set<TrustedActor> actors = new HashSet<>();

    public double calcConfidence(String transactionHash) {
        double confidence = 0;
        for(TrustedActor actor : actors)
            confidence += actor.calcConfidence(transactionHash);
        double trustSum =  Math.max(calcTrustSum(), 1E-80); // >0 to avoid division by zero;
        return confidence / trustSum;
    }

    public void addActor(TrustedActor actor) {
        actors.add(actor);
    }

    public void process(Vertex vertex) {
        for (TrustedActor actor : actors)
            actor.process(vertex);
    }

    private double calcTrustSum() {
        double trustSum = 0;
        for(TrustedActor actor : actors)
            trustSum += actor.trust;
        return trustSum;
    }

    public String doRandomWalk(Tangle tangle) {
        return doRandomWalk(tangle, new LinkedList<>());
    }

    public String doRandomWalk(Tangle tangle, List<String> walkLogger) {
        String startingParticleHash = getRandomConfirmedTransactionHash();
        Vertex particle = tangle.findVertexByHash(startingParticleHash);
        List<Vertex> transitions;
        while ((transitions = particle.getChildren()).size() > 0) {
            if(particle.transaction == Transaction.NULL_TRANSACTION) transitions.remove(particle);
            if(walkLogger != null) walkLogger.add(particle.transaction.hash);
            particle = walkRandomly(transitions);
        }
        if(walkLogger != null)  walkLogger.add(particle.transaction.hash);
        return particle.transaction.hash;
    }

    private String getRandomConfirmedTransactionHash() {
        String randomTransactionHash;
        int tries = 0;
        do {
            if(tries++ >= 100) return Transaction.NULL_TRANSACTION.hash;
            TrustedActor randomActor = new LinkedList<>(actors).get((int)(Math.random() * actors.size()));
            DoubleCone randomDoubleCone = randomActor.getDoubleConeOfRandomMarker();
            randomTransactionHash = randomDoubleCone == null ? "" : randomDoubleCone.getRandomFromPast();
        } while (calcConfidence(randomTransactionHash) < Constants.CONFIRMATION_CONFIDENCE);
        return randomTransactionHash;
    }

    private Vertex walkRandomly(List<Vertex> transitions) {
        Map<Vertex, Double> transitionProbabilities = calcTransitionProbabilities(transitions);
        double random = Math.random();
        for (Map.Entry<Vertex, Double> transitionWithProbability : transitionProbabilities.entrySet()) {
            double probability = transitionWithProbability.getValue();
            if(random <= probability)
                return transitionWithProbability.getKey();
            random -= probability;
        }
        // suspected reasons that no transition was found: either transitions.size() == 0 or sum(transitionProbabilities) < 1
        throw new IllegalStateException("No transition found: " + (transitions.size() == 0 ? "no transition available." : "invalid transition probabilities (sum < 1)."));
    }

    private Map<Vertex, Double> calcTransitionProbabilities(List<Vertex> transitions) {
        Map<Vertex, Double> transitionProbabilities = calcTransitionWeights(transitions);
        normalizeTransitionProbabilities(transitionProbabilities);
        return transitionProbabilities;
    }

    private Map<Vertex, Double> calcTransitionWeights(List<Vertex> transitions) {
        Map<Vertex, Double> transitionWeights = new HashMap<>();
        for(Vertex transition : transitions)
            transitionWeights.put(transition, calcWeight(transition));
        return transitionWeights;
    }

    private double calcWeight(Vertex vertex) {
        // intermediate solution: does not make sense but ensures convergence
        return Trytes.TRYTES.indexOf(vertex.transaction.hash.charAt(0))+1;
    }

    private static void normalizeTransitionProbabilities(Map<Vertex, Double> transitionWeights) {
        double sum = 0;
        for(double weight : transitionWeights.values())
            sum += weight;
        for(Vertex key : transitionWeights.keySet())
            transitionWeights.put(key, transitionWeights.get(key) / sum);
    }
}
