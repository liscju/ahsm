package attatrol.ahsm.site;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class SiteContentMerger {

  private Element firstUnique;
  private Element secondUnique;

  private int total1 = 0;
  private int total2 = 0;
  private SiteContent content1;
  private SiteContent content2;
  private String anchorName;
  private int anchorNum;

  @SuppressWarnings("unused")
  private SiteContentMerger() {

  }

  public SiteContentMerger(SiteContent content1, SiteContent content2) {
    this.content1 = content1;
    this.content2 = content2;
    firstUnique = new Element(Tag.valueOf("td"), "").text(content1.getSimpleName());
    secondUnique = new Element(Tag.valueOf("td"), "").text(content2.getSimpleName());
  }

  public SiteContent merge() {
    Map<String, Element> entries1 = content1.getFileTables();
    Map<String, Element> entries2 = content2.getFileTables();
    Set<String> keysAll = new HashSet<>();
    keysAll.addAll(entries1.keySet());
    keysAll.addAll(entries2.keySet());
    Map<String, Element> mergedMap = new HashMap<>();
    for (String name : keysAll) {
      setAnchorName(name.substring(name.length() - 9, name.length() - 5));
      final Element entry1 = entries1.get(name);
      final Element entry2 = entries2.get(name);
      if (entry1 == null) {
        mergedMap.put(name, secondFullEntry(entry2));
      } else if (entry2 == null) {
        mergedMap.put(name, firstFullEntry(entry1));
      } else {
        final Element mergedEntry = mergeEntry(entry1, entry2, name);
        if (mergedEntry != null) {
          mergedMap.put(name, mergedEntry);
        }
      }
    }

    Element mergedSummary = generateSection();
    mergedSummary.appendText("\n");
    Element summary1 = content1.getSummarySection().clone();
    summary1.appendElement("h3")
        .text("Got unique rows for \"" + content1.getSimpleName() + "\":" + total1)
        .appendText("\n");
    Element summary2 = content2.getSummarySection().clone();
    summary2.appendElement("h3")
        .text("Got unique rows for \"" + content2.getSimpleName() + "\":" + total2)
        .appendText("\n");
    mergedSummary.appendChild(summary1);
    mergedSummary.appendText("\n");
    mergedSummary.appendChild(summary2);
    mergedSummary.appendText("\n");
    SiteContent merge = new SiteContent(mergedMap, mergedSummary);
    total1 = 0;
    total2 = 0;
    return merge;
  }

  private Element firstFullEntry(Element entry) {
    Element residue = entry.clone();
    Element merge = generateSection();
    merge.appendChild(residue.children().first());
    merge.appendText("\n");
    final Element table = residue.getElementsByTag("tbody").first();
    Element mergePoint = merge.appendElement("table").attr("border", "0")
        .attr("class", "bodyTable");
    Element header = table.children().first();
    header.appendElement("th").text("Site");
    header.prependElement("th").text("Anchor");
    mergePoint.appendChild(header).appendText("\n");
    for (ListIterator<Element> iterator = table.children().listIterator(); iterator.hasNext();) {
      total1++;
      final Element current = iterator.next();
      current.attr("class", "a");
      current.appendChild(firstUnique.clone());
      current.prependChild(getAnchor());
      mergePoint.appendChild(current);
      mergePoint.appendText("\n");
    }
    return merge;
  }

  private Element secondFullEntry(Element entry) {
    Element residue = entry.clone();
    Element merge = generateSection();
    merge.appendChild(residue.children().first());
    merge.appendText("\n");
    final Element table = residue.getElementsByTag("tbody").first();
    Element mergePoint = merge.appendElement("table").attr("border", "0")
        .attr("class", "bodyTable");
    Element header = table.children().first();
    header.appendElement("th").text("Site");
    header.prependElement("th").text("Anchor");
    mergePoint.appendChild(header).appendText("\n");
    for (ListIterator<Element> iterator = table.children().listIterator(); iterator.hasNext();) {
      total2++;
      final Element current = iterator.next();
      current.attr("class", "b");
      current.appendChild(secondUnique.clone());
      current.prependChild(getAnchor());
      mergePoint.appendChild(current);
      mergePoint.appendText("\n");
    }
    return merge;
  }

  private Element mergeEntry(Element entry1, Element entry2, String key) {
    boolean noMerging = true;
    Element residue1 = entry1.clone();
    Element residue2 = entry2.clone();

    Element merge = generateSection();
    merge.appendChild(residue1.children().first());
    merge.appendText("\n");
    final Element table1 = residue1.getElementsByTag("tbody").first();
    final Element table2 = residue2.getElementsByTag("tbody").first();
    Element mergePoint = merge.appendElement("table").attr("border", "0")
        .attr("class", "bodyTable");
    Element header = table1.children().first();
    header.appendElement("th").text("Site");
    header.prependElement("th").text("Anchor");
    mergePoint.appendChild(header).appendText("\n");

    // first check
    for (ListIterator<Element> iterator1 = table1.children().listIterator(1); iterator1.hasNext();) {
      final Element current1 = iterator1.next();
      boolean unique1 = true;
      for (ListIterator<Element> iterator2 = table2.children().listIterator(); iterator2.hasNext();) {
        final Element current2 = iterator2.next();
        if (rowEqualityCheck(current1, current2)) {
          unique1 = false;
          break;
        }
      }
      if (unique1) {
        noMerging = false;
        total1++;
        final Element addition = current1.clone();
        addition.attr("class", "a");
        addition.prependChild(getAnchor());
        addition.appendChild(firstUnique.clone());

        mergePoint.appendChild(addition);
        mergePoint.appendText("\n");
      }
    }
    // second check
    for (ListIterator<Element> iterator2 = table2.children().listIterator(1); iterator2.hasNext();) {
      final Element current2 = iterator2.next();
      boolean unique2 = true;
      for (ListIterator<Element> iterator1 = table1.children().listIterator(); iterator1.hasNext();) {
        final Element current1 = iterator1.next();
        if (rowEqualityCheck(current1, current2)) {
          unique2 = false;
          break;
        }
      }
      if (unique2) {
        noMerging = false;
        total2++;
        final Element addition = current2.clone();
        addition.attr("class", "b");
        addition.prependChild(getAnchor());
        addition.appendChild(secondUnique.clone());

        mergePoint.appendChild(addition);
        mergePoint.appendText("\n");
      }
    }
    if (noMerging) {
      return null;
    } else {
      return merge;
    }
  }

  private static Element generateSection() {
    final Attributes attrs = new Attributes();
    attrs.put("class", "section");
    Element merge = new Element(Tag.valueOf("div"), "", attrs);
    merge.appendText("\n");
    return merge;
  }

  /**
   * Check html table row for identity
   * 
   * @param a
   * @param b
   * @return
   */
  private static boolean rowEqualityCheck(Element a, Element b) {
    Iterator<Element> valueA = a.getElementsByTag("td").iterator();
    Iterator<Element> valueB = b.getElementsByTag("td").iterator();
    while (valueA.hasNext() && valueB.hasNext()) {
      final String textA = valueA.next().text().trim();
      final String textB = valueB.next().text().trim();
      if (!textA.equals(textB)) {
        return false;
      }
    }
    if (valueA.hasNext() || valueB.hasNext()) {
      return false;
    }
    return true;
  }

  private void setAnchorName(String name) {
    anchorName = name;
    anchorNum = 1;
  }

  private Element getAnchor() {
    final String anchorText = String.format("%s.%d", anchorName, anchorNum);
    final String name = String.format("%s.%d", anchorText, total1 + total2);
    anchorNum++;
    Element anchor = new Element(Tag.valueOf("td"), "");
    anchor.appendElement("a").attr("name", name).attr("href", "#" + name).text(anchorText);
    return anchor;
  }

}
