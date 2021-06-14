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


import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class JongoCodecProvider implements CodecProvider {

    private final Mapper mapper;

    public JongoCodecProvider() {
        this(jacksonMapper().build());
    }

    public JongoCodecProvider(Mapper mapper) {
        this.mapper = mapper;
    }

    public <T> Codec<T> get(final Class<T> type, final CodecRegistry registry) {
        return new JongoCodec<T>(mapper, type, registry);
    }
}
