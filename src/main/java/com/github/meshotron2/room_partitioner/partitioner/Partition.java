package com.github.meshotron2.room_partitioner.partitioner;

public class Partition 
{
    private final int xi;
    private final int xf;
    private final int yi;
    private final int yf;
    private final int zi;
    private final int zf;

    public Partition(int xi, int xf, int yi, int yf, int zi, int zf)
    {
        this.xi = xi;
        this.xf = xf;
        this.yi = yi;
        this.yf = yf;
        this.zi = zi;
        this.zf = zf;
    }

    public int getXi()
    {
        return xi;
    }

    public int getXf()
    {
        return xf;
    }

    public int getYi()
    {
        return yi;
    }

    public int getYf()
    {
        return yf;
    }

    public int getZi()
    {
        return zi;
    }

    public int getZf()
    {
        return zf;
    }
}
