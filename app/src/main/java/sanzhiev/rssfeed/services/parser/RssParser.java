package sanzhiev.rssfeed.services.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sanzhiev.rssfeed.model.FeedChannel;
import sanzhiev.rssfeed.model.FeedItem;

class RssParser extends Parser {
    private static final String namespace = null;
    private final XmlPullParser pullParser;
    private final String channelUrl;
    private final String itemTagName = "item";

    RssParser(final XmlPullParser pullParser, final String channelUrl) {
        this.pullParser = pullParser;
        this.channelUrl = channelUrl;
    }

    @Override
    public ArrayList<FeedItem> parseFeed(final FeedChannel channel) throws ParserException {
        final ArrayList<FeedItem> feed = new ArrayList<>();
        try {
            while (pullParser.getEventType() != XmlPullParser.END_TAG) {
                if (pullParser.getEventType() != XmlPullParser.START_TAG) {
                    pullParser.next();
                    continue;
                }

                final String tagName = pullParser.getName();

                final String channelTagName = "channel";
                
                if (tagName.equals(channelTagName)) {
                    pullParser.nextTag();
                    continue;
                }

                if (tagName.equals(itemTagName)) {
                    feed.add(parseItem(channel));
                } else {
                    skip(pullParser);
                }

                pullParser.next();
            }

            return feed;
        } catch (final XmlPullParserException | IOException e) {
            throw new ParserException(e.getMessage());
        }
    }

    @Override
    public FeedChannel parseChannel() throws ParserException {
        try {
            final String channelTagName = "channel";
            pullParser.require(XmlPullParser.START_TAG, namespace, channelTagName);

            String title = null;
            String description = null;

            final int maxDepth = 3;

            while (pullParser.next() != XmlPullParser.END_TAG) {
                if (pullParser.getEventType() != XmlPullParser.START_TAG
                        || pullParser.getDepth() > maxDepth) {
                    continue;
                }

                if (title != null && description != null) {
                    return new FeedChannel(title, description, channelUrl);
                }

                final String tagName = pullParser.getName();

                switch (tagName) {
                    case "title":
                        title = parseAttribute(tagName);
                        break;
                    case "description":
                        description = parseAttribute(tagName);
                        break;
                    default:
                        skip(pullParser);
                        break;
                }
            }

            return new FeedChannel(title, description, channelUrl);
        } catch (XmlPullParserException | IOException e) {
            throw new ParserException(e.getMessage());
        }
    }

    @Override
    protected FeedItem parseItem(final FeedChannel channel)
            throws IOException, XmlPullParserException {
        pullParser.require(XmlPullParser.START_TAG, namespace, itemTagName);

        String title = null;
        String description = null;
        String link = null;
        Date pubDate = null;

        final String datePattern = "EEE, dd MMM yyyy HH:mm:ss Z";
        final SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        while (pullParser.next() != XmlPullParser.END_TAG) {
            if (pullParser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            final String tagName = pullParser.getName();

            switch (tagName) {
                case "title":
                    title = parseAttribute(tagName);
                    break;
                case "description":
                    description = parseAttribute(tagName);
                    break;
                case "link":
                    link = parseAttribute(tagName);
                    break;
                case "pubDate":
                    final String dateString = parseAttribute(tagName);
                    pubDate = dateFormat.parse(dateString, new ParsePosition(0));
                    break;
                default:
                    skip(pullParser);
                    break;
            }
        }

        pullParser.require(XmlPullParser.END_TAG, namespace, itemTagName);

        final boolean isItemRead = false;

        return new FeedItem(title, description, link, pubDate, channel, isItemRead);
    }

    @Override
    protected String parseAttribute(final String attributeName) throws IOException, XmlPullParserException {
        pullParser.require(XmlPullParser.START_TAG, namespace, attributeName);
        final String text = readText(pullParser);
        pullParser.require(XmlPullParser.END_TAG, namespace, attributeName);

        return text;
    }
}
