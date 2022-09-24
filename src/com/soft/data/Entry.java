package com.soft.data;

import com.soft.util.Browser;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Entry implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String url;
    protected transient Document document;

    public Entry(String url) {
        this.url = url;
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

    protected <T> T get(T t, Consumer<T> setter, Function<Document, T> parser) {
        if (t != null) return t;
        setter.accept(t = parser.apply(document()));
        return t;
    }

    protected <T> void trySet(T t, Supplier<T> supplier, Consumer<T> setter) {
        if (t == null) setter.accept(supplier.get());
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