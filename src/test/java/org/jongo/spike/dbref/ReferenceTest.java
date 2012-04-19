package org.jongo.spike.dbref;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ReferenceTest {

    private DBRef dbRef;
    private ObjectMapper mapper;
    private Reference reference;

    @Before
    public void setUp() throws Exception {
        dbRef = mock(DBRef.class);
        mapper = mock(ObjectMapper.class);
        reference = new Reference(dbRef, mapper);
    }

    @Test
    public void shouldFetchAndMarshallResult() throws Exception {

        when(dbRef.fetch()).thenReturn(new BasicDBObject());

        reference.as(ReferenceTest.class);

        verify(dbRef).fetch();
        verify(mapper).readValue("{ }", ReferenceTest.class);
    }
}
