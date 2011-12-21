package com.jongo;

import static org.fest.assertions.Assertions.assertThat;

import java.net.UnknownHostException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;

public class MongoCollectionTest
{
    MongoCollection mongoCollection;

    @Before
    public void setUp() throws UnknownHostException, MongoException
    {
        mongoCollection = new MongoCollection("jongo", "poi");
        mongoCollection.drop();
    }

    @Test
    public void canFindEntity() throws Exception
    {
        /* given */
        String address = "22 rue des murlins";
        mongoCollection.save(new Poi(address));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{address:{$exists:true}}", Poi.class);

        /* then */
        assertThat(results.next().address).isEqualTo(address);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canFindEntityUsingSubProperty() throws Exception
    {
        /* given */
        String address = "22 rue des murlins";
        int lat = 48, lng = 2;
        mongoCollection.save(new Poi(address, lat, lng));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{'coordinate.lat':48}", Poi.class);

        /* then */
        assertThat(results.next().coordinate.lat).isEqualTo(lat);
        assertThat(results.hasNext()).isFalse();
    }
}
