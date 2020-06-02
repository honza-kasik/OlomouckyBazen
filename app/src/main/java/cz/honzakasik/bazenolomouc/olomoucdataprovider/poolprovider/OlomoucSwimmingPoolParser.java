package cz.honzakasik.bazenolomouc.olomoucdataprovider.poolprovider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;

public class OlomoucSwimmingPoolParser {

    private static final Logger logger = LoggerFactory.getLogger(OlomoucSwimmingPoolParser.class);

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
        this.document = document;
    }

    /**
     * Parses swimming pool from document
     * @return parsed swimming pool
     * @throws NoPoolParsedException if no pool was parsed!
     */
    public SwimmingPool parseSwimmingPool() throws NoPoolParsedException {
        Element poolElement = document.getElementById(SWIMMING_POOL_ELEMENT_ID);

        if (poolElement == null) {
            throw new NoPoolParsedException("Is your device set to correct time in Czech Republic?");
        }

        logger.debug(poolElement.toString());

        final SwimmingPool.Builder builder = new SwimmingPool.Builder();

        //find a pool element table
        final Elements innerTables = poolElement.children().select(HTML_TABLE);
        Element innerPoolTable;
        if (innerTables == null || innerTables.size() == 0) {
            innerPoolTable = poolElement;
        } else {
            innerPoolTable = innerTables.first();
        }

        if (findIfPoolIsOpen(innerPoolTable)) {
            builder.open();
            SwimmingPool.TrackOrientation orientation = findSwimmingPoolOrientationFromInnerPoolTable(innerPoolTable);

            builder.orientation(orientation);

            for (Element track : getPoolTrackElements(innerPoolTable)) {
                boolean isForPublic = isTrackAvailableIsForPublicByImageElement(track);
                builder.track(new SwimmingPool.Track(isForPublic));
            }
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

    /**
     * Find out if pool is open from element
     * @return true if pool is open, false otherwise
     */
    private boolean findIfPoolIsOpen(Element innerPoolTable) {
        return innerPoolTable.getElementsByTag("img").size() > 1;
    }
}
