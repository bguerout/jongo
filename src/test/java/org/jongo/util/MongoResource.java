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

package org.jongo.util;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.io.NullProcessor;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.ExtractedArtifactStore;

import java.net.UnknownHostException;

public class MongoResource {

    public DB getDb(String dbname) {
        return getInstance().getDB(dbname);
    }

    public MongoDatabase getDatabase(String dbname) {
        return getInstance().getDatabase(dbname);
    }

    public MongoClient getInstance() {
        String isDisabled = System.getProperty("embedmongo.disabled");
        if (isDisabled != null && isDisabled.equals("true")) {
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

                DownloadConfig downloadConfig = Defaults.downloadConfigFor(mongoD)
                        .artifactStorePath(getMongoPath())
                        .build();

                ExtractedArtifactStore artifactStore = Defaults.extractedArtifactStoreFor(mongoD)
                        .withDownloadConfig(downloadConfig);

                StreamProcessor output = new NullProcessor();
                ProcessOutput processOutput = new ProcessOutput(output, output, output);

                RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(mongoD)
                        .processOutput(processOutput)
                        .artifactStore(artifactStore)
                        .build();

                Net network = new Net(port, Network.localhostIsIPv6());
                Version version = getVersion();


                ImmutableMongoCmdOptions.Builder mongoCmdOptionsBuilder = MongoCmdOptions.builder();
                if (version.compareTo(Version.V3_2_0) > -1) {
                    mongoCmdOptionsBuilder.storageEngine("ephemeralForTest");
                }

                MongodConfig mongodConfig = MongodConfig.builder()
                        .version(version)
                        .cmdOptions(mongoCmdOptionsBuilder.build())
                        .net(network)
                        .build();

                MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig).start();

                return createClient(port);

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }

        private static Directory getMongoPath() {
            String path = System.getProperty("jongo.test.embedmongo.dir");
            if (path == null) {
                return new UserHome(".embedmongo");
            }
            return new FixedPath(path);
        }

        private static Version getVersion() {
            String version = System.getProperty("embedmongo.version");
            if (version == null) {
                return Version.V4_0_2;
            }
            return Version.valueOf("V" + version.replaceAll("\\.", "_"));
        }
    }

    private static class LocalMongo {

        private static MongoClient instance = getInstance();

        private static MongoClient getInstance() {
            try {
                return createClient(27017);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize local Mongo instance: " + e, e);
            }
        }
    }

    private static MongoClient createClient(int port) throws UnknownHostException {
        return new MongoClient(
                new ServerAddress("127.0.0.1", port),
                MongoClientOptions.builder()
                        .writeConcern(WriteConcern.MAJORITY)
                        .build());
    }
}
