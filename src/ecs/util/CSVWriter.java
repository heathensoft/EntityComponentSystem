package ecs.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * @author Frederik Dahl
 * 12/09/2021
 */


public class CSVWriter {

    private final File file;
    private BufferedWriter out;
    private final CSVEntryList entries;


    public CSVWriter(File file, String... columnDescriptors) {
        this.entries = new CSVEntryList(columnDescriptors);
        this.file = file;
    }

    public void newEntry(Object... cells) {
        entries.newEntry(cells);
    }

    public void write() {
        try {
            out = FileUtils.newBufferedWriter(file,true);
            for (String entry: entries)
                out.write(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            entries.clear();
            try {
                out.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getFile() {
        return file;
    }


}
