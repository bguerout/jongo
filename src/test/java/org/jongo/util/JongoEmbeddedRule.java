/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.UnknownHostException;
import java.util.Set;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

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
    private JacksonMapper.Builder mapperBuilder = jacksonMapper();

    public JongoEmbeddedRule(MongoEmbeddedRule mongoRule) {
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
                    for (String collectionName : collectionNames) {
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

    public JongoEmbeddedRule withMixIn(final Class<?> spec, final Class<?> mixIn) {
        mapperBuilder.addModifier(new MapperModifier() {
            public void modify(ObjectMapper mapper) {
                mapper.addMixInAnnotations(spec, mixIn);
            }
        });
        return this;
    }

}
