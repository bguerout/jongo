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

package org.jongo.util;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.NullProcessor;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

public class EmbeddedMongo {

    public DB getDb(String dbname) {
        return Holder.instance.getDB(dbname);
    }

    private static class Holder {

        private static MongoClient instance = getLocalInstance();

        private static MongoClient getLocalInstance() {
            try {
                int port = RandomPortNumberGenerator.pickAvailableRandomEphemeralPortNumber();
                MongoClient mongo = new EmbeddedMongoClient(port);
                mongo.setWriteConcern(WriteConcern.FSYNC_SAFE);
                return mongo;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }
    }


    /**
     * Special subclass of {@link com.mongodb.MongoClient} that
     * launches an embedded Mongod server instance in a separate process.
     *
     * @author Alexandre Dutra
     */
    private static class EmbeddedMongoClient extends MongoClient {

        public static final String DEFAULT_HOST = "127.0.0.1";

        public static final int DEFAULT_PORT = 27017;

        public static final WriteConcern DEFAULT_WRITE_CONCERN = WriteConcern.FSYNC_SAFE;

        public static final Version DEFAULT_VERSION = Version.V2_2_4;

        public EmbeddedMongoClient() throws MongoException, IOException {
            this(DEFAULT_HOST, DEFAULT_PORT);
        }

        public EmbeddedMongoClient(int port) throws MongoException, IOException {
            this(DEFAULT_HOST, port);
        }

        public EmbeddedMongoClient(String host, int port) throws MongoException, IOException {
            this(host, port, DEFAULT_WRITE_CONCERN);
        }

        public EmbeddedMongoClient(String host, int port, WriteConcern writeConcern) throws MongoException, IOException {
            this(host, port, writeConcern, DEFAULT_VERSION);
        }

        public EmbeddedMongoClient(String host, int port, WriteConcern writeConcern, Version version) throws MongoException, IOException {
            super(host, port);
            this.setWriteConcern(writeConcern);
            IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD)
                            //no logs
                    .processOutput(new ProcessOutput(new NullProcessor(), new NullProcessor(), new NullProcessor()))
                    .build();
            MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
            MongodConfig config = new MongodConfig(version, port, Network.localhostIsIPv6());
            MongodExecutable mongodExe = runtime.prepare(config);
            mongodExe.start();
        }
    }
}
