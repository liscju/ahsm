package attatrol.ahsm;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemUtils {

  private FilesystemUtils() {

  }

  public static void delete(Path root) throws IOException {
    if (Files.isDirectory(root)) {
      DirectoryStream<Path> subPaths = Files.newDirectoryStream(root);
      for (Path path : subPaths) {
        delete(path);
      }
      subPaths.close();
      Files.delete(root);
    } else {
      Files.delete(root);
    }
  }

  public static void createDirectory(Path path) throws IOException {
    if (Files.exists(path)) {
      delete(path);
    }
      Files.createDirectory(path);
  }

  public static void copy(Path source, Path destination) throws IllegalArgumentException,
      IOException {
    if (Files.isDirectory(source)) {
      if (Files.exists(destination)) {
        if (!Files.isDirectory(destination))  {
          throw new IllegalArgumentException(String.format(
              "fail to copy %s to %s because former is not a directory", source.toString(),
              destination.toString()));
        }
      } else {
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
  
  public static void copyFile(Path source, Path destination) throws IOException {
    Path destFolders = destination.getParent();
    if(Files.notExists(destFolders)) {
      Files.createDirectories(destFolders);
    }
    Files.copy(source, destination);
  }

}
