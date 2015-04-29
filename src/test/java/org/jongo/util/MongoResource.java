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
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.NullProcessor;
import de.flapdoodle.embed.process.runtime.Network;

import java.net.UnknownHostException;

public class MongoResource {

    public DB getDb(String dbname) {
        return getInstance().getDB(dbname);
    }

    private Mongo getInstance() {
        String isLocal = System.getProperty("jongo.test.mongo.local");
        if (isLocal != null && isLocal.equals("true")) {
            return LocalMongo.instance;
        } else {
            return EmbeddedMongo.instance;
        }
    }

    /**
     * Launches an embedded Mongod server instance in a separate process.
     *
     * @author Alexandre Dutra
     */
    private static class EmbeddedMongo {

        private static MongoClient instance = getInstance();

        private static MongoClient getInstance() {
            try {
                Command mongoD = Command.MongoD;
                int port = RandomPortNumberGenerator.pickAvailableRandomEphemeralPortNumber();

                ArtifactStoreBuilder artifactStoreBuilder = new ArtifactStoreBuilder();
                artifactStoreBuilder.defaults(mongoD);
                artifactStoreBuilder.executableNaming(new UserTempNaming());

                IStreamProcessor output = new NullProcessor();
                ProcessOutput processOutput = new ProcessOutput(output, output, output);

                IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                        .defaults(mongoD)
                        .processOutput(processOutput)
                        .artifactStore(artifactStoreBuilder.build())
                        .build();

                Net network = new Net(port, Network.localhostIsIPv6());
                IMongodConfig mongodConfig = new MongodConfigBuilder()
                        .version(getVersion())
                        .net(network)
                        .build();

                MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig).start();

                return createClient(port);

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }

        private static Version.Main getVersion() {
            String version = System.getProperty("jongo.test.db.version");
            if (version == null) {
                return Version.Main.PRODUCTION;
            }
            return Version.Main.valueOf("V" + version.replaceAll("\\.", "_"));
        }
    }

    public static class LocalMongo {

        public static MongoClient instance = getInstance();

        private static MongoClient getInstance() {
            try {
                return createClient(27017);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize local Mongo instance: " + e, e);
            }
        }
    }

    private static MongoClient createClient(int port) throws UnknownHostException {
        MongoClient mongo = new MongoClient("127.0.0.1", port);
        mongo.setWriteConcern(WriteConcern.FSYNC_SAFE);
        return mongo;
    }

}
