package org.jongo.spike.dbref.jackson;

public interface ReferenceLink<T> {
    String getReferenceCollectionName(T obj);

    String getId(T obj);
}
