package com.jongo;

public class Poi
{
    String address;
    Coordinate coordinate;

    Poi()
    {
    }

    public Poi(String address)
    {
        this.address = address;
    }

    public Poi(String address, int lat, int lng)
    {
        this.address = address;
        this.coordinate = new Coordinate(lat, lng);
    }
}
