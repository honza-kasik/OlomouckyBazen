package cz.honzakasik.bazenolomouc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

import cz.honzakasik.bazenolomouc.olomoucdataprovider.OlomoucUniversalTableParser;
import cz.honzakasik.bazenolomouc.olomoucdataprovider.RowIndexOutOfTableBoundsException;

public class OlomoucUniversalTableParserUnitTest {

    private static final String OCCUPANCY_SCRIPT_OUTPUT =
            "<table width=\"180\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "<tbody><tr>\n" +
            "  <td colspan=\"3\" style=\"padding:2px 0 2px 4px;background-color:#00326f;color:white;font-weight:bold;\">Krytý bazén, veřejné plavání</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td style=\"padding:2px 0 2px 4px;\">aktuálně</td>\n" +
            "  <td align=\"right\"><b>83</b></td>\n" +
            "  <td>&nbsp;plavců</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td style=\"padding:2px 0 2px 4px;\">celkem za den</td>\n" +
            "  <td align=\"right\"><b>129</b></td>\n" +
            "  <td>&nbsp;plavců</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td>&nbsp;</td>\n" +
            "  <td>&nbsp;</td>\n" +
            "  <td>&nbsp;</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td colspan=\"3\" style=\"padding:2px 0 2px 4px;background-color:#00326f;color:white;font-weight:bold;\">Letní areál</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td style=\"padding:2px 0 2px 4px;\">aktuálně</td>\n" +
            "  <td align=\"right\"><b>0</b></td>\n" +
            "  <td>&nbsp;osob</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td style=\"padding:2px 0 2px 4px;\">celkem za den</td>\n" +
            "  <td align=\"right\"><b>0</b></td>\n" +
            "  <td>&nbsp;osob</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td>&nbsp;</td>\n" +
            "  <td>&nbsp;</td>\n" +
            "  <td>&nbsp;</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td colspan=\"3\" style=\"padding:2px 0 2px 4px;background-color:#00326f;color:white;font-weight:bold;\">Sauna - volná místa</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "  <td style=\"padding:2px 0 2px 4px;\">Volných</td>\n" +
            "  <td align=\"right\">2</td>\n" +
            "  <td>&nbsp;míst</td>\n" +
            "</tr>\n" +
            "</tbody></table>";

    @Test
    public void testCurrentOccupancyByRowIndex() throws RowIndexOutOfTableBoundsException {
        Document document = Jsoup.parse(OCCUPANCY_SCRIPT_OUTPUT);

        Assert.assertEquals(Integer.valueOf(83), new OlomoucUniversalTableParser(document, 1).parse());
    }

    @Test
    public void testDailyOccupancyByRowIndex() throws RowIndexOutOfTableBoundsException {
        Document document = Jsoup.parse(OCCUPANCY_SCRIPT_OUTPUT);

        Assert.assertEquals(Integer.valueOf(129), new OlomoucUniversalTableParser(document, 2).parse());
    }

    @Test
    public void testSaunaOccupancyByRowIndex() throws RowIndexOutOfTableBoundsException {
        Document document = Jsoup.parse(OCCUPANCY_SCRIPT_OUTPUT);

        Assert.assertEquals(Integer.valueOf(2), new OlomoucUniversalTableParser(document, 9).parse());
    }
}
