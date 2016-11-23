package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;

public class OlomoucSwimmingPoolParser {

    private static final String
            SWIMMING_POOL_ELEMENT_ID = "bazen",
            HTML_SRC_ATTR = "src",
            HTML_TABLE = "table";

    private final Element document;

    public OlomoucSwimmingPoolParser(Document document) {
        this.document = document;
    }

    public SwimmingPool parseSwimmingPool() {
        SwimmingPool.Builder builder = new SwimmingPool.Builder();

        Element poolElement = document.getElementById(SWIMMING_POOL_ELEMENT_ID);
        Element innerPoolTable = poolElement.getElementsByTag(HTML_TABLE).get(0);
        SwimmingPool.TrackOrientation orientation = findSwimmingPoolOrientationFromInnerPoolTable(innerPoolTable);

        builder.orientation(orientation);

        System.out.println(getPoolTrackElements(innerPoolTable));
        for (Element track : getPoolTrackElements(innerPoolTable)) {
            boolean isForPublic = isTrackAvailableIsForPublicByImageElement(track);
            builder.track(new SwimmingPool.Track(isForPublic));
        }

        return builder.build();
    }

    private SwimmingPool.TrackOrientation findSwimmingPoolOrientationFromInnerPoolTable(Element innerPoolTable) {
        System.out.println(getPoolTrackElements(innerPoolTable));
        System.out.println(getPoolTrackElements(innerPoolTable).attr(HTML_SRC_ATTR));
        if (getPoolTrackElements(innerPoolTable).attr(HTML_SRC_ATTR).matches(".*d\\dx?\\.gif")) {
            return SwimmingPool.TrackOrientation.HORIZONTAL;
        } else {
            return SwimmingPool.TrackOrientation.VERTICAL;
        }
    }

    private boolean isTrackAvailableIsForPublicByImageElement(Element trackImage) {
        return !trackImage.attr(HTML_SRC_ATTR).contains("x");
    }

    private Elements getPoolTrackElements(Element poolElement) {
        return poolElement.select("img[src~=[kd]\\dx?\\.gif]");
    }
}
