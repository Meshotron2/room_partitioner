package com.github.meshotron2.room_partitioner.partitioner;

import java.io.*;

/**
 * Represents a room and provides methods to read from and write it to a file in the dwm format
 */
public class Room {
    /**
     * The name of the file this room should be stored on
     */
    private final String fileName;

    /**
     * The number of nodes in the x axis
     */
    private final int x;
    /**
     * The number of nodes in the y axis
     */
    private final int y;
    /**
     * The number of nodes in the z axis
     */
    private final int z;

    /**
     * The sampling frequency.
     * Please refer to the official dwm file specification to se which ones are allowed
     */
    private final int f;

    /**
     * The utility used to write the data to the file
     */
    private final BufferedRandomAccessFile roomFile;

    public Room(String fileName, int x, int y, int z, int f) throws IOException {
        this.fileName = fileName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = f;

        this.roomFile = new BufferedRandomAccessFile(fileName, 16384);
    }

    private Room(String fileName, int x, int y, int z, int f, BufferedRandomAccessFile roomFile) {
        this.fileName = fileName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = f;
        this.roomFile = roomFile;
    }

    /**
     * Reads a room from a file.
     *
     * @param fileName The location where the file is
     * @return The room read from it, if the header data is valid
     * @throws IOException in case any error from the {@link BufferedRandomAccessFile}
     */
    public static Room fromFile(String fileName) throws IOException {
        final BufferedRandomAccessFile roomFile = new BufferedRandomAccessFile(fileName, 16384);

        roomFile.seek(0);
        final int x = Integer.reverseBytes(roomFile.readInt());
        final int y = Integer.reverseBytes(roomFile.readInt());
        final int z = Integer.reverseBytes(roomFile.readInt());

        final int f = Integer.reverseBytes(roomFile.readInt());

        roomFile.seek(24);

        return new Room(fileName, x, y, z, f, roomFile);
    }

    /**
     * Opens the {@link BufferedRandomAccessFile} and writes the header.
     * All previous data is lost.
     *
     * @throws IOException in case any error from the {@link BufferedRandomAccessFile}
     */
    public void startWrite() throws IOException {
        this.roomFile.seek(0);
        this.roomFile.writeInt(Integer.reverseBytes(x));
        this.roomFile.writeInt(Integer.reverseBytes(y));
        this.roomFile.writeInt(Integer.reverseBytes(z));

        this.roomFile.writeInt(Integer.reverseBytes(f));
    }

    /**
     * Writes a byte to this room's file in the location defined by (x, y, z).
     * <p>
     * To see how it translates in a position in the file, please refer to the official dwm file specification
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param c the byte to write
     * @throws IOException in case any error from the {@link BufferedRandomAccessFile}
     */
    public void writeNodeAt(int x, int y, int z, Byte c) throws IOException {
        roomFile.writeByte(c, (long) (x * this.y * this.z + y * this.z + z) + 16);
    }

    /**
     * Closes the file stream.
     *
     * @throws IOException in case any error from the {@link BufferedRandomAccessFile}
     */
    public void endWrite() throws IOException {
        roomFile.close();
    }

    /**
     * Returns the node in the location defined by (x, y, z)
     * <p>
     * To see how this translates into placement in the file please refer to the official dwm file specification
     *
     * @param x the x coordinates
     * @param y the y coordinates
     * @param z the z coordinates
     * @return the byte refering to the location (x, y, z)
     * @throws IOException in case any error from the {@link BufferedRandomAccessFile}
     */
    public byte readNodeAt(int x, int y, int z) throws IOException {
        return roomFile.readByte((long) (x * this.y * this.z + y * this.z + z) + 16);
    }

    public String getFileName() {
        return fileName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getF() {
        return f;
    }
}