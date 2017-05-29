package org.jongo;

/**
 * A functional interface that takes a value and returns another value.
 *
 * @param <T> the input value type
 * @param <R> the output value type
 */
public interface Function<T, R> {

    /**
     * Takes value of type T and transforms it to type R
     * @param t input value
     * @return output value
     */
    R apply(T t);

}
