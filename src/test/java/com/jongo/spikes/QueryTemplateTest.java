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

package com.jongo.spikes;

import com.mongodb.DBObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class QueryTemplateTest {

    private QueryTemplate template = new QueryTemplate();

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithoutParameters() throws Exception {
        template.parameterize("{id:#}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {
        template.parameterize("{id:#,id2:#}", "123");
    }

    @Test
    public void canMapParameter() throws Exception {

        DBObject dbObject = template.parameterize("{id:#}", "123");

        assertThat(dbObject.get("id")).isEqualTo("123");
    }

    @Test
    public void canMapParameters() throws Exception {

        DBObject dbObject = template.parameterize("{id:#, test:#}", "123", "456");

        assertThat(dbObject.get("id")).isEqualTo("123");
        assertThat(dbObject.get("test")).isEqualTo("456");
    }

    @Test
    public void canMapDateParameter() throws Exception {

        Date epoch = new Date(0);

        DBObject dbObject = template.parameterize("{mydate:#}", epoch);

        assertThat(dbObject.get("mydate")).isEqualTo(epoch);
    }

    @Test
    public void canMapListParameter() throws Exception {

        ArrayList<String> elements = new ArrayList<String>();
        elements.add("1");
        elements.add("2");

        DBObject dbObject = template.parameterize("{$in:#}", elements);

        assertThat(dbObject.get("$in")).isEqualTo(elements);
    }

    @Test
    public void canHandleNullParameter() throws Exception {

        DBObject dbObject = template.parameterize("{id:#}", null);

        assertThat(dbObject.get("id")).isNull();
    }

    @Test
    public void canHandleBooleanParameter() throws Exception {

        DBObject dbObject = template.parameterize("{id:#}", true);

        assertThat(dbObject.get("id")).isEqualTo(Boolean.TRUE);
    }
}


