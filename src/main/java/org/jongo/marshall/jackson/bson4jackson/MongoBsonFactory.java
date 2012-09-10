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

package org.jongo.marshall.jackson.bson4jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class MongoBsonFactory extends BsonFactory {

    public static BsonFactory createFactory() {
        BsonFactory factory = new MongoBsonFactory();
        factory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
        return factory;
    }

    @Override
    protected BsonParser _createJsonParser(InputStream in, IOContext ctxt) throws IOException, JsonParseException {
        BsonParser p = new MongoBsonParser(ctxt, _parserFeatures, _bsonParserFeatures, in);
        ObjectCodec codec = getCodec();
        if (codec != null) {
            p.setCodec(codec);
        }
        return p;
    }

    @Override
    public BsonGenerator createJsonGenerator(OutputStream out) throws IOException {
        BsonGenerator g = new MongoBsonGenerator(_generatorFeatures, _bsonGeneratorFeatures, out);
        ObjectCodec codec = getCodec();
        if (codec != null) {
            g.setCodec(codec);
        }
        return g;
    }
}
