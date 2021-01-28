package org.jongo.query;

import java.util.LinkedList;

class Stack<T> {
    private final LinkedList<T> stack;
    private final T noValue;

    Stack(T noValue) {
        this.stack = new LinkedList<>();
        this.noValue = noValue;
    }

    public T peek() {
        if (stack.isEmpty()) {
            return noValue;
        }

        return stack.peekLast();
    }

    public T pop() {
        if (stack.isEmpty()) {
            return noValue;
        }

        return this.stack.removeLast();
    }

    public void push(T value) {
        this.stack.addLast(value);
    }
}
