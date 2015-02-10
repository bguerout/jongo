package org.jongo;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.JongoAnnotationIntrospector;
import org.jongo.marshall.jackson.configuration.AnnotationModifier;
import org.jongo.util.compatibility.CompatibilitySuite;
import org.jongo.util.compatibility.TestContext;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.Annotation;

@RunWith(CompatibilitySuite.class)
public class NewAnnotationsCompatibilitySuiteTest {

    @Parameterized.Parameters
    public static TestContext context() {

        Mapper mapper = new JacksonMapper.Builder()
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
            mapper.getDeserializationConfig().with(pair);
            mapper.getSerializationConfig().with(pair);
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
