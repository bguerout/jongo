package org.jongo;

import com.mongodb.DBCursor;

public interface CursorModifier {
    void modify(DBCursor cursor);
}
