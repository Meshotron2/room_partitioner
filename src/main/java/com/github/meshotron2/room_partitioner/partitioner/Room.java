package com.github.meshotron2.room_partitioner.partitioner;

import java.io.*;

public class Room 
{
    private final String fileName;

    private final int x;
    private final int y;
    private final int z;

    private final int f;

    private RandomAccessFile roomFile;

    public Room(String fileName, int x, int y, int z, int f) throws IOException
    {
        this.fileName = fileName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = f;

        this.roomFile = new RandomAccessFile(fileName, "rw");
    }

    private Room(String fileName, int x, int y, int z, int f, RandomAccessFile roomFile) 
    {
        this.fileName = fileName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = f;
        this.roomFile = roomFile;
    }

    public static Room fromFile(String fileName) throws IOException
    {
        final RandomAccessFile roomFile = new RandomAccessFile(fileName, "rw");

        roomFile.seek(0);
        final int x = Integer.reverseBytes(roomFile.readInt());
        final int y = Integer.reverseBytes(roomFile.readInt());
        final int z = Integer.reverseBytes(roomFile.readInt());

        final int f = Integer.reverseBytes(roomFile.readInt());

        roomFile.seek(24);

        return new Room(fileName, x, y, z, f, roomFile);
    }

    public void startWrite() throws IOException
    {
        this.roomFile.seek(0);
        this.roomFile.writeInt(Integer.reverseBytes(x));
        this.roomFile.writeInt(Integer.reverseBytes(y));
        this.roomFile.writeInt(Integer.reverseBytes(z));

        this.roomFile.writeInt(Integer.reverseBytes(f));
    }

    public void writeNodeAt(int x, int y, int z, Byte c) throws IOException
    {
        roomFile.seek((long)(x * this.y * this.z + y * this.z + z) + 16);
        roomFile.writeByte(c);
    }

    public void writeNode(Byte c) throws IOException
    {
        roomFile.writeByte(c);
    }

    public void endWrite() throws IOException
    {
        roomFile.close();
    }

    public byte readNodeAt(int x, int y, int z) throws IOException
    {
        roomFile.seek((long)(x * this.y * this.z + y * this.z + z) + 16);
        return roomFile.readByte();
    }

    public byte readNode() throws IOException
    {
        return roomFile.readByte();
    }

    public String getFileName()
    {
        return fileName;
    }
    
    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public int getF()
    {
        return f;
    }
}