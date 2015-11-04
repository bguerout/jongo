package org.jongo.util;

import static org.junit.Assume.assumeTrue;

import java.net.UnknownHostException;
import java.util.Set;

import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.mongodb.CommandResult;
import com.mongodb.DB;

/**
 * A JUnit test rule for testing Jongo with embedded Mongo.
 * 
 * @author Benoit Guérout
 * @author yamsellem
 * @author Alexandre Dutra
 * @author Christian Trimble
 */
public class JongoEmbeddedRule implements TestRule {

    private Jongo jongo;
    private Mapper mapper;
    private MongoEmbeddedRule mongoRule;
    private Set<String> collectionNames = Sets.newHashSet();
    private JacksonMapper.Builder mapperBuilder = new JacksonMapper.Builder();
    
    public JongoEmbeddedRule( MongoEmbeddedRule mongoRule ) {
        this.mongoRule = mongoRule;
    }

    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MongoResource mongoResource = mongoRule.getResource();
                mapper = mapperBuilder.build();
                jongo = new Jongo(mongoResource.getDb("test_jongo"), mapper);
                try {
                  base.evaluate();
                } finally {
                    for( String collectionName : collectionNames ) {
                        dropCollection(collectionName);
                    }
                }
            }
        };
    }
    
    public MongoCollection createEmptyCollection(String collectionName) throws UnknownHostException {
        collectionNames.add(collectionName);
        MongoCollection col = jongo.getCollection(collectionName);
        col.drop();
        return col;
    }

    public void dropCollection(String collectionName) throws UnknownHostException {
        getDatabase().getCollection(collectionName).drop();
    }

    public DB getDatabase() throws UnknownHostException {
        return jongo.getDatabase();
    }

    public Jongo getJongo() {
        return jongo;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void assumeThatMongoVersionIsGreaterThan(String expectedVersion) throws UnknownHostException {
        int expectedVersionAsInt = Integer.valueOf(expectedVersion.replaceAll("\\.", ""));
        CommandResult buildInfo = getDatabase().command("buildInfo");
        String version = (String) buildInfo.get("version");
        int currentVersion = Integer.valueOf(version.replaceAll("\\.", ""));
        assumeTrue(currentVersion >= expectedVersionAsInt);
    }

    public void prepareMarshallingStrategy(Mapper mapper) {
        this.mapper = mapper;
        this.jongo = new Jongo(mongoRule.getResource().getDb("test_jongo"), mapper);
    }

    public JongoEmbeddedRule withMixIn(final Class<?> spec, final Class<?> mixIn) {
        mapperBuilder.addModifier(new MapperModifier() {
            public void modify(ObjectMapper mapper) {
                mapper.addMixInAnnotations(spec, mixIn);
            }});
        return this;
    }

}
