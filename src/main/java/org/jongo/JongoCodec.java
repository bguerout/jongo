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


import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.jongo.bson.Bson;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;

public class JongoCodec<T> implements CollectibleCodec<T> {

    private final Codec<RawBsonDocument> codec;
    private final Class<T> clazz;
    private final ObjectIdUpdater objectIdUpdater;
    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public JongoCodec(Mapper mapper, Class<T> clazz, CodecRegistry codecRegistry) {
        this.codec = codecRegistry.get(RawBsonDocument.class);
        this.clazz = clazz;
        objectIdUpdater = mapper.getObjectIdUpdater();
        unmarshaller = mapper.getUnmarshaller();
        marshaller = mapper.getMarshaller();
    }

    public T decode(BsonReader reader, DecoderContext decoderContext) {
        RawBsonDocument raw = codec.decode(reader, decoderContext);
        org.jongo.bson.BsonDocument bsonDocument = Bson.createDocument(raw.getByteBuffer().array());
        return unmarshaller.unmarshall(bsonDocument, clazz);
    }

    public void encode(BsonWriter writer, Object pojo, EncoderContext encoderContext) {
        org.jongo.bson.BsonDocument document = marshaller.marshall(pojo);
        codec.encode(writer, new RawBsonDocument(document.toByteArray()), encoderContext);
    }

    public Class<T> getEncoderClass() {
        return this.clazz;
    }

    public T generateIdIfAbsentFromDocument(T document) {
        if (objectIdUpdater.mustGenerateObjectId(document)) {
            ObjectId newOid = ObjectId.get();
            objectIdUpdater.setObjectId(document, newOid);
        }
        return document;
    }

    public boolean documentHasId(T document) {
        return objectIdUpdater.mustGenerateObjectId(document);
    }

    public BsonValue getDocumentId(T document) {

        Object id = objectIdUpdater.getId(document);

        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }
        if (id instanceof ObjectId) {
            return new BsonObjectId((ObjectId) id);
        }

        throw new UnsupportedOperationException("Unable to get document id");
    }

}
