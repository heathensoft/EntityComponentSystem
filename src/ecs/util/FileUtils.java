package ecs.util;

import java.io.*;

/**
 * @author Frederik Dahl
 * 11/09/2021
 */


public class FileUtils {

    static public final String INTERNAL_PREFIX;
    static public final String EXTERNAL_PREFIX;

    static {
        try {
            INTERNAL_PREFIX = new File("").getAbsolutePath() + File.separator;
            EXTERNAL_PREFIX = System.getProperty("user.home") + File.separator;
        }catch (SecurityException e) {
            throw new SecurityException("No permission to access System Properties",e);
        }
    }


    /**
     * universal method for creating or getting a file or folder in the Project hierarchy.
     * catches a number of exceptions under one IOException.
     * the returned file is guarantied to exist.
     *
     * @param overwrite whether to overwrite a non-directory file if one already exists.
     *                  does not support overwriting directories.
     * @param fileName If null, it will get or create a directory
     * @param folders the folders i.e. arg1\arg2\fileName
     * @return the file
     * @throws IOException "catch all" exception. DOES NOT handle SecurityExceptions
     */
    public static File internal(boolean overwrite, String fileName, String... folders)
            throws IOException {
        File directory = directoryFile(true,folders);
        return getOrCreate(directory,fileName,overwrite);
    }

    /**
     * universal method for creating or getting a file or folder under System.getProperty("user.home").
     * catches a number of exceptions under one IOException.
     * the returned file is guarantied to exist.
     *
     * @param overwrite whether to overwrite a non-directory file if one already exists.
     *                  does not support overwriting directories.
     * @param fileName If null, it will get or create a directory
     * @param folders the folders i.e. arg1\arg2\fileName
     * @return the file
     * @throws IOException "catch all" exception. DOES NOT handle SecurityExceptions
     */
    public static File external(boolean overwrite, String fileName, String... folders)
            throws IOException {
        File directory = directoryFile(false,folders);
        return getOrCreate(directory,fileName,overwrite);
    }

    public static FileWriter newFileWriter(File file, boolean append)
            throws IOException{
        return new FileWriter(file,append);
    }

    public static BufferedWriter newBufferedWriter(File file, boolean append, int bufferSize)
            throws IOException{
        return new BufferedWriter(newFileWriter(file,append),bufferSize);
    }

    public static BufferedWriter newBufferedWriter(File file, boolean append)
            throws IOException{
        return new BufferedWriter(newFileWriter(file,append));
    }

    private static File getOrCreate(File directory, String fileName, boolean overwrite)
            throws IOException {

        if (!validateDirectory(directory))
            throw new IOException("Folder does not exist, and could not create one");
        if (fileName == null) return directory;

        File file = new File(directory,fileName);
        if (file.exists()) {
            if (!overwrite) return file;
            if (file.isDirectory())
                throw new UnsupportedOperationException("Method does not support overwriting directories");
            if (!file.delete())
                throw new IOException("Could not overwrite existing file: " + file.getPath());
        }
        if (!file.createNewFile())
            throw new IOException("Could not create file: " + file.getPath());
        return file;
    }

    private static File directoryFile(boolean internal, String... folders) {
        String prefix = internal? INTERNAL_PREFIX : EXTERNAL_PREFIX;
        StringBuilder path = new StringBuilder(prefix);
        for (String directory : folders) {
            path.append(directory).append(File.separator);
        } return new File(path.toString());
    }

    private static boolean validateDirectory(File file) {
        if (file == null)
            throw new IllegalArgumentException("argument File cannot be null");
        if (file.isDirectory()) return true;
        return !file.isFile() && file.mkdirs();
    }
}
