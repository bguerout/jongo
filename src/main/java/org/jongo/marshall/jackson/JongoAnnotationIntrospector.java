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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import org.jongo.marshall.jackson.oid.*;

@SuppressWarnings("deprecation")
public class JongoAnnotationIntrospector extends NopAnnotationIntrospector {

    private final IdSelector<Annotated> idSelector;

    public JongoAnnotationIntrospector() {
        this(new AnnotatedIdSelector());
    }

    public JongoAnnotationIntrospector(IdSelector<Annotated> idSelector) {
        this.idSelector = idSelector;
    }

    @Override
    public Include findSerializationInclusion(Annotated a, Include defValue) {
        return idSelector.isObjectId(a) ? Include.NON_NULL : defValue;
    }

    @Override
    public Object findSerializer(Annotated a) {
        return idSelector.isObjectId(a) ? ObjectIdSerializer.class : super.findSerializer(a);
    }

    @Override
    public Object findDeserializer(Annotated a) {
        return idSelector.isObjectId(a) ? ObjectIdDeserializer.class : super.findDeserializer(a);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        return idSelector.isId(a) ? new PropertyName("_id") : super.findNameForSerialization(a);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        return idSelector.isId(a) ? new PropertyName("_id") : super.findNameForDeserialization(a);
    }

    public static class AnnotatedIdSelector implements IdSelector<Annotated> {

        public boolean isId(Annotated a) {
            return a.hasAnnotation(MongoId.class) || a.hasAnnotation(Id.class);
        }

        public boolean isObjectId(Annotated a) {
            return a.hasAnnotation(MongoObjectId.class) || a.hasAnnotation(ObjectId.class);
        }
    }
}