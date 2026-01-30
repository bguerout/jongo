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
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;

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
        private static TransitionWalker.ReachedState<RunningMongodProcess> runningMongod;

        private static MongoClient getInstance() {
            try {
                applyArtifactStoreOverride();
                Version version = getVersion();
                runningMongod = Mongod.instance().start(version);
                de.flapdoodle.embed.mongo.commands.ServerAddress address = runningMongod.current().getServerAddress();
                MongoClient client = createClient(address.getHost(), address.getPort());
                addShutdownHook(client);
                return client;

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }

        private static void applyArtifactStoreOverride() {
            String path = System.getProperty("jongo.test.embedmongo.dir");
            if (path == null || path.trim().isEmpty()) {
                return;
            }
            System.setProperty("de.flapdoodle.embed.mongo.artifacts", path);
        }

        private static Version getVersion() {
            String version = System.getProperty("embedmongo.version");
            if (version == null) {
                return Version.V4_0_12;
            }
            return Version.valueOf("V" + version.replaceAll("\\.", "_"));
        }
    }

    private static class LocalMongo {

        private static MongoClient instance = getInstance();

        private static MongoClient getInstance() {
            try {
                return createClient("127.0.0.1", 27017);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize local Mongo instance: " + e, e);
            }
        }
    }

    private static void addShutdownHook(MongoClient client) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.close();
            } catch (Exception ignored) {
            }
            try {
                if (EmbeddedMongo.runningMongod != null) {
                    EmbeddedMongo.runningMongod.close();
                }
            } catch (Exception ignored) {
            }
        }));
    }

    private static MongoClient createClient(String host, int port) throws UnknownHostException {
        return new MongoClient(
                new ServerAddress(host, port),
                MongoClientOptions.builder()
                        .writeConcern(WriteConcern.MAJORITY)
                        .build());
    }
}
