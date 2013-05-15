package org.jongo.util;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.junit.rules.ExternalResource;

public class EmbeddedMongoRule extends ExternalResource {

    public DB getDb(String dbname) {
        return LocalMongo.instance.getDB(dbname);
    }

    private static class LocalMongo {

        private static MongoClient instance = getLocalInstance();

        private static MongoClient getLocalInstance() {
            try {
                int port = RandomPortNumberGenerator.pickAvailableRandomEphemeralPortNumber();
                EmbeddedMongo mongo = new EmbeddedMongo(port);
                mongo.setWriteConcern(WriteConcern.FSYNC_SAFE);
                return mongo;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
            }
        }
    }


}
