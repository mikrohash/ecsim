package org.iota.ecsim.ict.network;

import org.iota.ecsim.ec.Cluster;
import org.iota.ecsim.ict.tangle.Tangle;
import org.iota.ecsim.ict.model.Transaction;
import org.iota.ecsim.ict.tangle.Vertex;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Node implements Runnable {

    private BlockingQueue<Message> incomingMessages = new LinkedBlockingDeque<>();

    private final Tangle tangle = new Tangle();
    private List<Node> neighbors = new LinkedList<>();
    private Set<String> requests = new HashSet<>();
    private final Cluster cluster = new Cluster();
    private boolean isProcessingMessage = false;

    private Thread runningThread = new Thread(this);

    public Node() {
        runningThread.start();
    }

    @Override
    public void run() {
        try {
            while (runningThread.isAlive()) {
                Message message = incomingMessages.take();
                processMessage(message);
            }
        } catch (InterruptedException e) {
            if(!runningThread.isAlive())
                throw new RuntimeException(e);
        }
    }

    public void terminate() {
        try {
            do {
                runningThread.interrupt();
                Thread.sleep(1);
            } while (runningThread.isAlive());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void neighbor(Node nodeA, Node nodeB) {
        nodeA.neighbors.add(nodeB);
        nodeB.neighbors.add(nodeA);
    }

    private void receiveMessage(Message message) {
        incomingMessages.add(message);
    }

    private void processMessage(Message message) {
        isProcessingMessage = true;
        requests.remove(message.requestHash);
        Vertex newVertex = tangle.addTransaction(message.transaction);
        answerRequest(message);
        if(newVertex != null) {
            cluster.process(newVertex);
            forwardMessage(message);
        }
        isProcessingMessage = false;
    }

    private void answerRequest(Message message) {
        if(message.requestHash != null) {
            Vertex requested = tangle.findVertexByHash(message.requestHash);
            if(requested != null)
                sendMessageToNeighbor(message.sender, requested.transaction, getRequest());
        }
    }

    private String getRequest() {
        List<String> asList = new LinkedList<>(requests);
        Collections.shuffle(asList);
        return asList.size() == 0 ? null : asList.get(0);
    }

    public void submit(Transaction transaction) {
        Vertex vertex = tangle.addTransaction(transaction);
        if(vertex != null) {
            for(Node neighbor : neighbors)
                sendMessageToNeighbor(neighbor, transaction, getRequest());
        }
        cluster.process(vertex);
    }

    private void forwardMessage(Message message) {
        for(Node neighbor : neighbors)
            if(neighbor != message.sender)
                sendMessageToNeighbor(neighbor, message.transaction, getRequest());
    }

    private void sendMessageToNeighbor(Node neighbor, Transaction transaction, String requestHash) {
        Message message = new Message(this, transaction, requestHash);
        neighbor.receiveMessage(message);
    }

    public Tangle getTangle() {
        return tangle;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public boolean finishedProcessing() {
        return incomingMessages.isEmpty() && !isProcessingMessage;
    }
}
