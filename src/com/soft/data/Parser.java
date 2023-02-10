package com.soft.data;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Parser<T> {

    T parse(Document document);

    default Supplier<T> toSupplier(Entry entry) {
        return () -> parse(entry.document());
    }

    static <T> T parseFirst(Element document, String query, Function<Element, T> parser) {
        Element element = document.selectFirst(query);
        return element == null ? null : parser.apply(element);
    }
}