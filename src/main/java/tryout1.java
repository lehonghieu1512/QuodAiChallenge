import MyUtils.MyUltils;
import java.text.ParseException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import java.lang.String;
import java.util.*;
import java.util.stream.Collectors;
import Metrics.Metrics;

public class tryout1 {

    public static ArrayList<JSONObject> date_range_filter(String start_time, String end_time, ArrayList<JSONObject> events) throws ParseException, IOException, org.json.simple.parser.ParseException {
        Date start_date = MyUltils.iso8601_parse(start_time);
        Date end_date = MyUltils.iso8601_parse(end_time);

        ArrayList<JSONObject> result = new ArrayList<JSONObject>();

        for (JSONObject event : events) {
            Date create_date = MyUltils.iso8601_parse(event.get("created_at").toString());
            if (create_date.before(end_date) && create_date.after(start_date)) {
                result.add(event);
            }
        }
        return result;
    }

    public static void main(String args[]) throws ParseException, IOException, org.json.simple.parser.ParseException {
        ArrayList<JSONObject> events = MyUltils.json_read(args[2]);
        ArrayList<JSONObject> events_in_range = tryout1.date_range_filter(args[0], args[1], events);
        Map<String, ArrayList<String>> org_reponames = MyUltils.get_org_reponame(events);
        ArrayList<Map<String, ArrayList<Double>>> list_of_metrics = new ArrayList<Map<String, ArrayList<Double>>>();

        Map<String, ArrayList<Double>> avg_time_issue_open_events = Metrics.avg_time_issue_open(events);
        Map<String, ArrayList<Double>> num_contributor_per_project_events = Metrics.num_contributor_per_project(events);
        Map<String, ArrayList<Double>> avg_num_commit_events = Metrics.avg_num_commit(events);
        Map<String, ArrayList<Double>> avg_time_pr_merged_events = Metrics.avg_time_pr_merged(events);
        Map<String, ArrayList<Double>> ratio_commit_per_dev_events = Metrics.ratio_commit_per_dev(events);
        Map<String, ArrayList<Double>> num_commit_per_project_events = Metrics.num_commit_per_project(events);

        // Create a list of metrics
        // And the order of output attribute also obeys
        // the order of us adding elements below
        list_of_metrics.add(num_commit_per_project_events);
        list_of_metrics.add(num_contributor_per_project_events);
        list_of_metrics.add(avg_num_commit_events);
        list_of_metrics.add(avg_time_issue_open_events);
        list_of_metrics.add(avg_time_pr_merged_events);
        list_of_metrics.add(ratio_commit_per_dev_events);

        List<Map<String, ArrayList<Double>>> iterator = new ArrayList<Map<String, ArrayList<Double>>>();
        for (Map<String, ArrayList<Double>> metric: list_of_metrics)
        {
            iterator.add(metric);
        }

        Set<String> repo_ids = new LinkedHashSet<>();

        for (Map<String, ArrayList<Double>> ele : iterator) {
            repo_ids.addAll(ele.keySet());
        }

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (String repo_id : repo_ids) {
            ArrayList<Double> health_data = new ArrayList<Double>();
            ArrayList<Double> raw_data = new ArrayList<Double>();
            for (Map<String, ArrayList<Double>> ele : iterator) {

                if (ele.containsKey(repo_id)) {
                    ArrayList <Double> heal_raw =  ele.get(repo_id);// Health and raw metric
                    health_data.add(heal_raw.get(0));
                    raw_data.add(heal_raw.get(1));
                } else {
                    health_data.add(0.d);
                    raw_data.add(0.d);
                }
            }
            Double health = health_data.stream().mapToDouble(i -> i.doubleValue()).sum();
            raw_data.add(0, health);
            result.put(repo_id, raw_data);
        }

        List<String> sorted_by_health_repo_ids = repo_ids.stream().sorted((t1, t2) -> {
            return result.get(t2).get(0).compareTo(result.get(t1).get(0));
        }).collect(Collectors.toList());

        // write to csv file
        FileWriter csv_writer = new FileWriter(args[3]);
        // write attribute labels
        csv_writer.append("name," +
                "org,num_commit," +
                "health,"+
                "num_contributor," +
                "avg_num_commit_per_day," +
                "avg_time_issue_open(seconds)," +
                "avg_time_til_pr_merged(seconds)," +
                "ratio_commit_per_dev\n");
        for (String repo_id : sorted_by_health_repo_ids) {
            if (!org_reponames.containsKey(repo_id)) {
                continue;
            }
            ArrayList<String> org_reponame = org_reponames.get(repo_id);
            csv_writer.append(org_reponame.get(0));
            csv_writer.append(",");

            // get org
            String org_name = org_reponame.get(1);
            if (org_name.equals("")) {
                csv_writer.append("null");
            } else {
                csv_writer.append(org_name);
            }
            csv_writer.append(",");

            // Write stats for each repo
            ArrayList<Double> stats = result.get(repo_id);
            for (Double ele : stats) {
                csv_writer.append(ele.toString() + ",");
            }
            csv_writer.append("\n");
        }
        csv_writer.flush();
        csv_writer.close();

    }
}
