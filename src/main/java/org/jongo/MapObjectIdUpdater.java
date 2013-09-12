package org.jongo;

import org.bson.types.ObjectId;

import java.util.Map;

public class MapObjectIdUpdater implements ObjectIdUpdater<Map> {
    public boolean mustGenerateObjectId(Map map) {
        return !map.containsKey("_id");
    }

    public Object getId(Map map) {
        return map.get("_id");
    }

    public void setObjectId(Map map, ObjectId id) {
        map.put("_id", id);
    }
}
