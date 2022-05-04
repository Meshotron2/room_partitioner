package com.github.meshotron2.room_partitioner.partitioner;

import java.util.ArrayList;
import java.util.List;

/**
 * A very basic / barebones Tree implementation in Java.
 */
public class Node {
    private final ArrayList<Node> children;
    private final int value;

    public Node(int value) {
        this.children = new ArrayList<Node>();
        this.value = value;
    }

    public List<Node> getChildren() {
        return children;
    }

    public int getValue() {
        return value;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public int getNumLeafs() {
        if (this.children.size() == 0) return 1;

        int sum = 0;
        for (Node child : this.children) {
            sum += child.getNumLeafs();
        }

        return sum;
    }

    public List<Node> getLeafs() {
        List<Node> leafs = new ArrayList<Node>();

        this.getLeafsInternal(leafs);

        return leafs;
    }

    private void getLeafsInternal(List<Node> leafs) {
        if (this.children.size() == 0) {
            leafs.add(this);
            return;
        }

        for (Node child : this.children) {
            child.getLeafsInternal(leafs);
        }
    }
}