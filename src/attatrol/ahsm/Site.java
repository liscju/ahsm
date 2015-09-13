package attatrol.ahsm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author atta_troll
 *
 */
public class Site {
  /**
   * Flag of static initialization.
   */
  private static boolean sHasProjectName = false;

  /**
   * Project name.
   */
  private static String sProjectName;

  /**
   * List of files which are essentual for a narrow success.
   */
  private static Path[] sObligateFiles = { Paths.get("index.html"), null, null, };

  /**
   * List of folders which are essentual for a narrow success.
   */
  private static final Path[] OBLIGATE_FOLDERS = new Path[] { Paths.get("images"),
      Paths.get("xref"), Paths.get("css"), };

  /**
   * Parsed contents of the site.
   */
  private Map<Path, ArrayList<String>> contents = new TreeMap<>();

  /**
   * Absolutized path to the site folder.
   */
  private final Path path;

  private Site(Path path) {
    this.path = path;
  }

  /**
   * Static factory method, perform basic verification of the site files.
   * 
   * @param path
   *          , path to the site.
   * @param project
   *          , name of the project.
   * @return instance of Site.
   * @throws IllegalArgumentException
   *           , if site integrity is broken.
   */
  public static Site create(Path path, String project) throws IllegalArgumentException {
    if (!sHasProjectName) {
      sObligateFiles[1] = Paths.get(project + ".html");
      sObligateFiles[2] = Paths.get(project + ".rss");
      sProjectName = project;
      sHasProjectName = true;
    }
    if (Files.exists(path) && (Files.isDirectory(path))) {
      boolean isValidSite = true;
      for (Path p : sObligateFiles) {
        final Path fullP = path.resolve(p);
        isValidSite &= Files.exists(fullP) && Files.isReadable(fullP) && Files.isRegularFile(fullP);
        if (!isValidSite) {
          throw new IllegalArgumentException("cant resolve file: " + fullP.toString());
        }
      }
      for (Path p : OBLIGATE_FOLDERS) {
        final Path fullP = path.resolve(p);
        isValidSite &= Files.exists(fullP) && Files.isReadable(fullP) && Files.isDirectory(fullP);
        if (!isValidSite) {
          throw new IllegalArgumentException("cant resolve folder: " + fullP.toString());
        }
      }
      return new Site(path);
    } else {
      throw new IllegalArgumentException("site doesnt exist");
    }
  }

  /**
   * fills contents map with essentual data from the site.
   * @return
   */
  public SiteReport fill() {
    return null;
  }
  
  public static Site merge(Site site1, Site site2) throws IOException {
    return null;
  }

}
