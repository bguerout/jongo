package org.jongo.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit rule for testing with embedded Mongo.
 * 
 * @author Alexandre Dutra
 * @author Christian Trimble
 */
public class MongoEmbeddedRule implements TestRule {
    private MongoResource mongoResource;
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                mongoResource = new MongoResource();
                base.evaluate();
            }
        };
    }
    
    public MongoResource getResource() {
        return mongoResource;
    }

}
