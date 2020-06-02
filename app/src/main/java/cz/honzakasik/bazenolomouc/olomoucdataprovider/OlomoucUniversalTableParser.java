package cz.honzakasik.bazenolomouc.olomoucdataprovider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for parsing tables with not unique css selector containing this structure:<br />
 * <pre>
 * {@code
 * <tr>
 *   <td>key</td>
 *   <td>value</td>
 * </tr>}
 * </pre>
 */
public class OlomoucUniversalTableParser {

    private static final Logger logger = LoggerFactory.getLogger(OlomoucUniversalTableParser.class);

    private static final String
            HTML_TR = "tr",
            HTML_TD = "td",
            HTML_B = "b";

    private final Document document;
    private String key;
    private int rowIndex;

    public OlomoucUniversalTableParser(Document document, String key) {
        this.document = document;
        this.key = key;
    }

    public OlomoucUniversalTableParser(Document document, int rowIndex) {
        this.document = document;
        this.rowIndex = rowIndex;
    }

    public Integer parse() throws RowIndexOutOfTableBoundsException {
        if (key == null) {
            return findValueUsingRowIndex();
        } else {
            return findValueUsingKey();
        }
    }

    private Integer findValueUsingKey() {
        Elements elementsContainingKey = document.getElementsContainingText(key);
        if (elementsContainingKey.size() == 0) {
            logger.error("No element containing key '{}' was found");
            return null;
        } else {
            if (elementsContainingKey.size() > 1) {
                logger.warn("More than one element containing key '{}' was found", key);
            } else {
                logger.debug("Exactly one element was found containing '{}'", key);
            }
            Element valueElement = elementsContainingKey.first().nextElementSibling();
            if (valueElement.getElementsByTag(HTML_B).size() > 0) {
                valueElement = valueElement.getElementsByTag(HTML_B).first();
            }
            return Integer.valueOf(valueElement.text());
        }
    }

    /**
     * Get value from a row using an index of row in table. Value should be present in sibling cell.
     * @return an actual value
     * @throws RowIndexOutOfTableBoundsException when row index is out of table bounds
     */
    private Integer findValueUsingRowIndex() throws RowIndexOutOfTableBoundsException {
        Elements rowElements = document.getElementsByTag(HTML_TR);
        if (rowIndex >= rowElements.size()) {
            throw new RowIndexOutOfTableBoundsException("Index " + rowIndex + " is out of table " +
                    "since table has only " + rowElements.size() + " rows.");
        }
        Element theChosenOne = rowElements.get(rowIndex);
        Elements cells = theChosenOne.getElementsByTag(HTML_TD);
        Element valueElement = cells.get(1);
        if (valueElement.getElementsByTag(HTML_B).size() > 0) {
            valueElement = valueElement.getElementsByTag(HTML_B).first();
        }
        return Integer.valueOf(valueElement.text());
    }
}
