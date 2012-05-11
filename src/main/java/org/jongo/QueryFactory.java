package org.jongo;

import org.jongo.marshall.BSONMarshaller;
import org.jongo.marshall.Marshaller;

public class QueryFactory {

    private final ParameterBinder binder;

    public QueryFactory(Marshaller marshaller) {
        this.binder = new ParameterBinder(new BSONMarshaller(marshaller));
    }

    public Query createQuery(String query) {
        return new Query(query, binder);
    }

    public Query createQuery(String query, Object... parameters) {
        return new Query(query, binder, parameters);
    }
}
