package com.jongo;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class JongoTest
{
    private static final String MONGO_ID = "_id";
    private DBCollection pois;

    @Before
    public void setUp() throws Exception
    {
        Mongo mongo = new Mongo();
        pois = mongo.getDB("jongo").getCollection("poi");
    }

    @After
    public void tearDown() throws Exception
    {
        pois.drop();
    }

    @Test
    public void whenParsingShouldBeADbObject() throws Exception
    {
        Object parse = JSON.parse("{address:'Rochelle'}");

        assertThat(parse).isInstanceOf(DBObject.class);
    }

    @Test
    public void shouldParseExistsToken() throws Exception
    {
        DBObject query = Jongo.createQuery("{address:{$exists:true}}");

        DBObject address = ((DBObject) query.get("address"));
        assertThat(address).isNotNull();
        assertThat(address.get("$exists")).isEqualTo(true);
    }

    @Test
    public void testShouldSaveEntityFromString() throws Exception
    {
        pois.save(Jongo.createQuery("{address:'22 rue des murlins'}"));

        assertThat(pois.count()).isEqualTo(1);
    }

    @Test
    public void testShouldSaveEntityFromBean() throws Exception
    {
        Poi poi = new Poi("22 rue des murlins");
        pois.save(Jongo.marshall(poi));

        assertThat(pois.count()).isEqualTo(1);
    }

    @Test
    public void shouldFindEntity() throws Exception
    {
        pois.save(Jongo.createQuery("{address:'22 rue des murlins'}"));

        String query = "{address:{$exists:true}}";
        DBCursor cursor = pois.find(Jongo.createQuery(query));

        assertThat((Iterator) cursor).hasSize(1);
    }

    @Test
    public void shouldApplyCriteria() throws Exception
    {
        pois.save(Jongo.createQuery("{address:'44 rue des murlins'}"));
        pois.save(Jongo.createQuery("{address:'22 rue des murlins'}"));

        DBCursor cursor = pois.find(Jongo.createQuery("{address:'22 rue des murlins'}"));

        DBObject murlins = cursor.next();
        assertThat(murlins.get("address")).isEqualTo("22 rue des murlins");
    }

    @Test
    public void shouldFindBeanEntity() throws Exception
    {
        Poi poi = new Poi("22 rue des murlins");
        pois.save(Jongo.marshall(poi));

        DBCursor cursor = pois.find(Jongo.createQuery("{address:'22 rue des murlins'}"));

        DBObject murlins = cursor.next();
        murlins.put(MONGO_ID, murlins.get(MONGO_ID).toString());
        Poi p = Jongo.unmarshallString(murlins.toString(), Poi.class);
        assertThat(p.address).isEqualTo("22 rue des murlins");
    }
}
