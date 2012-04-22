package org.jongo.util;

import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonProcessor;

import java.net.UnknownHostException;

public abstract class JongoTestCase {

    private TestContext testContext;

    public JongoTestCase(TestContext testContext) {
        this.testContext = testContext;
    }

    public JongoTestCase() {
        JacksonProcessor processor = new JacksonProcessor();
        testContext = new TestContext(processor, processor);
    }

    protected MongoCollection createEmptyCollection(String collectionName) throws UnknownHostException {
        MongoCollection col = getCollection(collectionName);
        col.drop();
        return col;
    }

    protected MongoCollection getCollection(String collectionName) throws UnknownHostException {
        return createJongoUsingContext().getCollection(collectionName);
    }

    private Jongo createJongoUsingContext() throws UnknownHostException {
        return new Jongo(getDB(), testContext.getMarshaller(), testContext.getUnmarshaller());
    }

    protected void dropCollection(String collectionName) throws UnknownHostException {
        getDB().getCollection(collectionName).drop();
    }

    protected DB getDB() throws UnknownHostException {
        return testContext.getDB();
    }
}
