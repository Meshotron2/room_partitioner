package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Partitioner {

    private Room room;
    private final int xg;
    private final int yg;
    private final int zg;

    public Partitioner(Room room, int xg, int yg, int zg) {
        this.room = room;
        this.xg = xg;
        this.yg = yg;
        this.zg = zg;
    }

    public List<Room> partition() throws IOException {
        if (!validate())
            return null;

        System.out.println("PREVIOUSLY: " + room.getF());

        int x = room.getX();
        int y = room.getY();
        int z = room.getZ();
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
