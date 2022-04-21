package com.github.meshotron2.room_partitioner.merger;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferedRandomReadAccessFile extends RandomAccessFile
{
    private final byte[] buffer;
    private long firstCachedPos;
    private long lastCachedPos;

    private int bufferSize;

    
    public BufferedRandomReadAccessFile(String fileName, int bufferSize) throws IOException
    {
        super(fileName, "r");
        super.seek(0);
        buffer = new byte[bufferSize];
        firstCachedPos = -1;
        lastCachedPos = -1;

        this.bufferSize = -1;
    }

    public BufferedRandomReadAccessFile(String fileName) throws IOException
    {
        super(fileName, "r");
        super.seek(0);
        buffer = new byte[4096];
        firstCachedPos = -1;
        lastCachedPos = -1;

        this.bufferSize = -1;
    }

    @Override
    public int read() throws IOException
    {        
        return this.readByte(super.getFilePointer());
    }

    public byte readByte(long pos) throws IOException
    {
        if(!isPositionCached(pos))
        {
            fillBuffer(pos);
        }

        return buffer[(int) (pos - firstCachedPos)];
    }

    public int readInt(long pos) throws IOException
    {
        if(!isPositionCached(pos) || !isPositionCached(pos+3))
        {
            fillBuffer(pos);
        }

        return ((buffer[(int) pos] & 0xFF) << 24) | 
            ((buffer[(int) pos + 1] & 0xFF) << 16) | 
            ((buffer[(int) pos + 2] & 0xFF) << 8 ) | 
            ((buffer[(int) pos + 3] & 0xFF));
    }

    @Override
    public void close() throws IOException
    {
        super.close();
    }

    private boolean isPositionCached(long pos)
    {
        return pos >= firstCachedPos && pos <= lastCachedPos;
    }

    private void fillBuffer(long pos) throws IOException
    {
        super.seek(pos);
        bufferSize = super.read(buffer);

        if(bufferSize > 0)
        {
            firstCachedPos = pos;
            lastCachedPos = pos + bufferSize - 1;
        }
    }
}
