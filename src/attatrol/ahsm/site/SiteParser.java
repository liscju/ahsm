package attatrol.ahsm.site;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import attatrol.ahsm.FilesystemUtils;

/**
 * Transmutes site data from files on disc into SiteContent format
 * and in opposite direction.
 * @author atta_troll
 *
 */
public class SiteParser {
  public static SiteContent parse(Path sitePath, String simpleName) throws IOException {
    final Document doc = Jsoup.parse(sitePath.toFile(), "UTF-8");
    final Element table = doc.getElementById("contentBox");
    final Element summarySection = table.child(1);
    final Element tableSection = table.child(3);
    Iterator<Element> sections = tableSection.getElementsByClass("section").iterator();
    Map<String,Element> fileTables = new HashMap<>();
    //skipping fileTables itself
    sections.next();
    while(sections.hasNext()) {
      final Element section = sections.next();
      final String name = section.children().first().id();
      fileTables.put(name,section);

    }
    SiteContent content = new SiteContent(fileTables, summarySection, simpleName);
    return content;
  }
  
  public static Document generateFromTemplate(Path sitePath, SiteContent content) throws IOException {
    final Document doc = Jsoup.parse(sitePath.toFile(), "UTF-8");
    final Element table = doc.getElementById("contentBox");
    final Element summarySection = table.child(1);
    final Element tableSection = table.child(3);
    summarySection.replaceWith(content.getSummarySection());
    final Attributes attrs = new Attributes();
    attrs.put("class","section");
    Element mergedTableSection = new Element(Tag.valueOf("div"), "", attrs);
    final Map<String,Element> entries = content.getFileTables();
    List<String> keys = new ArrayList<String>(entries.keySet());
    Collections.sort(keys);
    for(String key:keys) {
      mergedTableSection.appendChild(entries.get(key));
      mergedTableSection.appendText("\n");
    }
    tableSection.replaceWith(mergedTableSection);
    //delete irrelevant
    doc.getElementsByAttributeValue("id", "leftColumn").first().remove();
    return doc;
    
  }

  public static void exportToFile(SiteContent mergedContent, Path resultPath, Path templatePath, String projectName) throws IOException {
    Path resultSubject = resultPath.resolve(projectName + "_merged.html");
    try {
      FilesystemUtils.delete(resultSubject);
    } catch (NoSuchFileException e) {
      // empty
    }
    Files.createFile(resultSubject);
    final Document resultDoc = SiteParser.generateFromTemplate(templatePath, mergedContent);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultSubject.toFile()))) {
      bw.write(resultDoc.outerHtml());
      bw.close();
    } catch (FileNotFoundException ex) {
      System.out.println(ex.toString());
    }
  }

}
