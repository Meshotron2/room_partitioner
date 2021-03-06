package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * A basic and incomplete implementation od a bufered random access file.
 */
public class BufferedRandomAccessFile extends RandomAccessFile {
    private final byte[] buffer;
    private long firstCachedPos;
    private long lastCachedPos;

    private final int maxBufferSize;
    private int bufferSize;
    private int lastBufferPos;

    /**
     * @param fileName The name of the file to open/create.
     * @param bufferSize The size of the buffer. This affects performance. 4096 is fine.
     */
    public BufferedRandomAccessFile(String fileName, int bufferSize) throws IOException {
        super(fileName, "rw");
        buffer = new byte[bufferSize];
        firstCachedPos = -1;
        lastCachedPos = -1;

        maxBufferSize = bufferSize;
        this.bufferSize = -1;
        lastBufferPos = -1;
    }

    /**
     * Writes a byte in the specified position. If the position is beyond the end of the file the file will be extended with 0s.
     * 
     * @param b The byte to write.
     * @param pos The position to write at.
     */
    public void writeByte(byte b, long pos) throws IOException {
        if (!isPositionCached(pos)) {
            if (pos > super.length() - 1) {
                writeBuffer();
                extendFile(pos);
            } else {
                writeBuffer();
                fillBuffer(pos);
            }
        }

        buffer[(int) (pos - firstCachedPos)] = b;

        if((int) (pos - firstCachedPos) > lastBufferPos) {
            lastBufferPos = (int) (pos - firstCachedPos);
        }
    }

    /**
     * Reads a byte at a given position.
     * 
     * @param pos The position to read from.
     */
    public byte readByte(long pos) throws IOException {
        if (!isPositionCached(pos)) {
            if (pos > super.length() - 1) {
                writeBuffer();
                extendFile(pos);
            } else {
                writeBuffer();
                fillBuffer(pos);
            }
        }

        return buffer[(int) (pos - firstCachedPos)];
    }

    @Override
    public void close() throws IOException {
        writeBuffer();
        super.close();
    }

    private boolean isPositionCached(long pos) {
        return pos >= firstCachedPos && pos <= lastCachedPos;
    }

    private void extendFile(long pos) throws IOException {
        super.seek(pos);
        Arrays.fill(buffer, (byte)0);
        bufferSize = maxBufferSize;
        firstCachedPos = pos;
        lastCachedPos = pos + maxBufferSize - 1;
        lastBufferPos = 0;
    }

    private void fillBuffer(long pos) throws IOException {
        super.seek(pos);
        bufferSize = super.read(buffer);

        if (bufferSize > 0) {
            firstCachedPos = pos;
            lastCachedPos = pos + bufferSize - 1;
        }
    }

    private void writeBuffer() throws IOException {
        if (bufferSize > 0 && lastBufferPos > 0) {
            super.seek(firstCachedPos);
            super.write(buffer, 0, lastBufferPos + 1);
            lastBufferPos = 0;
            bufferSize = 0;
        }
    }
}
