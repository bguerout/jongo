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

    private WriteConcern concern;
    private boolean upsert = false;
    private boolean multi = false;

    Update(DBCollection collection, QueryFactory queryFactory, String query, Object... parameters) {
        this.collection = collection;
        this.queryFactory = queryFactory;
        this.query = createQuery(query, parameters);
    }

    private WriteConcern determineWriteConcern() {
        return concern == null ? collection.getWriteConcern() : concern;
    }

    public WriteResult with(String modifier) {
        return with(modifier, new Object[0]);
    }

    public WriteResult with(String modifier, Object... parameters) {
        Query updateQuery = queryFactory.createQuery(modifier, parameters);
        return collection.update(this.query.toDBObject(), updateQuery.toDBObject(), upsert, multi, determineWriteConcern());
    }

    public WriteResult with(Object pojo) {
        DBObject updateDbo = queryFactory.createQuery("{$set:#}", pojo).toDBObject();
        DBObject pojoAsDbo = (DBObject) updateDbo.get("$set");
        pojoAsDbo.removeField("_id");
        return collection.update(this.query.toDBObject(), updateDbo, upsert, multi, determineWriteConcern());
    }

    public Update concern(WriteConcern concern) {
        this.concern = concern;
        return this;
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
