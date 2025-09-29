package org.example.spv;

import org.example.ecc.Int;
import org.example.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

    private final int total;
    private final int maxDepth;
    private final List<Int[]> nodes;
    private int currentDepth;
    private int currentIndex;

    public MerkleTree(int total) {
        this.total = total;
        this.maxDepth = (int) Math.ceil(Helper.log(total, 2));
        this.nodes = new ArrayList<>();

        for (int depth = 0; depth < maxDepth + 1; depth++) {
            var numItems = (int) Math.ceil(this.total / Math.pow(2, this.maxDepth - depth));
            var levelHashes = new Int[numItems];
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

    public Int root() {
        return nodes.getFirst()[0];
    }

    public void setCurrentNode(Int value) {
        nodes.get(currentDepth)[currentIndex] = value;
    }

    public Int getCurrentNode() {
        return nodes.get(currentDepth)[currentIndex];
    }

    public Int getLeftNode() {
        return nodes.get(currentDepth + 1)[currentIndex * 2];
    }

    public Int getRightNode() {
        return nodes.get(currentDepth + 1)[currentIndex * 2 + 1];
    }

    public boolean isLeaf() {
        return currentDepth == maxDepth;
    }

    public boolean rightExists() {
        return nodes.get(currentDepth + 1).length > currentIndex * 2 + 1;
    }



}
