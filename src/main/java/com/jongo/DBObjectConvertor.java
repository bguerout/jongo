package com.jongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class DBObjectConvertor
{
    public static DBObject from(String query)
    {
        return ((DBObject) JSON.parse(query));
    }
}
