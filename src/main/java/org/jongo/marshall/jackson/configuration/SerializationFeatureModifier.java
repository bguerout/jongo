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

package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class SerializationFeatureModifier implements MapperModifier {
    private final SerializationFeature feature;
    private final boolean enable;

    public SerializationFeatureModifier(SerializationFeature feature, boolean enable) {
        this.feature = feature;
        this.enable = enable;
    }

    public void modify(ObjectMapper mapper) {
        if (enable)
            mapper.enable(feature);
        else
            mapper.disable(feature);
    }
}
