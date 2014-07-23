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

import com.mongodb.AggregationOptions;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;
import org.jongo.util.JongoTestCase;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class AggregateTestWithMocks extends JongoTestCase {

    @Test
    public void shouldAddAggregationOptionsToCommand() throws Exception {
        // Given
        DB db = mock(DB.class);

        Aggregate aggregate = createAggregateWithMocks(db);

        // When
        aggregate.options(AggregationOptions.builder().allowDiskUse(true).build());
        aggregate.as(Object.class);

        // Then
        verify(db).command(
                argThat(
                        new BaseMatcher<DBObject>() {
                            public boolean matches(Object o) {
                                return ((DBObject) o).get("allowDiskUse") == Boolean.TRUE;
                            }

                            public void describeTo(Description description) {
                            }
                        }));
    }

    private Aggregate createAggregateWithMocks(DB db) {
        Unmarshaller unmarshaller = mock(Unmarshaller.class);
        QueryFactory queryFactory = mock(QueryFactory.class);
        CommandResult commandResult = mock(CommandResult.class);

        when(db.command(Matchers.<DBObject>anyObject())).thenReturn(commandResult);
        when(commandResult.get(eq("result"))).thenReturn(new ArrayList<DBObject>());

        return new Aggregate(db, "articles", unmarshaller, queryFactory);
    }
}
