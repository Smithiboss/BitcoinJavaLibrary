package org.example.spv;

import org.example.utils.Helper;

import java.util.*;

public class MerkleTree {

    private final int total;
    private final int maxDepth;
    private final List<List<byte[]>> nodes;
    private int currentDepth;
    private int currentIndex;

    public MerkleTree(int total) {
        this.total = total;
        this.maxDepth = (int) Math.ceil(Helper.log(total, 2));
        this.nodes = new ArrayList<>();

        for (int depth = 0; depth < maxDepth + 1; depth++) {
            var numItems = (int) Math.ceil(this.total / Math.pow(2, this.maxDepth - depth));
            List<byte[]> levelHashes = new ArrayList<>(Collections.nCopies(numItems, null));
            nodes.add(levelHashes);
        }
        
        currentDepth = 0;
        currentIndex = 0;
    }

    public void up() {
        currentDepth -= 1;
        currentIndex /= 2;
    }

    public void left() {
        currentDepth += 1;
        currentIndex *= 2;
    }

    public void right() {
        currentDepth += 1;
        currentIndex = currentIndex * 2 + 1;
    }

    public byte[] root() {
        return nodes.getFirst().getFirst();
    }

    public void setCurrentNode(byte[] value) {
        nodes.get(currentDepth).set(currentIndex, value);
    }

    public byte[] getCurrentNode() {
        return nodes.get(currentDepth).get(currentIndex);
    }

    public byte[] getLeftNode() {
        return nodes.get(currentDepth + 1).get(currentIndex * 2);
    }

    public byte[] getRightNode() {
        return nodes.get(currentDepth + 1).get(currentIndex * 2 + 1);
    }

    public boolean isLeaf() {
        return currentDepth == maxDepth;
    }

    public boolean rightExists() {
        return nodes.get(currentDepth + 1).size() > currentIndex * 2 + 1;
    }

    /**
     * Populates the tree with given hashes using flag bits
     * @param flagBitsArray a {@code byte} array
     * @param hashesList a {@code byte} array
     */
    public void populateTree(byte[] flagBitsArray, List<byte[]> hashesList) {
        Deque<Byte> flagBits = new ArrayDeque<>();
        for (byte b : flagBitsArray) {
            flagBits.push(b);
        }
        Deque<byte[]> hashes = new ArrayDeque<>(hashesList);

        while (root() == null) {
            if (isLeaf()) {
                flagBits.pop();
                setCurrentNode(hashes.pop());
                up();
            } else {
                var leftHash = getLeftNode();
                if (leftHash == null) {
                    if (flagBits.pop() == 0) {
                        setCurrentNode(hashes.pop());
                        up();
                    } else {
                        left();
                    }
                } else if (rightExists()) {
                    var rightHash = getRightNode();
                    if (rightHash == null) {
                        right();
                    } else {
                        setCurrentNode(Helper.merkleParent(leftHash, rightHash));
                        up();
                    }
                } else {
                    setCurrentNode(Helper.merkleParent(leftHash, leftHash));
                    up();
                }
            }
        }
        if (!hashes.isEmpty()) {
            throw new IllegalStateException("Not all hashes were consumed");
        }
        for (Byte flagBit : flagBits) {
            if (flagBit != 0) {
                throw new IllegalStateException("Not all flag bits were consumed");
            }
        }
    }



}
