package org.teamkaji.doyukoto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

public class ImageSearcher {

    public String getImageUrl(String searchKey) throws JsonParseException, IOException {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
                    "v=1.0&safe=off&rsz=1&hl=ja&lr=lang_ja&gl=jp&q=" + URLEncoder.encode(searchKey, "UTF-8"));
            
            URLConnection connection = url.openConnection();
            connection.addRequestProperty("Referer", "http://www.yahoo.co.jp/");
            
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<Map<String, String>>>> response = mapper.readValue(builder.toString(), HashMap.class);
        Map<String,  List<Map<String, String>>> responseData = response.get("responseData");
        List<Map<String, String>> results = responseData.get("results");
        
        System.out.println(results.toString());
        if (results.isEmpty()) {
            return null;
        }
        
        return results.get(0).get("url");
    }
}
