package com.soft;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static javax.swing.JOptionPane.*;

public final class Display {

    public static <T> void list(List<T> values, Object message, String title) {
        list(buildList(values, false), message, title);
    }

    public static <T> void list(JList<T> list, Object message, String title) {
        listShow(list, message, title, INFORMATION_MESSAGE);
    }

    public static <T> T select(List<T> values, Object message, String title) {
        return select(values, message, title, () -> null);
    }

    public static <T> T select(List<T> values, Object message, String title, Supplier<T> defaultValue) {
        return single(select(values, message, title, false, defaultValue));
    }

    public static <T> List<T> selectMultiple(List<T> values, Object message, String title) {
        return select(values, message, title, true, () -> null);
    }

    public static <T> List<T> select(List<T> values, Object message, String title, boolean multiple, Supplier<T> defaultValue) {
        return select(buildList(values, multiple), message, title, defaultValue);
    }

    public static <T> List<T> select(JList<T> list, Object message, String title, Supplier<T> defaultValue) {
        listShow(list, message, title, QUESTION_MESSAGE);
        return compile(list.isFocusable(), defaultValue, list);
    }

    public static <T> T search(List<T> values, Object search, Object message,
                               String title, BiFunction<List<T>, String, List<T>> filter) {
        return search(values, search, message, title, filter, input -> null);
    }

    public static <T> T search(List<T> values, Object search, Object message, String title,
                               BiFunction<List<T>, String, List<T>> filter, Function<String, T> inputValue) {
        return single(search(values, search, message, title, false, filter, inputValue));
    }

    public static <T> List<T> searchMultiple(List<T> values, Object search, Object message,
                                             String title, BiFunction<List<T>, String, List<T>> filter) {
        return search(values, search, message, title, true, filter, input -> null);
    }

    public static <T> List<T> search(List<T> values, Object search, Object message, String title, boolean multiple,
                                     BiFunction<List<T>, String, List<T>> filter, Function<String, T> inputValue) {
        return search(buildList(values, multiple), search, message, title, filter, inputValue);
    }

    public static <T> List<T> search(JList<T> list, Object search, Object message, String title,
                                     BiFunction<List<T>, String, List<T>> filter, Function<String, T> inputValue) {
        ListModel<T> model = list.getModel();
        List<T> values = IntStream.range(0, model.getSize()).mapToObj(model::getElementAt).toList();
        listSearch(list, search, message, title, QUESTION_MESSAGE, string -> filter.apply(values, string));
        return compile(list.isFocusable(), () -> inputValue.apply(list.getName()), list);
    }

    private static <T> JList<T> buildList(List<T> values, boolean multiple) {
        JList<T> list = new JList<>(new Vector<>(values));
        list.setVisibleRowCount(Math.min(values.size(), 10));
        if (!multiple) list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return list;
    }

    public static <T> T single(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    private static <T> List<T> compile(boolean focus, Supplier<T> getter, JList<T> list) {
        boolean single = list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION;
        List<T> selected = new ArrayList<>();
        if (!focus) return selected;
        selected.addAll(list.getSelectedValuesList());
        if (single && !selected.isEmpty()) return selected;
        T value = getter.get();
        if (value != null) selected.add(value);
        return selected;
    }

    public static boolean confirm(Object message, String title) {
        return showConfirmDialog(null, message, title, YES_NO_OPTION, QUESTION_MESSAGE) == YES_OPTION;
    }

    public static String ask(Object message, String title) {
        return showInputDialog(null, message, title, QUESTION_MESSAGE);
    }

    public static void info(Object message, String title) {
        showMessageDialog(null, message, title, INFORMATION_MESSAGE);
    }

    public static <T> void listShow(JList<T> list, Object message, String title, int type) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        if (list.getModel().getSize() == 0) scrollPane = null;
        Object paneMessage = new Object[]{message, scrollPane};
        JOptionPane pane = new JOptionPane(paneMessage, type, DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(title);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        KeyEventDispatcher dispatcher = event -> {
            dispatch(manager, list, event);
            return true;
        };
        manager.addKeyEventDispatcher(dispatcher);
        dialog.setVisible(true);
        dialog.dispose();
        manager.removeKeyEventDispatcher(dispatcher);
        Object value = pane.getValue();
        if (value instanceof Integer && (int) value == OK_OPTION) return;
        list.setFocusable(false);
    }

    public static <T> void listSearch(JList<T> list, Object search, Object message,
                                      String title, int type, Function<String, List<T>> filter) {
        JTextField field = new JTextField();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        if (list.getModel().getSize() == 0) scrollPane = null;
        Object paneMessage = new Object[]{search, field, message, scrollPane};
        JOptionPane pane = new JOptionPane(paneMessage, type, DEFAULT_OPTION);
        JDialog dialog = pane.createDialog(title);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        AtomicReference<FilterThread<T>> ref = new AtomicReference<>();
        list.setName("");
        KeyEventDispatcher dispatcher = event -> {
            String text = field.getText();
            if (text.isEmpty() && event.getKeyCode() == '\b') return true;
            if (field.hasFocus()) {
                dispatch(manager, field, event);
                dispatch(manager, list, event);
            } else {
                dispatch(manager, list, event);
                dispatch(manager, field, event);
            }
            FilterThread<T> thread = ref.get();
            if (text.equals(list.getName())) return true;
            if (thread != null) thread.interrupt();
            thread = new FilterThread<>(text, list, filter);
            list.setName(text);
            ref.set(thread);
            thread.start();
            return true;
        };
        manager.addKeyEventDispatcher(dispatcher);
        dialog.setVisible(true);
        dialog.dispose();
        manager.removeKeyEventDispatcher(dispatcher);
        Object value = pane.getValue();
        if (value instanceof Integer && (int) value == OK_OPTION) return;
        list.setFocusable(false);
    }

    private static void dispatch(KeyboardFocusManager manager, Component component, KeyEvent event) {
        try {
            manager.redispatchEvent(component, event);
        } catch (Exception ignored) {
        }
    }

    private static class FilterThread<T> extends Thread {

        private final String text;
        private final JList<T> list;
        private final Function<String, List<T>> filter;

        private FilterThread(String text, JList<T> list, Function<String, List<T>> filter) {
            this.text = text;
            this.list = list;
            this.filter = filter;
        }

        @Override
        public void run() {
            int index = list.getLeadSelectionIndex();
            ListModel<T> model = list.getModel();
            T lead = index < 0 || index >= model.getSize() ? null : model.getElementAt(index);
            List<T> newValues = filter.apply(text), selected = list.getSelectedValuesList();
            if (isInterrupted()) return;
            list.setListData(new Vector<>(newValues));
            model = list.getModel();
            int newIndex = 0;
            for (T value : selected) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).equals(value)) {
                        if (value.equals(lead)) newIndex = i;
                        list.addSelectionInterval(i, i);
                        break;
                    }
                }
            }
            list.ensureIndexIsVisible(newIndex);
        }
    }
}