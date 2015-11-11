package crawl;

import org.jsoup.nodes.Document;

import java.io.Serializable;

/**
 * Created by gsm on 10/31/15.
 */
public class MyDocument implements Serializable {
    public Document document;

    public MyDocument(Document document) {
        this.document = document;
    }
}
