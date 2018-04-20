package sanzhiev.rssfeed.services.parser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import sanzhiev.rssfeed.model.FeedChannel;
import sanzhiev.rssfeed.model.FeedItem;

public abstract class Parser {
    public static Parser createParser(final InputStream input, final String channelUrl)
            throws ParserException {
        final XmlPullParser pullParser = Xml.newPullParser();

        try {
            pullParser.setInput(input, null);

            pullParser.nextTag();

            if (pullParser.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Wrong format");
            }

            final String documentType = pullParser.getName();

            if (documentType == null) {
                throw new XmlPullParserException("Wrong format");
            }

            switch (documentType) {
                case "rss":
                    pullParser.nextTag();
                    return new RssParser(pullParser, channelUrl);
                case "feed":
                    return new AtomParser(pullParser, channelUrl);
                default:
                    throw new XmlPullParserException("Wrong format");
            }
        } catch (final XmlPullParserException | IOException e) {
            throw new ParserException(e.getMessage());
        }
    }

    public abstract ArrayList<FeedItem> parseFeed(final FeedChannel channel) throws ParserException;

    public abstract FeedChannel parseChannel() throws ParserException;

    protected abstract FeedItem parseItem(final FeedChannel channel)
            throws IOException, XmlPullParserException;

    protected abstract String parseAttribute(final String attributeName)
            throws IOException, XmlPullParserException;

    String readText(final XmlPullParser pullParser) throws IOException, XmlPullParserException {
        String text = null;

        if (pullParser.next() == XmlPullParser.TEXT) {
            text = pullParser.getText();
            pullParser.nextTag();
        }

        return text;
    }

    void skip(final XmlPullParser pullParser) throws XmlPullParserException, IOException {
        if (pullParser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (pullParser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
                default:
                    //тут нужно вписать строку, которая при сборке, подписанной ключом, не зовется,
                    //чтобы не прятать ошибки от тестировщика
                    break;
            }
        }
    }
}
