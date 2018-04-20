package sanzhiev.rssfeed.services.parser;

//TODO: обработка ошибок при парсинге
public class ParserException extends Exception {
    ParserException(final String message) {
        super(message);
    }
}
