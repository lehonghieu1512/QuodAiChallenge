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
    public static Date iso8601_parse(String iso8601_string) throws ParseException
    {
        Date date = Date.from( Instant.parse(iso8601_string));
        return date;
    }

    public static ArrayList<JSONObject> json_read(String filepath) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException, ClassCastException
    {
        ArrayList<JSONObject> json=new ArrayList<JSONObject>();
        JSONObject obj;
        // The name of the file to open.

        // This will reference one line at a time
        String line = null;
        FileReader fileReader = new FileReader(filepath);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            obj = (JSONObject) new JSONParser().parse(line);
            json.add(obj);
            JSONObject ob = (JSONObject) obj.get("actor");
            String a = (String)ob.get("login");
        }
        // Always close files.
        bufferedReader.close();

        return json;
    }

    public static Object dict_get(JSONObject dict, String dict_path )
    {
        String[] keys = dict_path.split("/");

        Object result = new Object();
        JSONObject ob = new JSONObject();
        ob = (JSONObject) dict.get(keys[0]);
        for (int i=1; i<keys.length-1; i++) {
            ob = (JSONObject) ob.get(keys[i]);
        }
        result = (Object) ob.get(keys[keys.length-1]);
        if (result == null)
        {
            Object object = new Object();
            object = "null";
            return object;
        }
        return result;
    }

    public static Map<String, ArrayList<String>> get_org_reponame(ArrayList<JSONObject> events)
    {
        Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        for (JSONObject event: events)
        {
            String repo_id = MyUltils.dict_get(event,"repo/id").toString();
            if(event.containsKey("org") && !result.containsKey(repo_id))
            {
                ArrayList<String> org_reponame = new ArrayList<String>();
                String repo_name = MyUltils.dict_get(event,"repo/name").toString();
                String org_name = MyUltils.dict_get(event,"org/login").toString();
                org_reponame.add(org_name);
                org_reponame.add(repo_name);
                result.put(repo_id, org_reponame);
            }
        }
        System.out.println(result.toString());
        return result;
    }
}
