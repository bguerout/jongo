package org.jongo;

import org.jongo.marshall.NativeMarshaller;

 class QueryFactory {

    private final ParameterBinder binder;

    public QueryFactory() {
        this.binder = new ParameterBinder(new NativeMarshaller());
    }

    public Query createQuery(String query) {
        return new Query(binder, query);
    }

    public Query createQuery(String query, Object... parameters) {
        return new Query(binder, query, parameters);
    }
}
