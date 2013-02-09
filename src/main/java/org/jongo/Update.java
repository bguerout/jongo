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

package org.jongo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

public class Update {

    private final DBCollection collection;
    private final Query query;
    private final QueryFactory queryFactory;

    private WriteConcern writeConcern;
    private boolean upsert = false;
    private boolean multi = false;

    Update(DBCollection collection, WriteConcern writeConcern, QueryFactory queryFactory, String query, Object... parameters) {
        this.collection = collection;
        this.writeConcern = writeConcern;
        this.queryFactory = queryFactory;
        this.query = createQuery(query, parameters);
    }

    public WriteResult with(String modifier) {
        return with(modifier, new Object[0]);
    }

    public WriteResult with(String modifier, Object... parameters) {
        DBObject dbQuery = query.toDBObject();
        DBObject dbModifier = queryFactory.createQuery(modifier, parameters).toDBObject();
        return collection.update(dbQuery, dbModifier, upsert, multi, writeConcern);
    }

    public Update upsert() {
        this.upsert = true;
        return this;
    }

    public Update multi() {
        this.multi = true;
        return this;
    }

    private Query createQuery(String query, Object[] parameters) {
        try {
            return this.queryFactory.createQuery(query, parameters);
        } catch (Exception e) {
            String message = String.format("Unable execute update operation using query %s", query);
            throw new IllegalArgumentException(message, e);
        }
    }
}
