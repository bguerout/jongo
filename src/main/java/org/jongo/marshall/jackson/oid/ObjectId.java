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

package org.jongo.marshall.jackson.oid;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Retention;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Deprecated
@Retention(RUNTIME)
@JacksonAnnotationsInside

@JsonInclude(NON_NULL)
@JsonSerialize(using = ObjectIdSerializer.class)
@JsonDeserialize(using = ObjectIdDeserializer.class)
/**
 * This class has been deprecated because it has side effects when used with an external Jackson ObjectMapper.
 * It will be removed in a future release.
 * Use @MongoObjectId instead.
 */
public @interface ObjectId {
}
