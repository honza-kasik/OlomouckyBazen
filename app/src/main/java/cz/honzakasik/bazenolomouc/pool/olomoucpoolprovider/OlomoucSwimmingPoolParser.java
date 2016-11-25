package cz.honzakasik.bazenolomouc.pool.olomoucpoolprovider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;

public class OlomoucSwimmingPoolParser {

    private static final String
            SWIMMING_POOL_ELEMENT_ID = "bazen",
            HTML_SRC_ATTR = "src",
            HTML_TABLE = "table";

    private final Element document;

    /**
     * Creates new parser
     * @param document document from which the {@link SwimmingPool} will be parsed
     */
    public OlomoucSwimmingPoolParser(Document document) {
        /*Logger logger = LoggerFactory.getLogger(OlomoucSwimmingPoolParser.class);
        logger.debug(document.outerHtml());*/
        this.document = document;
    }

    /**
     * Parses swimming pool from document
     * @return parsed swimming pool
     */
    public SwimmingPool parseSwimmingPool() {
        SwimmingPool.Builder builder = new SwimmingPool.Builder();

        Element poolElement = document.getElementById(SWIMMING_POOL_ELEMENT_ID);
        Element innerPoolTable = poolElement.getElementsByTag(HTML_TABLE).get(0);
        SwimmingPool.TrackOrientation orientation = findSwimmingPoolOrientationFromInnerPoolTable(innerPoolTable);

        builder.orientation(orientation);

        for (Element track : getPoolTrackElements(innerPoolTable)) {
            boolean isForPublic = isTrackAvailableIsForPublicByImageElement(track);
            builder.track(new SwimmingPool.Track(isForPublic));
        }

        return builder.build();
    }

    private SwimmingPool.TrackOrientation findSwimmingPoolOrientationFromInnerPoolTable(Element innerPoolTable) {
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
