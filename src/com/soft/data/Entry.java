package com.soft.data;

import com.soft.util.Browser;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Entry implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String url;
    private transient Document document;

    private final Map<String, Value<?>> values;

    public Entry(String url) {
        this.url = url;
        this.values = new HashMap<>();
    }

    public abstract String type();

    public abstract Storage storage();

    public void load() {
        toString();
    }

    public boolean isLoaded() {
        return values.values().stream().allMatch(Value::isLoaded);
    }

    public String url() {
        return url;
    }

    public Document document() {
        return document == null ? document = Browser.connect(url) : document;
    }

    public void dispose() {
        document = null;
    }

    protected <T> Value<T> getValue(String name, Parser<T> parser) {
        Value<T> value = (Value<T>) values.computeIfAbsent(name, s -> new Value<>());
        value.setParser(parser.toSupplier(this));
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return url.equals(((Entry) o).url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}