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

package com.jongo;

import com.jongo.ParameterizedQuery;
import com.mongodb.DBObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class ParameterizedQueryTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithCharParameter() throws Exception {
        char c = '1';
        ParameterizedQuery query = new ParameterizedQuery("{id:#}", new Object[]{c});
        query.toDBObject();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {
        ParameterizedQuery query = new ParameterizedQuery("{id:#,id2:#}", new Object[]{"123"});
        query.toDBObject();
    }

    @Test
    public void canMapParameter() throws Exception {
        ParameterizedQuery query = new ParameterizedQuery("{id:#}", new Object[]{"123"});

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("id")).isEqualTo("123");
    }

    @Test
    public void canMapParameters() throws Exception {
        ParameterizedQuery query = new ParameterizedQuery("{id:#, test:#}", new Object[]{"123", "456"});
        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("id")).isEqualTo("123");
        assertThat(dbObject.get("test")).isEqualTo("456");
    }

    @Test
    public void canMapDateParameter() throws Exception {

        Date epoch = new Date(0);
        ParameterizedQuery query = new ParameterizedQuery("{mydate:#}", new Object[]{epoch});

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("mydate")).isEqualTo(epoch);
    }

    @Test
    public void canMapListParameter() throws Exception {

        ArrayList<String> elements = new ArrayList<String>();
        elements.add("1");
        elements.add("2");
        ParameterizedQuery query = new ParameterizedQuery("{$in:#}", new Object[]{elements});

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("$in")).isEqualTo(elements);
    }

    @Test
    public void canHandleNullParameter() throws Exception {

        ParameterizedQuery query = new ParameterizedQuery("{id:#}", new Object[]{null});

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("id")).isNull();
    }

    @Test
    public void canHandleBooleanParameter() throws Exception {

        ParameterizedQuery query = new ParameterizedQuery("{id:#}", new Object[]{true});

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("id")).isEqualTo(Boolean.TRUE);
    }
}


