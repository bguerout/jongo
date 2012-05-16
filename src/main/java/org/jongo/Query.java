package org.jongo;

import com.mongodb.DBObject;

public interface Query {
    DBObject toDBObject();
}
