package crawl;

import fourfourtwo.Helper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gsm on 8/31/15.
 */

public class Test {
    public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException, ParseException {
        String prepend = "json/";
        JSONParser jsonParser = new JSONParser();

        for(int i=0;i<380;i++) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(prepend + i));
            //System.out.println(jsonObject.toJSONString());
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Helper.getLocale((Long)jsonObject.get("LeagueID")));
            Date game_date = df.parse(((String)jsonObject.get("Date")));
            System.out.println(game_date);
        }
    }
}
