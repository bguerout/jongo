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

import com.mongodb.*;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.jongo.ResultHandlerFactory.newResultHandler;

public class Aggregate {

    private final DB db;
    private final String collectionName;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final List<DBObject> pipeline;
    private AggregationOptions options = defaultOptions();

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
        return map(newResultHandler(clazz, unmarshaller));
    }

    public <T> List<T> map(ResultHandler<T> resultHandler) {
        List<DBObject> results = executeAggregateCommand();
        List<T> mappedResult = new ArrayList<T>(results.size());
        for (DBObject dbObject : results) {
            mappedResult.add(resultHandler.map(dbObject));
        }
        return mappedResult;
    }

    public Aggregate options(AggregationOptions options) {
        if (options != null) {
            checkOptions(options);

            this.options = options;
        } else {
            this.options = defaultOptions();
        }

        return this;
    }

    private AggregationOptions defaultOptions() {
        return AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.INLINE).build();
    }

    private List<DBObject> executeAggregateCommand() {
        CommandResult commandResult = db.command(createCommand());
        commandResult.throwOnError();

        return (List<DBObject>) (commandResult.get("result"));
    }

    DBObject createCommand() {
        BasicDBObject cmd = new BasicDBObject("aggregate", collectionName);
        cmd.put("pipeline", pipeline);
        addOptionsToCommand(cmd, options);
        return cmd;
    }

    private void addOptionsToCommand(DBObject command, AggregationOptions options) {
        checkOptions(options);

        if (options.getMaxTime(MILLISECONDS) > 0) {
            command.put("maxTimeMS", options.getMaxTime(MILLISECONDS));
        }

        if (options.getAllowDiskUse() != null) {
            command.put("allowDiskUse", options.getAllowDiskUse());
        }
    }

    private void checkOptions(AggregationOptions options) {
        if (options.getOutputMode() == AggregationOptions.OutputMode.CURSOR) {
            throw new IllegalArgumentException("Cursour output mode is not supported");
        }
    }
}
