package com.nudge.ecs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 * Writing statistics to .csv format (graphs).
 *
 * @author Frederik Dahl
 * 08/09/2021
 */


public class CSVEntryList implements Iterable<String> {

    private static final String EOL = "\n";
    private static final String SPR = ",";
    private static final String QUO = "\"";

    private final ArrayList<String> entries;
    private final int cols;

    public CSVEntryList(String... headers) {
        entries = new ArrayList<>();
        if (headers.length == 0) cols = 1;
        else {
            cols = headers.length;
            newEntry((Object[]) headers);
        }
    }

    public void newEntry(Object... cells) {
        if (cells.length != cols)
            throw new IllegalStateException("cell count must match columns");
        StringBuilder sb = new StringBuilder();
        for (Object obj : cells) {
            if (obj == null) throw new IllegalArgumentException("null cell");
            if (obj instanceof String)
                sb.append(QUO).append(obj).append(QUO).append(SPR);
            else sb.append(obj).append(SPR);}
        sb.replace(sb.length()-1,sb.length(),EOL);
        entries.add(sb.toString());
    }


    public int currentSize() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    @Override
    public Iterator<String> iterator() {
        return entries.iterator();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        entries.forEach(action);
    }

    @Override
    public Spliterator<String> spliterator() {
        return entries.spliterator();
    }
}
