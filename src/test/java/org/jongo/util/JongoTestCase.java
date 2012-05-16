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
