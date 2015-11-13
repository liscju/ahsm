package attatrol.ahsm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemUtils {

    private FilesystemUtils() {

    }

    public static void delete(Path root)
            throws IOException {
        if (Files.isDirectory(root)) {
            DirectoryStream<Path> subPaths = Files.newDirectoryStream(root);
            for (Path path : subPaths) {
                delete(path);
            }
            subPaths.close();
            Files.delete(root);
        }
        else {
            Files.delete(root);
        }
    }

    public static void createDirectory(Path path)
            throws IOException {
        if (Files.exists(path)) {
            delete(path);
        }
        Files.createDirectory(path);
    }

    public static void copy(Path source, Path destination)
            throws IllegalArgumentException,
            IOException {
        if (Files.isDirectory(source)) {
            if (Files.exists(destination)) {
                if (!Files.isDirectory(destination)) {
                    throw new IllegalArgumentException(String.format(
                            "fail to copy %s to %s because former is not a directory",
                            source.toString(),
                            destination.toString()));
                }
            }
            else {
                Files.createDirectory(destination);
            }
            DirectoryStream<Path> subPaths = Files.newDirectoryStream(source);
            for (Path path : subPaths) {
                final Path relativePath = source.relativize(path);
                copy(path, destination.resolve(relativePath));
            }
            subPaths.close();

        }
        else {
            Files.copy(source, destination);
        }
    }

    public static void copyFile(Path source, Path destination)
            throws IOException {
        Path destFolders = destination.getParent();
        if (Files.notExists(destFolders)) {
            Files.createDirectories(destFolders);
        }
        Files.copy(source, destination);
    }
    
    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    public static void exportResource(String resourceName, Path destination) throws IOException {
        
        //OutputStream out = Files.newOutputStream(destination);
        //InputStream in = null;
        /*try {
            in = FilesystemUtils.class.getResourceAsStream(resourceName);
            if(in == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }*/
         
         try(InputStream in = FilesystemUtils.class.getResourceAsStream(resourceName);
                 OutputStream out = Files.newOutputStream(destination)){
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, 4096);
            }
        } 
    }

}
