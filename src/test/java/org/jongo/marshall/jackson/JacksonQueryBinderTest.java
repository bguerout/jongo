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

package org.jongo.marshall.jackson;

import com.google.common.collect.Lists;
import org.jongo.marshall.jackson.configuration.MappingConfig;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;
import org.jongo.model.Gender;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class JacksonQueryBinderTest {

    private JacksonQueryBinder binder;

    @Before
    public void setUp() throws Exception {
        MappingConfig config = MappingConfigBuilder.usingJson().createConfiguration();
        binder = new JacksonQueryBinder(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidParameter() throws Exception {

        binder.bind("{id:#}", new ErrorObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        binder.bind("{id:#,id2:#}", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotTooManyParameters() throws Exception {

        binder.bind("{id:#}", 123, 456);
    }

    @Test
    public void shouldBindOneParameter() throws Exception {

        String query = binder.bind("{id:#}", 123);

        assertThat(query).isEqualTo("{id:123}");
    }

    @Test
    public void shouldBindManyParameters() throws Exception {

        String query = binder.bind("{id:#, test:#}", 123, 456);

        assertThat(query).isEqualTo("{id:123, test:456}");
    }

    @Test
    public void shouldSerializeListOfPrimitive() throws Exception {
        List<String> strings = Lists.newArrayList("1", "2");

        String query = binder.bind("{test:#}", strings);

        assertThat(query).isEqualTo("{test:[\"1\",\"2\"]}");
    }

    @Test
    public void shouldSerializeEnum() throws Exception {

        String query = binder.bind("{test:#}", Gender.FEMALE);

        assertThat(query).isEqualTo("{test:\"FEMALE\"}");
    }


    @Test
    public void shouldBindParameterWithCustomToken() throws Exception {

        MappingConfig config = MappingConfigBuilder.usingJson().createConfiguration();
        JacksonQueryBinder binderWithToken = new JacksonQueryBinder(config, "@");

        String query = binderWithToken.bind("{id:@}", 123);

        assertThat(query).isEqualTo("{id:123}");
    }

    @Test
    public void shouldBindAQueryWithOnlyAToken() throws Exception {

        String query = binder.bind("#", 123);

        assertThat(query).isEqualTo("123");
    }


}


