package attatrol.ahsm.site;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * Handles data transition from html file to SiteContent instance and in the other direction.
 * 
 * @author atta_troll
 *
 */
public class SiteParser {

  public static final String HELP_FILENAME = "ahsm-help.html";

  public static final String[] HELP_TEXT = {
      "This is symmetric difference generated from two maven-site reports.",
      "All matching rows from each report are deleted, then remaining rows are merged into single report.",
      "Currently number of identical lines in each report is not taken into account." };

  public static final String SOURCE_LINK = "https://github.com/attatrol/ahsm";

  public static final String HELP_HTML_LINK = "ahsm-help.html";

  /**
   * Parsing site date from html maven site into SiteContent 
   * TODO overhaul ASAP!, reuse without absolute indexes, use search by tag
   * 
   * @param sitePath
   *          path to site
   * @param simpleName
   *          simple id for SiteContent TODO it should be generated inside from sitePath
   * @return resulting SiteContent
   * @throws IOException
   *           thrown on jsoup internal parsing error.
   */
  public static SiteContent parse(Path sitePath, String simpleName) throws IOException {
    final Document doc = Jsoup.parse(sitePath.toFile(), "UTF-8");
    final Element table = doc.getElementById("contentBox");
    // TODO simplify next 2 strings into final Element summarySection = table.child(1).clone();
    final Element summarySection = new Element(Tag.valueOf("div"), "").attr("class", "section");
    summarySection.appendChild(table.child(1).clone());
    final Element tableSection = table.child(3);
    Iterator<Element> sections = tableSection.getElementsByClass("section").iterator();
    Map<String, Element> fileTables = new HashMap<>();
    // skipping fileTables itself
    sections.next();
    // new Element(Tag.valueOf("div"), "").attr("class", "section");
    while (sections.hasNext()) {
      final Element section = sections.next();
      final String name = section.children().first().id();
      fileTables.put(name, section);
    }
    SiteContent content = new SiteContent(fileTables, summarySection, simpleName);
    return content;
  }

  /**
   * Exports SiteContent to the html file. Adds help html file.
   * 
   * @param mergedContent
   *          the SiteContent.
   * @param resultPath
   *          path to the html file.
   * @throws IOException
   *           thrown on i/o error while writing on disc.
   */
  public static void exportToFile(SiteContent mergedContent, Path resultPath) throws IOException {
    final Path helpPath = resultPath.getParent().resolve(HELP_FILENAME);

    final Document resultDoc = SiteParser.generateReportDoc(mergedContent);
    final Document helpDoc = SiteParser.generateHelpDoc(helpPath, resultPath);

    writeToFile(resultDoc, resultPath);
    writeToFile(helpDoc, helpPath);

  }

  /**
   * Creates new jsoup Document and fills it with SiteContent.
   * 
   * @param content
   *          SiteContent argument
   * @return jsoup Document.
   */
  private static Document generateReportDoc(SiteContent content) {
    Document doc = Document.createShell("");
    final Element contentBox = setBasicStructure(doc, content.getSimpleName());
    final Element titleSection = addTitledSection(contentBox, content.getSimpleName());
    final Element summarySection = addTitledSection(contentBox, "Summary:");
    final Element uniqueRowsSection = addTitledSection(contentBox, "Unique rows:");

    titleSection.appendElement("a").attr("href", HELP_HTML_LINK)
        .appendElement("h4").text("explanation");
    summarySection.appendChild(content.getSummarySection());
    summarySection.appendText("\n");
    final Map<String, Element> entries = content.getFileTables();
    List<String> keys = new ArrayList<String>(entries.keySet());
    Collections.sort(keys);
    for (String key : keys) {
        uniqueRowsSection.appendChild(entries.get(key));
        uniqueRowsSection.appendText("\n");
    }
    // empty lines for visual division:
    summarySection.appendElement("br");
    summarySection.appendElement("br");
    return doc;
  }

  /**
   * Generates ahsm help file as a jsoup Document.
   * 
   * @param helpPath
   *          path to help file
   * @param resultPath
   * @return
   */
  private static Document generateHelpDoc(Path helpPath, Path resultPath) {
    Document doc = Document.createShell("");
    final Element contentBox = setBasicStructure(doc, "ahsm help");
    final Element textSection = addTitledSection(contentBox, "Explanation:");
    for (String line : HELP_TEXT) {
      textSection.append(line);
      textSection.appendElement("br");
    }
    textSection.appendElement("br");
    textSection.appendElement("a").attr("href", SOURCE_LINK)
        .text("Utility that generated this report.");
    textSection.appendElement("h3").appendElement("a")
        .attr("href", resultPath.getName(resultPath.getNameCount() - 1).toString()).text("back to report");
    return doc;
  }

  /**
   * Writes out jsoup Document to html file.
   * 
   * @param doc
   *          jsoup Document instance.
   * @param resultPath
   *          resultPath path to the html file, expected that there is no file on this path.
   * @throws IOException
   *           thrown on i/o error while writing on disc.
   */
  private static void writeToFile(Document doc, Path resultPath) throws IOException {
    Files.createFile(resultPath);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultPath.toFile()))) {
      bw.write(doc.outerHtml());
      bw.close();
    } catch (FileNotFoundException ex) {
      System.out.println(ex.toString());
    }

  }

  /**
   * Populates head section of the empty document and adds basic structure to its body.
   * 
   * @param doc
   *          emptyDocument
   * @return contentDox, mounting point for any content.
   */
  private static Element setBasicStructure(Document doc, String title) {
    final Element head = doc.head();
    head.appendElement("title").text(title);
    head.appendElement("style").attr("type", "text/css").attr("media", "all")
        .text("@import url(\"./css/maven-base.css\");\n"
            + "@import url(\"./css/maven-theme.css\");\n" + "@import url(\"./css/site.css\"););");
    head.appendElement("link").attr("rel", "stylesheet").attr("href", "./css/print.css")
        .attr("type", "text/css").attr("media", "print");
    head.appendElement("http-equiv").attr("http-equiv", "Content-Language").attr("content", "en");
    doc.body().attr("class", "composite");
    return doc.body().appendElement("div").attr("id", "contentBox");
  }

  /**
   * Appends new section element with h2 header to the root.
   * 
   * @param root
   *          mounting point for new section.
   * @param title
   *          title for section.
   * @return the section element.
   */
  private static Element addTitledSection(Element root, String title) {
    final Element section = root.appendElement("div").attr("class", "section");
    section.appendText("\n").appendElement("h2").text(title).attr("a", title);
    return section;
  }

}
