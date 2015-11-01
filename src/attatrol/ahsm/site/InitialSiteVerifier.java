package attatrol.ahsm.site;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import attatrol.ahsm.FilesystemUtils;

/**
 * Utility class contain single method to check file structure of the report site.
 * 
 * @author atta_troll
 *
 */
public class InitialSiteVerifier {

  /**
   * List of folders which are essentual for a narrow success.
   */
  public static final Path XREF_FOLDER = Paths.get("xref");
  public static final Path CSS_FOLDER = Paths.get("css");
  public static final Path IMAGES_FOLDER = Paths.get("images");

  public static final Path[] XREF_FILES = new Path[] { Paths.get("stylesheet.css"),
      Paths.get("overview-summary.html"), Paths.get("overview-frame.html"), Paths.get("index.html"),
      Paths.get("allclasses-frame.html"), };

  public static final Path[] CSS_FILES = new Path[] { Paths.get("site.css"), Paths.get("print.css"),
      Paths.get("maven-base.css"), };

  public static final Path[] IMAGES_FILES = new Path[] { Paths.get("newwindow.png"),
      Paths.get("icon_warning_sml.gif"), Paths.get("icon_success_sml.gif"),
      Paths.get("icon_info_sml.gif"), Paths.get("icon_error_sml.gif"), Paths.get("external.png"),
      Paths.get("expanded.gif"), Paths.get("collapsed.gif"), Paths.get("rss.png"), };

  private InitialSiteVerifier() {

  }

  public static boolean verifySite(Path sitePath, String projectName) {
    final Path main = sitePath.resolve(projectName + ".html");
    boolean result = Files.exists(main) && Files.isRegularFile(main);
    result &= verifyFolder(sitePath, IMAGES_FOLDER, IMAGES_FILES);
    result &= verifyFolder(sitePath, CSS_FOLDER, CSS_FILES);
    result &= verifyFolder(sitePath, XREF_FOLDER, XREF_FILES);
    return result;
  }

  private static boolean verifyFolder(Path sitePath, Path folderPath, Path[] folderContent) {
    boolean result = true;
    final Path fullFolderPath = sitePath.resolve(folderPath);
    for (Path path : folderContent) {
      final Path fullPath = fullFolderPath.resolve(path);
      result &= Files.exists(fullPath) && Files.isRegularFile(fullPath);
    }
    return result;
  }

  public static void copyImmutableSiteFiles(Path pathSource, Path resultPath)
      throws IllegalArgumentException, IOException {
    FilesystemUtils.copy(pathSource.resolve(IMAGES_FOLDER), resultPath.resolve(IMAGES_FOLDER));
    FilesystemUtils.copy(pathSource.resolve(CSS_FOLDER), resultPath.resolve(CSS_FOLDER));
    final Path xRefSource = pathSource.resolve(XREF_FOLDER);
    final Path xRefDest = resultPath.resolve(XREF_FOLDER);
    Files.createDirectory(xRefDest);
    for (Path path : XREF_FILES) {
      FilesystemUtils.copy(xRefSource.resolve(path), xRefDest.resolve(path));
    }

    // replace with local maven-theme.css
    final Path cssReplaceDest = resultPath.resolve(CSS_FOLDER).resolve("maven-theme.css");
    FilesystemUtils.delete(cssReplaceDest);
    FilesystemUtils.copyFile(
        Paths.get(System.getProperty("user.dir")).resolve("resources").resolve("maven-theme.css"),
        cssReplaceDest);
  }

}
