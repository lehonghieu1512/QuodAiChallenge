import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.time.Instant;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.lang.String;


public class Utils {
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
//        String fileName = "C:\\Users\\taola\\OneDrive\\Desktop\\2015-01-01-15.json";

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
        for (int i=0; i<keys.length-1; i++) {
            ob = (JSONObject) dict.get(keys[i]);
        }
        result = (Object) ob.get(keys[keys.length-1]);
        return result;
    }

//    public static void main(String args[]) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException, ClassCastException
//    {
//        ArrayList<JSONObject> json = Utils.json_read("C:\\\\Users\\\\taola\\\\OneDrive\\\\Desktop\\\\2015-01-01-15.json");
//        System.out.println(json.toArray()[0].getClass());
//
//        for (int i=0; i< 10; i++)
//        {
//            Object a = Utils.dict_get(json.get(i), "actor/login");
//            System.out.println(a.toString());
//        }
//
////        Object a = Utils.dict_get(json.get(0), "actor/login");
//
//    }
}
