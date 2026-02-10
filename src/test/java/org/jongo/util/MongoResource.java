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
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.StreamProcessor;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.ImmutableStart;
import de.flapdoodle.reverse.transitions.Start;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MongoResource {

    private static final StreamProcessor NOOP_STREAM_PROCESSOR = new StreamProcessor() {
        @Override
        public void process(String block) {
            // intentionally no-op: keep embedded mongod completely silent for tests
        }

        @Override
        public void onProcessed() {
            // no-op
        }
    };


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
        private static Path dbPath;

        private static MongoClient getInstance() {
            try {
                Version version = getVersion();

                MongodArguments arguments = MongodArguments.builder()
                        .isQuiet(true)
                        .build();
                ImmutableStart<MongodArguments> transition = Start.to(MongodArguments.class).initializedWith(arguments);

                ProcessOutput silentOutput = ProcessOutput.builder()
                        .output(NOOP_STREAM_PROCESSOR)
                        .error(NOOP_STREAM_PROCESSOR)
                        .commands(NOOP_STREAM_PROCESSOR)
                        .build();
                ImmutableStart<ProcessOutput> outputTransition = Start.to(ProcessOutput.class)
                        .initializedWith(silentOutput);

                ImmutableMongod.Builder builder = Mongod.builder()
                        .mongodArguments(transition)
                        .processOutput(outputTransition);

                EmbeddedMongo.dbPath = EmbeddedMongo.createDbPath();
                if (EmbeddedMongo.dbPath != null) {
                    ImmutableStart<DatabaseDir> dbDirTransition = Start.to(DatabaseDir.class)
                            .initializedWith(DatabaseDir.of(EmbeddedMongo.dbPath));
                    builder.databaseDir(dbDirTransition);
                }

                runningMongod = builder.build().start(version);

                de.flapdoodle.embed.mongo.commands.ServerAddress address = runningMongod.current().getServerAddress();
                MongoClient client = createClient(address.getHost(), address.getPort());
                addShutdownHook(client);
                return client;

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }

        private static Version getVersion() {
            String version = System.getProperty("embedmongo.version");
            if (version == null) {
                return Version.V8_2_3;
            }

            Version v = Version.valueOf("V" + version.replaceAll("\\.", "_"));
            System.out.println("Using MongoDB version: " + v);
            return v;

        }

        private static Path createDbPath() {
            String dbPath = System.getProperty("embedmongo.dbpath");
            if (dbPath == null) {
                return null;
            }

            try {
                String suffix = java.util.UUID.randomUUID().toString().substring(0, 5);
                Path path = Paths.get(dbPath, "embedmongodb-" + suffix);
                Files.createDirectories(path);
                return path;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create embedded mongo dbpath", e);
            }
        }
    }

    private static class LocalMongo {

        private static MongoClient instance = getInstance();

        private static MongoClient getInstance() {
            try {
                String port = System.getProperty("localmongo.port");
                if (port == null) {
                    port = "27017";
                }

                return createClient("127.0.0.1", Integer.parseInt(port));
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize local Mongo instance: " + e, e);
            }
        }
    }

    private static void addShutdownHook(MongoClient client) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.close();

                if (EmbeddedMongo.runningMongod != null) {
                    EmbeddedMongo.runningMongod.close();
                }

                if (EmbeddedMongo.dbPath != null) {
                    deleteRecursively(EmbeddedMongo.dbPath);
                }
            } catch (Exception e) {
                System.err.println("Failed to shutdown embedded mongo instance: " + e);
            }
        }));
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }

        List<IOException> failures = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            failures.add(e);
                        }
                    });
        }

        if (!failures.isEmpty()) {
            IOException exception = new IOException("Failed to delete some files");
            failures.forEach(exception::addSuppressed);
            throw exception;
        }
    }

    private static MongoClient createClient(String host, int port) throws UnknownHostException {
        return new MongoClient(
                new ServerAddress(host, port),
                MongoClientOptions.builder()
                        .writeConcern(WriteConcern.MAJORITY)
                        .build());
    }
}
