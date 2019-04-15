// Vergleiche https://github.com/mfietz/JHyphenator abgerufen am 14.04.19

import java.util.List;
import de.mfietz.jhyphenator.*;

class HyphenGenerator {
  private String lang = "de";

  public HyphenGenerator() {
    this("de");
  }

  public HyphenGenerator(String lang) {
    this.lang = lang;
  }

  public List<String> hyphenate(String word) {
    HyphenationPattern de = HyphenationPattern.lookup(lang);
    Hyphenator h = Hyphenator.getInstance(de);
    List<String> hyphenated = h.hyphenate(word);
    return hyphenated;
  }
}
