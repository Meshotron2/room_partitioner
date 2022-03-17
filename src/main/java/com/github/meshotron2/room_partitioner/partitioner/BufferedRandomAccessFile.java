package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferedRandomAccessFile extends RandomAccessFile
{
    private final byte[] buffer;
    private long firstCachedPos;
    private long lastCachedPos;

    private final int maxBufferSize;
    private int bufferSize;
    private int lastBufferPos;
    
    public BufferedRandomAccessFile(String fileName, int bufferSize) throws IOException
    {
        super(fileName, "rw");
        buffer = new byte[bufferSize];
        firstCachedPos = -1;
        lastCachedPos = -1;

        maxBufferSize = bufferSize;
        bufferSize = -1;
        lastBufferPos = -1;
    }

    public void writeByte(byte b, long pos) throws IOException
    {        
        if(!isPositionCached(pos))
        {
            if(pos > super.length() - 1)
            {
                writeBuffer();
                extendFile(pos);
            }
            else
            {
                writeBuffer();
                fillBuffer(pos);
            }
        }

        buffer[(int) (pos - firstCachedPos)] = b;
        lastBufferPos = (int) (pos - firstCachedPos);
    }

    public byte readByte(long pos) throws IOException
    {
        if(!isPositionCached(pos))
        {
            if(pos > super.length() - 1)
            {
                writeBuffer();
                extendFile(pos);
            }
            else
            {
                writeBuffer();
                fillBuffer(pos);
            }
        }

        return buffer[(int) (pos - firstCachedPos)];
    }

    @Override
    public void close() throws IOException
    {
        writeBuffer();
        super.close();
    }

    private boolean isPositionCached(long pos)
    {
        return pos >= firstCachedPos && pos <= lastCachedPos;
    }

    private void extendFile(long pos) throws IOException
    {
        super.seek(pos);
        bufferSize = maxBufferSize;
        firstCachedPos = pos;
        lastCachedPos = pos + maxBufferSize - 1;
        lastBufferPos = 0;
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

    private void writeBuffer() throws IOException
    {
        if(bufferSize > 0 && lastBufferPos > 0)
        {
            super.seek(firstCachedPos);
            super.write(buffer, 0, lastBufferPos+1);
            lastBufferPos = 0;
            bufferSize = 0;
        }
    }
}
