package crawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gsm on 10/13/15.
 */
public class Test {
    public static void main(String[] args)  {

        try {
            long startTime = System.currentTimeMillis();
            HttpURLConnection httpURLConnection = (HttpURLConnection)(new URL("http://www.fourfourtwo.com/statszone/8-2015/matches/803233/team-stats/11/1_PASS_01").openConnection());
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line = br.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            //System.out.println(stringBuilder.toString().length());
            Document document = Jsoup.parse(stringBuilder.toString());
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken = " + (endTime - startTime) + "ms.");
        } catch (IOException e) {
            Test.main(null);
        }

    }
}
