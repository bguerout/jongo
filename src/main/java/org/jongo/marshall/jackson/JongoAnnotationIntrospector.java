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

import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.lang.annotation.Annotation;

public class JongoAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public boolean isAnnotationBundle(Annotation ann) {
        boolean isJongoId = ann.annotationType().equals(MongoId.class) || ann.annotationType().equals(MongoObjectId.class);
        return isJongoId ? isJongoId : super.isAnnotationBundle(ann);
    }


}