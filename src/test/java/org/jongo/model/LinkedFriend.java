package org.jongo.model;

import org.bson.types.ObjectId;

public class LinkedFriend extends Friend {
    ObjectId friendRelationId;

    public LinkedFriend(ObjectId friendRelationId) {
        this.friendRelationId = friendRelationId;
    }

    public ObjectId getRelationId() {
        return friendRelationId;
    }
}
