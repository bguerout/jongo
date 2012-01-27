package org.jongo.model;

public class Coordinate3D extends Coordinate
{
    public int alt;

    Coordinate3D()
    {
    }

    public Coordinate3D(int lat, int lng, int alt)
    {
        super(lat, lng);
        this.alt = alt;
    }
}
