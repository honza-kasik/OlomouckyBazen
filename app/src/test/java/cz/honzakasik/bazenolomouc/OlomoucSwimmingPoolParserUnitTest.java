package cz.honzakasik.bazenolomouc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;
import cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider.OlomoucSwimmingPoolParser;

public class OlomoucSwimmingPoolParserUnitTest {

    private static final int POOL_TRACK_COUNT = 8;

    private static final boolean[] HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC = new boolean[]{false, false, true, false, true, true, true, true};

    private static final String HORIZONTAL_SWIMMING_POOL_HTML =
            "<table id=\"bazen\" width=\"542\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
            "    <tbody>\n" +
            "        <tr>\n" +
            "            <td colspan=\"3\"><img src=\"/mdl_bazen/images/bazen1.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td><img src=\"/mdl_bazen/images/bazen2.gif\" alt=\"\" title=\"\"></td>\n" +
            "            <td width=\"494\" height=\"198\"><img src=\"/mdl_bazen/images/d1x.gif\" alt=\"D8 - výuka plav\" title=\"D8 - výuka plav\"><img src=\"/mdl_bazen/images/d2x.gif\" alt=\"D7 - potápění\" title=\"D7 - potápění\"><img src=\"/mdl_bazen/images/d3.gif\" alt=\"\" title=\"\"><img src=\"/mdl_bazen/images/d4x.gif\"\n" +
            "                    alt=\"D5 - potápění\" title=\"D5 - potápění\"><img src=\"/mdl_bazen/images/d5.gif\" alt=\"\" title=\"\"><img src=\"/mdl_bazen/images/d6.gif\" alt=\"\" title=\"\"><img src=\"/mdl_bazen/images/d7.gif\" alt=\"\" title=\"\"><img src=\"/mdl_bazen/images/d8.gif\" alt=\"\"\n" +
            "                    title=\"\"></td>\n" +
            "            <td><img src=\"/mdl_bazen/images/bazen3.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td colspan=\"3\"><img src=\"/mdl_bazen/images/bazen4.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "    </tbody>\n" +
            "</table>";

    private static final boolean[] VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC = new boolean[]{true, false, false, false, true, true, true, true};

    private static final String VERTICAL_SWIMMING_POOL_HTML =
            "<table id=\"bazen\" width=\"542\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
            "    <tbody>\n" +
            "        <tr>\n" +
            "            <td colspan=\"3\"><img src=\"/mdl_bazen/images/bazen1.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td><img src=\"/mdl_bazen/images/bazen2.gif\" alt=\"\" title=\"\"></td>\n" +
            "            <td width=\"494\" height=\"198\">\n" +
            "                <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
            "                    <tbody>\n" +
            "                        <tr>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k1.gif\" alt=\"\" title=\"\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k2x.gif\" alt=\"K7 - výuka plav\" title=\"K7 - výuka plav\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k3x.gif\" alt=\"K6 - plavání\" title=\"K6 - plavání\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k4x.gif\" alt=\"K5 - plavání\" title=\"K5 - plavání\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k5.gif\" alt=\"\" title=\"\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k6.gif\" alt=\"\" title=\"\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k7.gif\" alt=\"\" title=\"\"></td>\n" +
            "                            <td><img src=\"/mdl_bazen/images/k8.gif\" alt=\"\" title=\"\"></td>\n" +
            "                        </tr>\n" +
            "                    </tbody>\n" +
            "                </table>\n" +
            "            </td>\n" +
            "            <td><img src=\"/mdl_bazen/images/bazen3.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td colspan=\"3\"><img src=\"/mdl_bazen/images/bazen4.gif\" alt=\"\" title=\"\"></td>\n" +
            "        </tr>\n" +
            "    </tbody>\n" +
            "</table>";

    @Test
    public void testParsingHorizontalPoolOrientation() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(SwimmingPool.TrackOrientation.HORIZONTAL, pool.getOrientation());
    }

    @Test
    public void testParsingHorizontalPoolTrack0() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[0], pool.getTracks().get(0).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack1() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[1], pool.getTracks().get(1).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack2() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[2], pool.getTracks().get(2).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack3() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[3], pool.getTracks().get(3).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack4() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[4], pool.getTracks().get(4).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack5() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[5], pool.getTracks().get(5).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack6() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[6], pool.getTracks().get(6).isForPublic());
    }

    @Test
    public void testParsingHorizontalPoolTrack7() {
        Document document = Jsoup.parse(HORIZONTAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(HORIZONTAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[7], pool.getTracks().get(7).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolOrientation() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(SwimmingPool.TrackOrientation.VERTICAL, pool.getOrientation());
    }

    @Test
    public void testParsingVerticalPoolTrackCount() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(POOL_TRACK_COUNT, pool.getTracks().size());
    }

    @Test
    public void testParsingVerticalPoolTrack0() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[0], pool.getTracks().get(0).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack1() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[1], pool.getTracks().get(1).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack2() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[2], pool.getTracks().get(2).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack3() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[3], pool.getTracks().get(3).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack4() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[4], pool.getTracks().get(4).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack5() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[5], pool.getTracks().get(5).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack6() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[6], pool.getTracks().get(6).isForPublic());
    }

    @Test
    public void testParsingVerticalPoolTrack7() {
        Document document = Jsoup.parse(VERTICAL_SWIMMING_POOL_HTML);
        SwimmingPool pool = new OlomoucSwimmingPoolParser(document).parseSwimmingPool();

        Assert.assertEquals(VERTICAL_POOL_TRACKS_AVAILABLE_FOR_PUBLIC[7], pool.getTracks().get(7).isForPublic());
    }

}
