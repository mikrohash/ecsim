package org.iota.ecsim.ict.model;

import org.iota.ecsim.utils.Trytes;

public class Transaction {

    public final boolean isBundleHead, isBundleTail;
    public final long value;
    public final String address, trunk, branch, hash;

    public static final Transaction NULL_TRANSACTION = new Transaction();

    public Transaction(String address, String trunk, String branch) {
        this.address = address;
        this.trunk = trunk;
        this.branch = branch;
        this.isBundleHead = true;
        this.isBundleTail = true;
        this.value = 0;
        this.hash = Trytes.randomHash();
        assertValidity();
    }

    public Transaction(String address, String trunk, String branch, long value, boolean isBundleHead, boolean isBundleTail) {
        this.address = address;
        this.trunk = trunk;
        this.branch = branch;
        this.isBundleHead = isBundleHead;
        this.isBundleTail = isBundleTail;
        this.value = value;
        this.hash = Trytes.randomHash();
        assertValidity();
    }

    private Transaction() {
        this.isBundleHead = true;
        this.isBundleTail = true;
        this.value = 0;
        this.address = Trytes.NULL_HASH;
        this.trunk = Trytes.NULL_HASH;
        this.branch = Trytes.NULL_HASH;
        this.hash = Trytes.NULL_HASH;
        assertValidity();
    }

    private void assertValidity() {
        if(address == null) throw new NullPointerException("address is null");
        if(trunk == null) throw new NullPointerException("trunk is null");
        if(branch == null) throw new NullPointerException("branch is null");
    }
}