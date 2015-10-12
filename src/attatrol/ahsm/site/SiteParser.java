package attatrol.ahsm.site;

import java.io.IOException;
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
    return doc;
    
  }


}
