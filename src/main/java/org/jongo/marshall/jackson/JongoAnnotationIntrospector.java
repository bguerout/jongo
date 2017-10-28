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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import org.jongo.marshall.jackson.oid.*;

@SuppressWarnings("deprecation")
public class JongoAnnotationIntrospector extends NopAnnotationIntrospector {
    private static final long serialVersionUID = 1L;

    @Override
    public Include findSerializationInclusion(Annotated a, Include defValue) {
        return hasMongoObjectId(a) ? Include.NON_NULL : defValue;
    }

    @Override
    public Object findSerializer(Annotated a) {
        return hasMongoObjectId(a) ? ObjectIdSerializer.class : super.findSerializer(a);
    }

    @Override
    public Object findDeserializer(Annotated a) {
        return hasMongoObjectId(a) ? ObjectIdDeserializer.class : super.findDeserializer(a);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        return hasMongoId(a) ? new PropertyName("_id") : super.findNameForSerialization(a);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        return hasMongoId(a) ? new PropertyName("_id") : super.findNameForDeserialization(a);
    }

    private static boolean hasMongoId(Annotated a) {
        return a.hasAnnotation(MongoId.class) || a.hasAnnotation(Id.class) || a.hasAnnotation(javax.persistence.Id.class);
    }

    private static boolean hasMongoObjectId(Annotated a) {
        return a.hasAnnotation(MongoObjectId.class) || a.hasAnnotation(ObjectId.class) || a.hasAnnotation(javax.persistence.Id.class);
    }
}