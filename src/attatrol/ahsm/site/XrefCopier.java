package attatrol.ahsm.site;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import static attatrol.ahsm.site.InitialSiteVerifier.XREF_FOLDER;

import org.jsoup.nodes.Element;

import attatrol.ahsm.FilesystemUtils;

public class XrefCopier {
  private XrefCopier() {

  }

  public static void copyXrefFiles(Map<String, Element> map, Path path1, Path path2, Path pathResult)
      throws IllegalArgumentException, IOException {
    //Path xRefFolder1 = path1.resolve(XREF_FOLDER);
    //Path xRefFolder2 = path2.resolve(XREF_FOLDER);
    //Path xRefFolderResult = pathResult.resolve(XREF_FOLDER);
    List<Element> elements = new ArrayList<>(map.values());
    List<Path> paths = new ArrayList<>();
    for (Element element : elements) {
      paths.add(getXRefPath(element));
    }
    for (Path path : paths) {
      try {
        FilesystemUtils.copyFile(path1.resolve(path), pathResult.resolve(path));
      } catch (Exception e) {
        FilesystemUtils.copy(path2.resolve(path), pathResult.resolve(path));
      }
    }

  }

  private static Path getXRefPath(Element element) {
    Element link = element.child(1).child(1).child(4).child(0);
    String url = link.attr("href");
    url = url.substring(0, url.lastIndexOf('#'));
    return Paths.get(url);
  }

}
