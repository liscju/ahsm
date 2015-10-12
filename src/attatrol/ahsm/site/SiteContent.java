package attatrol.ahsm.site;

import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class SiteContent {

  private Map<String, Element> fileTables;
  private Element summarySection; 
  private String simpleName;
  
  public SiteContent(Map<String, Element> fileTables, Element summarySection, String simpleName) {
    this.fileTables = fileTables;
    this.summarySection = summarySection;
    this.simpleName = simpleName;
    summarySection.getElementsByTag("h2").first().remove();
    this.summarySection = new Element(Tag.valueOf("div"), "").attr("class", "section");
    this.summarySection.appendElement("h2").attr("id", "Summary").text("Summary for " + simpleName +":");
    this.summarySection.appendChild(summarySection);
  }
  
  public SiteContent(Map<String, Element> fileTables, Element summarySection) {
    this.fileTables = fileTables;
    this.summarySection = summarySection;
  }

  public Map<String, Element> getFileTables() {
    return fileTables;
  }

  public Element getSummarySection() {
    return summarySection;
  }

  public String getSimpleName() {
    return simpleName;
  }
}
