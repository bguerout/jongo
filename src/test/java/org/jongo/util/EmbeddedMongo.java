package org.jongo.util;

import java.io.IOException;

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

/**
 * Special subclass of {@link MongoClient} that
 * launches an embedded Mongod server instance in a separate process.
 * 
 * @author Alexandre Dutra
 */
public class EmbeddedMongo extends MongoClient {

	public static final String DEFAULT_HOST = "127.0.0.1";

	public static final int DEFAULT_PORT = 27017;

	public static final WriteConcern DEFAULT_WRITE_CONCERN = WriteConcern.FSYNC_SAFE;
	
	public static final Version DEFAULT_VERSION = Version.V2_2_4;

	public EmbeddedMongo() throws MongoException, IOException {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	public EmbeddedMongo(int port) throws MongoException, IOException {
		this(DEFAULT_HOST, port);
	}

	public EmbeddedMongo(String host, int port) throws MongoException, IOException {
		this(host, port, DEFAULT_WRITE_CONCERN);
	}

	public EmbeddedMongo(String host, int port, WriteConcern writeConcern) throws MongoException, IOException {
		this(host, port, writeConcern, DEFAULT_VERSION);
	}
	
	public EmbeddedMongo(String host, int port, WriteConcern writeConcern, Version version) throws MongoException, IOException {
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
