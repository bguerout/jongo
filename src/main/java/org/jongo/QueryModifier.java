package org.jongo;

import com.mongodb.DBCursor;

public interface QueryModifier {
    void modify(DBCursor cursor);
}
