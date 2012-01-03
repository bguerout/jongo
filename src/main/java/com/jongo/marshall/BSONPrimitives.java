package com.jongo.marshall;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BSONPrimitives
{
    private static Set<Class<?>> primitives;
    
    static {
        primitives = new HashSet<Class<?>>();
        primitives.add(String.class);
        primitives.add(Integer.class);
        primitives.add(int.class);
        primitives.add(Double.class);
        primitives.add(double.class);
        primitives.add(Long.class);
        primitives.add(long.class);
        primitives.add(Boolean.class);
        primitives.add(boolean.class);
        primitives.add(Date.class);
    }
    
    public static <T> boolean contains(Class<T> clazz)
    {
        return primitives.contains(clazz);
    }
}
