package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PartNode {
    private int value;
    private PartNode[] children;
    private int maxChildCnt;
    private int childCnt;

    private int leafs = -1;

    PartNode(int value, PartNode[] children, int childCnt, int maxChildCnt) {
        this.value = value;
        this.children = children;
        this.childCnt = childCnt;
        this.maxChildCnt = maxChildCnt;
    }

    void setChildCnt(int childCnt) {
        this.children = new PartNode[childCnt];
        this.maxChildCnt = childCnt;
        this.childCnt = 0;
    }

    void setValue(int value) {
        this.value = value;
    }

    PartNode addChild(PartNode node) {
        if (childCnt == maxChildCnt)
            return null;

        children[childCnt++] = node;
        return children[childCnt];
    }

    int getNumLeafs() {
        if (children == null)
            return 1;

        int sum = 0;

        for (int i = 0; i < childCnt; i++)
            sum += children[i].getNumLeafs();

        return sum;
    }

    private void getLeafsInternal(PartNode[] buffer) {
        if (leafs == -1)
            leafs = 0;

        if (children == null) {
            buffer[leafs] = this;
            leafs++;
            return;
        }

        for (int i = 0; i < childCnt; i++) {
            children[i].getLeafsInternal(buffer);
        }
    }

    PartNode[] getLeafs() {
        final int numLeafs = getNumLeafs();
        PartNode[] leafs = new PartNode[numLeafs];

        this.leafs = 0;
        getLeafsInternal(leafs);

        return leafs;
    }

    public int getValue() {
        return this.value;
    }
}

class PartTree {
    private final PartNode x;
    private final PartNode y;
    private final PartNode z;

    PartTree(PartNode x, PartNode y, PartNode z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private int findNextRoot(int n) {
        int r = 2;
        while (n % r != 0)
            r++;

        return r;
    }

    public void partition(int n) {
        if (n < 2) return;

        int r = findNextRoot(n);

        final PartTree[] pt = new PartTree[r];

        if (x.getValue() >= y.getValue()) {
            if (x.getValue() >= z.getValue()) {
                int rem = x.getValue() % r;
                x.setChildCnt(r);
                for (int i = 0; i < r; i++) {
                    int value;
                    if (rem > 0) {
                        value = x.getValue() / r + 1;
                        rem--;
                    } else {
                        value = x.getValue() / r;
                    }

                    final PartNode node = new PartNode(value, null, 0, 0);
                    final PartNode newNode = x.addChild(node);

                    final PartTree t = new PartTree(newNode, y, z);
                    pt[i] = t;
                }
            } else {
                int rem = z.getValue() % r;
                z.setChildCnt(r);
                for (int i = 0; i < r; i++) {
                    int value;
                    if (rem > 0) {
                        value = z.getValue() / r + 1;
                        rem--;
                    } else {
                        value = z.getValue() / r;
                    }

                    final PartNode node = new PartNode(value, null, 0, 0);
                    final PartNode newNode = z.addChild(node);

                    final PartTree t = new PartTree(x, y, newNode);
                    pt[i] = t;
                }
            }
        } else {
            if (x.getValue() >= z.getValue() || y.getValue() >= z.getValue()) {
                int rem = y.getValue() % r;
                y.setChildCnt(r);
                for (int i = 0; i < r; i++) {
                    int value;
                    if (rem > 0) {
                        value = y.getValue() / r + 1;
                        rem--;
                    } else {
                        value = y.getValue() / r;
                    }

                    final PartNode node = new PartNode(value, null, 0, 0);
                    final PartNode newNode = y.addChild(node);

                    final PartTree t = new PartTree(x, newNode, z);
                    pt[i] = t;
                }
            } else {
                int rem = z.getValue() % r;
                z.setChildCnt(r);
                for (int i = 0; i < r; i++) {
                    int value;
                    if (rem > 0) {
                        value = z.getValue() / r + 1;
                        rem--;
                    } else {
                        value = z.getValue() / r;
                    }

                    final PartNode node = new PartNode(value, null, 0, 0);
                    final PartNode newNode = z.addChild(node);

                    final PartTree t = new PartTree(x, y, newNode);
                    pt[i] = t;
                }
            }
        }
    }

    public PartNode getX() {
        return x;
    }

    public PartNode getY() {
        return y;
    }

    public PartNode getZ() {
        return z;
    }
}

public class Partitioner {

    private Room room;
    private final int xg;
    private final int yg;
    private final int zg;

    private final int n;
    private final PartTree tree;

    public Partitioner(Room room, int xg, int yg, int zg) {
        this.room = room;
        this.xg = xg;
        this.yg = yg;
        this.zg = zg;
        this.n = xg + yg + zg;
        tree = null;
    }

    public Partitioner(Room room, int x, int y, int z, int n) {
        this.room = room;
        this.xg = x;
        this.yg = y;
        this.zg = z;
        this.n = n;

        tree = new PartTree(
                new PartNode(x, null, 0, 0),
                new PartNode(y, null, 0, 0),
                new PartNode(z, null, 0, 0)
        );
    }

    public List<Room> autoPartition() throws IOException {
        if (tree == null) return null;

        tree.partition(n);

        final PartNode x = tree.getX();
        final PartNode y = tree.getY();
        final PartNode z = tree.getZ();

        final int xDiv = x.getNumLeafs();
        final int yDiv = y.getNumLeafs();
        final int zDiv = z.getNumLeafs();

        return new Partitioner(room, xDiv, yDiv, zDiv).partition();
    }

    public List<Room> partition() throws IOException {
        if (!validate())
            return null;

        System.out.println("PREVIOUSLY: " + room.getF());

        final int x = room.getX();
        final int y = room.getY();
        final int z = room.getZ();
        final int xdiv = x / xg;
        final int ydiv = y / yg;
        final int zdiv = z / zg;

        final String basename = room.getFile().substring(0, room.getFile().length() - 4);
        final List<Room> rooms = new ArrayList<>();

        room.startRead();
        for (int nodeNumber = 0; nodeNumber < xg * yg * zg; nodeNumber++) {
            final Room r = new Room(basename + "_" + nodeNumber + ".dwm", xdiv, ydiv, zdiv, room.getF());
            r.startWrite();
            rooms.add(r);
        }

        int cnt = 0;
        for (int i = 0; i < x; i++)
            for (int j = 0; j < y; j++)
                for (int k = 0; k < z; k++) {
                    final int nodeNumber = cnt++ % rooms.size();
                    rooms.get(nodeNumber).writeNode(room.readNode());
                    System.out.println("written " + cnt);
                }

        for (Room room1 : rooms)
            room1.endWrite();
        room.endRead();
        return rooms;
    }

    /**
     * Given the node index return the boundaries of the cuboid encapsulating the node
     *
     * @param n the node index
     * @return the boundaries of the node.
     * First point will be the closest to 0,0,0, the second will be the furthest
     */
    private int[][] getNodeBoundaries(int n) {
        final int x = n / (yg * zg);
        final int y = (n % (yg * zg)) / zg;
        final int z = (n % (yg * zg)) % zg;

        return new int[][]{{x * xg, y * yg, z * zg}, {x * xg + xg, y * yg + yg, z * zg + zg}};
    }

    private boolean validate() {
        return room != null && room.getFile() != null && isCoordsValid();
    }

    public boolean isCoordsValid() {
        // TODO: 12/4/21 validate if the integers make sense for this room
        return xg > 0 && yg > 0 && zg > 0;
    }

    public void setFile(Room room) {
        this.room = room;
    }
}
