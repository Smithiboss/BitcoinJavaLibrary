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
        // calculate max depth
        this.maxDepth = (int) Math.ceil(Helper.log(total, 2));
        this.nodes = new ArrayList<>();
        // initialize the tree with null nodes for every node on every tree level
        for (int depth = 0; depth < maxDepth + 1; depth++) {
            // calculate the number of nodes at this depth
            var numItems = (int) Math.ceil(this.total / Math.pow(2, this.maxDepth - depth));
            // initialize the list of nodes at this depth with null nodes
            List<byte[]> levelHashes = new ArrayList<>(Collections.nCopies(numItems, null));
            // add the list of nodes at this depth to the list of all nodes at all depths
            nodes.add(levelHashes);
        }
        
        currentDepth = 0;
        currentIndex = 0;
    }

    /**
     * Move up a tree level
     */
    public void up() {
        currentDepth -= 1;
        currentIndex /= 2;
    }

    /**
     * Move to the left child of the current node
     */
    public void left() {
        currentDepth += 1;
        currentIndex *= 2;
    }

    /**
     * Move to the right child of the current node
     */
    public void right() {
        currentDepth += 1;
        currentIndex = currentIndex * 2 + 1;
    }

    /**
     * Returns the root of the tree
     * @return a {@code byte} array
     */
    public byte[] root() {
        return nodes.getFirst().getFirst();
    }

    /**
     * Sets the current node to the given value
     * @param value a {@code byte} array
     */
    public void setCurrentNode(byte[] value) {
        nodes.get(currentDepth).set(currentIndex, value);
    }

    /**
     * Returns the current node
     * @return a {@code byte} array
     */
    public byte[] getCurrentNode() {
        return nodes.get(currentDepth).get(currentIndex);
    }

    /**
     * Returns the left child of the current node
     * @return a {@code byte} array
     */
    public byte[] getLeftNode() {
        return nodes.get(currentDepth + 1).get(currentIndex * 2);
    }

    /**
     * Returns the right child of the current node
     * @return a {@code byte} array
     */
    public byte[] getRightNode() {
        return nodes.get(currentDepth + 1).get(currentIndex * 2 + 1);
    }

    /**
     * Returns whether the current node is a leaf node
     * @return a {@code boolean}
     */
    public boolean isLeaf() {
        return currentDepth == maxDepth;
    }

    /**
     * Returns whether the right child of the current node exists
     * @return a {@code boolean}
     */
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
        for (int i = flagBitsArray.length - 1; i >= 0; i--) {
            flagBits.push(flagBitsArray[i]);
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
            throw new RuntimeException("Not all hashes were consumed");
        }
        for (Byte flagBit : flagBits) {
            if (flagBit != 0) {
                throw new RuntimeException("Not all flag bits were consumed");
            }
        }
    }
}
