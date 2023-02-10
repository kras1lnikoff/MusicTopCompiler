package com.soft.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public class Value<T> implements Serializable {

    private T value;

    private boolean parsed;
    private transient Supplier<T> parser;

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(get(), ((Value<?>) o).get());
    }

    @Override
    public String toString() {
        return Objects.toString(get(), "N/A");
    }

    protected void setParser(Supplier<T> parser) {
        this.parser = parser;
    }

    public T get() {
        return isPresent() ? stored() : parse();
    }

    public boolean isLoaded() {
        return value instanceof Entry ? ((Entry) value).isLoaded() : isPresent();
    }

    public void clear() {
        set(null);
    }

    public boolean isEmpty() {
        return get() == null;
    }

    public boolean isPresent() {
        return value != null || parsed;
    }

    public void trySet(T value) {
        if (!isPresent()) set(value);
    }

    public T stored() {
        return value;
    }

    public T parse() {
        value = parser.get();
        parsed = true;
        return value;
    }

    public void set(T value) {
        this.value = value;
        parsed = false;
    }
}