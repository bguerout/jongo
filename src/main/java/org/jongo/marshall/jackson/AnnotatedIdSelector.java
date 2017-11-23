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

import com.fasterxml.jackson.databind.introspect.Annotated;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

public class AnnotatedIdSelector implements IdSelector<Annotated> {

    public boolean isId(Annotated a) {
        return a.hasAnnotation(MongoId.class) || a.hasAnnotation(Id.class);
    }

    public boolean isObjectId(Annotated a) {
        return a.hasAnnotation(MongoObjectId.class) || a.hasAnnotation(org.jongo.marshall.jackson.oid.ObjectId.class);
    }
}
