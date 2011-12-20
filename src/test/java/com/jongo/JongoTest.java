package com.jongo;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class JongoTest
{
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
        Poi p = Poi.class.newInstance();
        for (Field field : p.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            field.
            field.set(p, murlins.get(field.getName()));
        }
        assertThat(p.address).isEqualTo("22 rue des murlins");
    }
}
