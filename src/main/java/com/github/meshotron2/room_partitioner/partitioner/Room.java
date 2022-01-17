package com.github.meshotron2.room_partitioner.partitioner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Room {
    private final String file;

    private final int x;
    private final int y;
    private final int z;

    private final long f;

    private DataInputStream reader;
    private DataOutputStream writer;

    Room(String file, int x, int y, int z, long f) {
        this.file = file;
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = f;

        System.out.println(this.f);
    }

    public void startRead() throws IOException {
        if (file == null)
            throw new IllegalStateException("Room file name cannot be null");

        this.reader = new DataInputStream(new FileInputStream(file));
        reader.skip(24); // skip until first char
    }

    public void endRead() throws IOException {
        reader.close();
    }

    public char readNode() throws IOException {
        final byte[] bs = new byte[2];

        int status = reader.read(bs);
        if (status != 2) return '\n';

        return (char) (((char) bs[0]) << 8 | ((char) bs[1]));
    }

    public void startWrite() throws IOException {
        if (file == null)
            throw new IllegalStateException("Room file name cannot be null");

        final Path path = Path.of(file);
        if (Files.exists(path))
            Files.delete(path);

        Files.createFile(path);

        this.writer = new DataOutputStream(new FileOutputStream(file));

        this.writer.writeInt(Integer.reverseBytes(x));
        this.writer.writeInt(Integer.reverseBytes(y));
        this.writer.writeInt(Integer.reverseBytes(z));

        this.writer.writeLong(Long.reverseBytes(f));
    }

    public void endWrite() throws IOException {
        writer.close();
    }

    public void writeNode(char c) throws IOException {
        writer.write(new byte[]{(byte) c, (byte) (c >> 8)});
    }

//    public void writeNode(char c, int x, int y, int z) throws IOException {
//        final int n = 24 + x * y * z;
//
//        byte[] bytes = {(byte) (c >> 8), (byte) c};
////        System.out.println(bytes.length);
////        writer.write(bytes, n, 2);
//
//        randWriter = new RandomAccessFile(file, "rw");
//        randWriter.seek(n);
//        randWriter.write(bytes);
//        randWriter.close();
//    }

    public static Room fromFile(String file) throws IOException {
        final DataInputStream reader = new DataInputStream(new FileInputStream(file));

        final int x = Integer.reverseBytes(reader.readInt());
        final int y = Integer.reverseBytes(reader.readInt());
        final int z = Integer.reverseBytes(reader.readInt());

        final long f = Long.reverseBytes(reader.readLong());

        reader.close();
        return new Room(file, x, y, z, f);
    }

    public String getFile() {
        return file;
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

    public long getF() {
        return f;
    }
}