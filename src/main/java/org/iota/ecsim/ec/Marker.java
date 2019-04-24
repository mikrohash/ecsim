package org.iota.ecsim.ec;

import org.iota.ecsim.ict.model.Transaction;

public class Marker extends Transaction {

    public Marker(Actor issuer, String ref1, String ref2) {
        super(issuer.address, ref1, ref2);
    }
}
