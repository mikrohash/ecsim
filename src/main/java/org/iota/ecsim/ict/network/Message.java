package org.iota.ecsim.ict.network;

import org.iota.ecsim.ict.model.Transaction;

public class Message {

    public final Node sender;
    public final Transaction transaction;
    public final String requestHash;

    public Message(Node sender, Transaction transaction, String requestHash) {
        this.sender = sender;
        this.transaction = transaction;
        this.requestHash = requestHash;
    }
}
