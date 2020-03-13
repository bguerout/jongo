/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.IdSelector;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.JacksonObjectIdUpdater;
import org.jongo.marshall.jackson.JongoAnnotationIntrospector;
import org.jongo.marshall.jackson.configuration.AnnotationModifier;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.util.compatibility.CompatibilitySuite;
import org.jongo.util.compatibility.TestContext;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(CompatibilitySuite.class)
public class DeprecatedAnnotationsCompatibilitySuiteTest {

    @Parameterized.Parameters
    public static TestContext context() {

        Mapping mapping = new Mapping.Builder()
                .addModifier(new DeprecatedAnnotationModifier())
                .build();

        ObjectMapper objectMapper = mapping.getObjectMapper();

        Mapper mapper = new JacksonMapper.Builder(objectMapper)
                .withObjectIdUpdater(new JacksonObjectIdUpdater(objectMapper, new DeprecatedObjectIdSelector()))
                .build();

        return new TestContext("DeprecatedAnnotations", mapper);
    }

    private static class DeprecatedAnnotationModifier extends AnnotationModifier {

        @Override
        public void modify(ObjectMapper mapper) {
            AnnotationIntrospector introspector = new JongoAnnotationIntrospector(new DeprecatedIdSelector());
            AnnotationIntrospector jacksonIntrospector = mapper.getSerializationConfig().getAnnotationIntrospector();
            AnnotationIntrospector pair = new AnnotationIntrospectorPair(introspector, jacksonIntrospector);
            mapper.setAnnotationIntrospector(pair);
        }

        private static class DeprecatedIdSelector implements IdSelector<Annotated> {

            public boolean isId(Annotated a) {
                return a.hasAnnotation(Id.class);
            }

            public boolean isObjectId(Annotated a) {
                return a.hasAnnotation(org.jongo.marshall.jackson.oid.ObjectId.class);
            }
        }
    }

    private static class DeprecatedObjectIdSelector implements IdSelector<BeanPropertyDefinition> {

        public boolean isId(BeanPropertyDefinition property) {
            return "_id".equals(property.getName()) || hasIdAnnotation(property);
        }

        public boolean isObjectId(BeanPropertyDefinition property) {
            return property.getPrimaryMember().getAnnotation(org.jongo.marshall.jackson.oid.ObjectId.class) != null
                    || ObjectId.class.isAssignableFrom(property.getAccessor().getRawType());
        }

        private boolean hasIdAnnotation(BeanPropertyDefinition property) {
            if (property == null) return false;
            AnnotatedMember accessor = property.getPrimaryMember();
            return accessor != null && accessor.getAnnotation(Id.class) != null;
        }
    }

}
