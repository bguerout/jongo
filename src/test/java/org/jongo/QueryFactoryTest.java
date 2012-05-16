package org.jongo;

import com.mongodb.DBObject;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class QueryFactoryTest {

    @Test
    public void shouldCreateBindableQuery() throws Exception {

        Query query = new QueryFactory().createQuery("{value:#}", 1);

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("value")).isEqualTo(1);

    }
}
