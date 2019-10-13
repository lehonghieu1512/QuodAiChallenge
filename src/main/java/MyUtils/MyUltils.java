package MyUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;

public class MyUltils {
    public static Date iso8601_parse(String iso8601_string) throws ParseException {
        Date date = Date.from(Instant.parse(iso8601_string));
        return date;
    }

    public static ArrayList<JSONObject> json_read(String filepath) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException, ClassCastException {
        ArrayList<JSONObject> json = new ArrayList<JSONObject>();
        JSONObject obj;
        String line = null;
        FileReader fileReader = new FileReader(filepath);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        while ((line = bufferedReader.readLine()) != null) {
            obj = (JSONObject) new JSONParser().parse(line);
            json.add(obj);
            JSONObject ob = (JSONObject) obj.get("actor");
            String a = (String) ob.get("login");
        }
        // Always close files.
        bufferedReader.close();

        return json;
    }

    public static Object dict_get(JSONObject dict, String dict_path) {
        /*
            Used to get value from multiple-leveled json object
            params:
                dict: the Json object for data extraction
                dict_path: the path that used to extract data from multiple nested json objects.
                            In the form of "level1/level2/target"
            return:
                the expected object
         */

        String[] keys = dict_path.split("/");

        Object result = new Object();
        JSONObject ob = new JSONObject();
        ob = (JSONObject) dict.get(keys[0]);
        for (int i = 1; i < keys.length - 1; i++) {
            ob = (JSONObject) ob.get(keys[i]);
        }
        result = (Object) ob.get(keys[keys.length - 1]);
        if (result == null) {
            Object object = new Object();
            object = "null";
            return object;
        }
        return result;
    }

    public static Map<String, ArrayList<String>> get_org_reponame(ArrayList<JSONObject> events) {
        /*
            get org and repo name with respect to repo id
            params:
                event: a list of json objects contaning data from retrieved json file
            return:
                a map (dictionary) with key being repo id and value bering a list with 2 elements of reponame and org
         */
        Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        for (JSONObject event : events) {
            String repo_id = MyUltils.dict_get(event, "repo/id").toString();
            if (!result.containsKey(repo_id)) {
                // if the result has got that repo_id already
                // we will proceed to checking if the org presents in it
                // If not we will add repo_id as well as its repo_name and org
                ArrayList<String> org_reponame = new ArrayList<String>();
                String repo_name = MyUltils.dict_get(event, "repo/name").toString();
                org_reponame.add(repo_name);
                if (event.containsKey("org")) {
                    String org_name = MyUltils.dict_get(event, "org/login").toString();
                    org_reponame.add(org_name);
                } else {
                    org_reponame.add("");
                }
                result.put(repo_id, org_reponame);
            } else {
                // If so we just check whether the org presents in it
                // If not we will check the currently examined elements
                // whether it has any org for that repo
                ArrayList<String> org_reponame = result.get(repo_id);
                if (org_reponame.get(1).equals("") && event.containsKey("org")) {
                    String org_name = MyUltils.dict_get(event, "org/login").toString();
                    org_reponame.set(1, org_name);
                }
            }
        }
        return result;
    }
}
