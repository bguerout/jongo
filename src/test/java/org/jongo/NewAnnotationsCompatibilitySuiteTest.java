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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.jongo.marshall.jackson.JongoAnnotationIntrospector;
import org.jongo.marshall.jackson.configuration.AnnotationModifier;
import org.jongo.util.compatibility.CompatibilitySuite;
import org.jongo.util.compatibility.TestContext;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.Annotation;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

@RunWith(CompatibilitySuite.class)
public class NewAnnotationsCompatibilitySuiteTest {

    @Parameterized.Parameters
    public static TestContext context() {

        Mapper mapper = jacksonMapper()
                .addModifier(new FakeAnnotationModifier())
                .build();

        return new TestContext("New Annotations", mapper);
    }

    private static class FakeAnnotationModifier extends AnnotationModifier {

        @Override
        public void modify(ObjectMapper mapper) {
            AnnotationIntrospector primary = new JongoAnnotationIntrospector();
            AnnotationIntrospector secondary = new IgnoreDeprecatedAnnotation();
            AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);

            mapper.setAnnotationIntrospector(pair);
        }
    }

    /**
     * Allows new annotations (@MongoId, @MongoObjectId) to be tested using domain model classes already annotated with deprecated annotations.
     * This class prevents Jackson to detect annotations which are annotated with @JacksonAnnotationsInside (ie. @Id and @ObjectId).
     * Note that domain classes are temporarily annotated with old and new annotations.
     */
    private static class IgnoreDeprecatedAnnotation extends JacksonAnnotationIntrospector {
        @Override
        public boolean isAnnotationBundle(Annotation ann) {
            return false;
        }
    }
}
