package org.jongo.util;

import com.mongodb.DBObject;
import org.jongo.ResultMapper;

public class DBObjectResultMapper implements ResultMapper<DBObject> {
    public DBObject map(DBObject dbObject) {
        return dbObject;
    }
}
