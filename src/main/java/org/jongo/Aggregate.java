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

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.jongo.ResultMapperFactory.newMapper;

public final class Aggregate {

    private final DB db;
    private final String collectionName;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final List<DBObject> pipeline;

    Aggregate(DB db, String collectionName, Unmarshaller unmarshaller, QueryFactory queryFactory) {
        this.db = db;
        this.collectionName = collectionName;
        this.unmarshaller = unmarshaller;
        this.queryFactory = queryFactory;
        this.pipeline = new ArrayList<DBObject>();
    }

    public Aggregate and(String pipelineOperator, Object... parameters) {
        DBObject dbQuery = queryFactory.createQuery(pipelineOperator, parameters).toDBObject();
        pipeline.add(dbQuery);
        return this;
    }

    public <T> List<T> as(final Class<T> clazz) {
        return map(newMapper(clazz, unmarshaller));
    }

    public <T> List<T> map(ResultMapper<T> resultMapper) {
        List<DBObject> results = executeAggregateCommand();
        List<T> mappedResult = new ArrayList<T>(results.size());
        for (DBObject dbObject : results) {
            mappedResult.add(resultMapper.map(dbObject));
        }
        return mappedResult;
    }

    private List<DBObject> executeAggregateCommand() {
        CommandResult commandResult = db.command(createCommand());
        commandResult.throwOnError();
        return (List<DBObject>) (commandResult.get("result"));
    }

    DBObject createCommand() {
        BasicDBObject cmd = new BasicDBObject("aggregate", collectionName);
        cmd.put("pipeline", pipeline);
        return cmd;
    }
}
