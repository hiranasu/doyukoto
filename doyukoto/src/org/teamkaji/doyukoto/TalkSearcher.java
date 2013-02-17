package org.teamkaji.doyukoto;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class TalkSearcher {

    public List<Talk> getTalks(long lastId) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL("http://210.129.194.74/getTalk.php?lastid=" + lastId + "&limit=2");
            
            URLConnection connection = url.openConnection();
            
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
//      String result = "[{\"id\":1,\"account\":\"tteduka\",\"text\":\"どゆこと？\",\"url\":\"http://img.wired.jp/blog/compiler/200803/20080328121925-1.jpg\"}," +
//        		"{\"id\":2,\"account\":\"hiranasu\",\"text\":\"ゆりこー？\",\"url\":\"http://www.ivystar.jp/uploads/2010/11/101125-mysql.jpg\"}," +
//        		"{\"id\":3,\"account\":\"ttamari\",\"text\":\"かれこれ？\",\"url\":\"http://storage.kanshin.com/free/img_42/420102/k1075952391.jpg\"}]";
        String result = builder.toString();
        
        ObjectMapper mapper = new ObjectMapper();
        List<Talk> talkDatas = null;
		try {
			talkDatas = mapper.readValue(result, new TypeReference<ArrayList<Talk>>() {});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (talkDatas == null || talkDatas.isEmpty()) {
            return null;
        }
        return talkDatas;
    }
}
