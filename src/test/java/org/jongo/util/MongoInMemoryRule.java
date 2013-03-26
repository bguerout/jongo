package org.jongo.util;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.rules.ExternalResource;

/**
 * @author dwu
 */
public class MongoInMemoryRule extends ExternalResource {

    public static final int PORT = 27017;
    private MongodExecutable mongoExec;

    @Override
    protected void before() throws Throwable {
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, PORT, Network.localhostIsIPv6());

        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongoExec = runtime.prepare(mongodConfig);
        mongoExec.start();
    }

    @Override
    protected void after() {
        if (mongoExec != null) {
            mongoExec.stop();
        }
    }
}
