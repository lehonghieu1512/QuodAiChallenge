import MyUtils.MyUltils;
import MyUtils.MyUltils.*;
import java.text.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.io.*;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import com.google.gson.Gson;
import java.lang.String;
import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.parser.JSONParser;
import Metrics.Metrics;

public class tryout1 {

//
//    public static void main(String args[]) throws FileNotFoundException, IOException, ParseException, ClassCastException
//    {
//        Map<String, Integer> map = new HashMap<String, Integer>();
//
//        ArrayList<JSONObject> commits = MyUltils.json_read("C:\\Users\\taola\\OneDrive\\Desktop\\2015-01-01-15.json");
//
//        for (JSONObject commit: commits)
//        {
//            String repo_id = Utils.dict_get(commit, "actor/login").toString();
//
//            if (!map.containsKey(repo_id))
//            {
//                map.put(repo_id, 1);
//            }
//            else
//            {
//                Integer fre = map.get(repo_id);
//                map.replace(repo_id, fre+1);
//            }
//        }
//
//        System.out.println(map.toString());
//        System.out.println(map.size());

    public static ArrayList<JSONObject> date_range_filter(String start_time, String end_time, ArrayList<JSONObject> events) throws ParseException, IOException,  org.json.simple.parser.ParseException
    {
        Date start_date = MyUltils.iso8601_parse(start_time);
        Date end_date = MyUltils.iso8601_parse(end_time);

        ArrayList<JSONObject> result = new ArrayList<JSONObject>();

        for (JSONObject event: events)
        {
            Date create_date = MyUltils.iso8601_parse(event.get("created_at").toString());
            if (create_date.before(end_date) && create_date.after(start_date))
            {
                result.add(event);
            }
        }
        return result;
    }

    public static void main(String args[]) throws ParseException, IOException,  org.json.simple.parser.ParseException
    {
        ArrayList<JSONObject> events = MyUltils.json_read("C:\\Users\\taola\\OneDrive\\Desktop\\abc.json");
        ArrayList<JSONObject> events_in_range = tryout1.date_range_filter("2015-01-01T15:00:01Z", "2017-01-01T15:00:01Z", events);
        Map<String, Double> avg_time_issue_open_events = Metrics.avg_time_issue_open(events);
        Map<String, Double> num_contributor_per_project_events = Metrics.num_contributor_per_project(events);
        Map<String, ArrayList<String>> org_reponame = MyUltils.get_org_reponame(events);

        Set<String> repo_ids = new LinkedHashSet<>();
        repo_ids.addAll(avg_time_issue_open_events.keySet());
        repo_ids.addAll(num_contributor_per_project_events.keySet());

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (String repo_id: repo_ids)
        {
            ArrayList<Double> data = new ArrayList<Double>();
            Double health;
            if (avg_time_issue_open_events.containsKey(repo_id))
            {
                data.add(avg_time_issue_open_events.get(repo_id));
            }
            else
            {
                data.add(0.d);
            }

            if (num_contributor_per_project_events.containsKey(repo_id))
            {
                data.add(num_contributor_per_project_events.get(repo_id));
            }
            else
            {
                data.add(0.d);
            }

            health = data.stream().mapToDouble(i->i.doubleValue()).sum();
            data.add(0, health);
            result.put(repo_id, data);
        }

        List<String> sorted_by_health_repo_ids = repo_ids.stream().sorted((t1, t2) -> {
            return result.get(t2).get(0).compareTo(result.get(t1).get(0));
        }).collect(Collectors.toList());

        for (String repo_id: sorted_by_health_repo_ids)
        {

        }

        System.out.println(result.toString());
        System.out.println(sorted_by_health_repo_ids.toString());
    }
}
