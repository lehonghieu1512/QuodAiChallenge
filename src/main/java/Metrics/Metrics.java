package Metrics;
import MyUtils.MyUltils;
import org.json.simple.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;
import java.util.Collections;


public class Metrics {
    public static Map<String, ArrayList<Double>> avg_num_commit(ArrayList<JSONObject> events) throws ParseException {
        /*
            This function calculates the average number of commits per day for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw average number of commits for the project
         */
        Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();
            if (eventype.equals("PushEvent")) {
                Date date = MyUltils.iso8601_parse(event.get("created_at").toString());
                String strdate = dateFormat.format(date);

                String repo_id = MyUltils.dict_get(event, "repo/id").toString();
                if (map.containsKey(repo_id)) {

                    Map<String, Integer> commit_per_day_histogram = map.get(repo_id);
                    if (commit_per_day_histogram.containsKey(strdate)) {
                        Integer num_commits = commit_per_day_histogram.get(strdate);
                        commit_per_day_histogram.replace(strdate, num_commits + 1);
                    } else {
                        commit_per_day_histogram.put(strdate, 1);
                    }
                } else {
                    Map<String, Integer> initial_value = new HashMap<String, Integer>();
                    initial_value.put(strdate, 1);
                    map.put(repo_id, initial_value);
                }
            }
        }

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (Map.Entry<String, Map<String, Integer>> ele : map.entrySet()) {
            ArrayList<Double> value = new ArrayList<Double>();
            Integer max_num_commit = Collections.max(ele.getValue().values()); // find max num of commits for that project
            Double raw_metric = ele.getValue().values().stream().mapToDouble(i -> i.doubleValue()).sum() / ele.getValue().size();
            Double heal_metric = raw_metric / max_num_commit;
            value.add(heal_metric);
            value.add(raw_metric);
            result.put(ele.getKey(), value);
        }
        return result;
    }

    public static Map<String, ArrayList<Double>> avg_time_issue_open(ArrayList<JSONObject> events) throws ParseException {
        /*
            This function calculates the average time an issue open for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw average time an issue open for each project
         */
        Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();

            if (eventype.equals("IssuesEvent")) {

                String action = MyUltils.dict_get(event, "payload/action").toString();

                if (!action.equals("closed")) {
                    continue;
                }

                Date create_date = MyUltils.iso8601_parse(MyUltils.dict_get(event, "payload/issue/created_at").toString());
                Date close_date = MyUltils.iso8601_parse(MyUltils.dict_get(event, "payload/issue/closed_at").toString());

                String repo_id = MyUltils.dict_get(event, "repo/id").toString();
                if (map.containsKey(repo_id)) {

                    ArrayList<Integer> durations = map.get(repo_id);
                    long diffmili = close_date.getTime() - create_date.getTime();
                    long diff = TimeUnit.SECONDS.convert(diffmili, TimeUnit.MILLISECONDS);
                    Integer duration = (int) diff;
                    durations.add(duration);
                } else {
                    ArrayList<Integer> durations = new ArrayList<Integer>();
                    long diffmili = close_date.getTime() - create_date.getTime();
                    long diff = TimeUnit.SECONDS.convert(diffmili, TimeUnit.MILLISECONDS);
                    Integer duration = (int) diff;
                    durations.add(duration);
                    map.put(repo_id, durations);
                }

            }
        }

        Map<String, ArrayList<Double>> metric = new HashMap<String, ArrayList<Double>>();
        for (Map.Entry<String, ArrayList<Integer>> ele : map.entrySet()) {
            Integer max_time_issue_open = Collections.max(ele.getValue());
            ArrayList<Double> value = new ArrayList<Double>();
            Double raw_metric = ele.getValue().stream().mapToDouble(i -> i.doubleValue()).sum() / ele.getValue().size();
            Double heal_metric = raw_metric / max_time_issue_open;
            value.add(heal_metric);
            value.add(raw_metric);
            metric.put(ele.getKey(), value);
        }
        return metric;
    }

    public static Map<String, ArrayList<Double>> avg_time_pr_merged(ArrayList<JSONObject> events) throws ParseException {
        /*
            This function calculates the average time until an pr get merged for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw average time until an pr get merged for each project
         */
        Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();

            if (eventype.equals("PullRequestEvent")) {

                String action = MyUltils.dict_get(event, "payload/action").toString();

                if (!action.equals("closed")) {
                    continue;
                }

                Date create_date = MyUltils.iso8601_parse(MyUltils.dict_get(event, "payload/pull_request/created_at").toString());
                String merge_status = MyUltils.dict_get(event, "payload/pull_request/merged_at").toString();
                if (merge_status == "null") {
                    continue;
                }
                Date merge_date = MyUltils.iso8601_parse(merge_status);
                String repo_id = MyUltils.dict_get(event, "repo/id").toString();
                if (map.containsKey(repo_id)) {

                    ArrayList<Integer> durations = map.get(repo_id);
                    long diffmili = merge_date.getTime() - create_date.getTime();
                    long diff = TimeUnit.SECONDS.convert(diffmili, TimeUnit.MILLISECONDS);

                    Integer duration = (int) diff;
                    durations.add(duration);
                } else {
                    ArrayList<Integer> durations = new ArrayList<Integer>();
                    long diffmili = merge_date.getTime() - create_date.getTime();
                    long diff = TimeUnit.SECONDS.convert(diffmili, TimeUnit.MILLISECONDS);
                    Integer duration = (int) diff;
                    durations.add(duration);
                    map.put(repo_id, durations);
                }
            }
        }

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (Map.Entry<String, ArrayList<Integer>> ele : map.entrySet()) {
//            if (ele.getValue().isEmpty())
//            {
//                System.out.println("eeee");
//                result.put(ele.getKey(), 0.d);
//                continue;
//            }
            Integer max_time_pr_merge = Collections.max(ele.getValue());
            ArrayList<Double> value = new ArrayList<Double>();
            Double raw_metric = ele.getValue().stream().mapToDouble(i -> i.doubleValue()).sum() / ele.getValue().size();
            Double heal_metric = raw_metric / max_time_pr_merge;
            value.add(heal_metric);
            value.add(raw_metric);
            result.put(ele.getKey(), value);
        }
        return result;
    }

    public static Map<String, ArrayList<Double>> ratio_commit_per_dev(ArrayList<JSONObject> events) throws ParseException {
        /*
            This function calculates the ratio commit per dev for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw ratio commit per dev for each project
         */
        Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();
            if (eventype.equals("PullRequestEvent")) {

                String action = MyUltils.dict_get(event, "payload/action").toString();

                if (!action.equals("closed")) {
                    continue;
                }
                String user_id = MyUltils.dict_get(event, "actor/id").toString();
                Integer num_commit = Integer.valueOf(MyUltils.dict_get(event, "payload/pull_request/commits").toString());
                String repo_id = MyUltils.dict_get(event, "repo/id").toString();
                if (map.containsKey(repo_id)) {

                    Map<String, Integer> commit_per_day_histogram = map.get(repo_id);
                    if (commit_per_day_histogram.containsKey(user_id)) {
                        Integer num_commits = commit_per_day_histogram.get(user_id);
                        commit_per_day_histogram.replace(user_id, num_commits + num_commit);
                    } else {
                        commit_per_day_histogram.put(user_id, num_commit);
                    }
                } else {
                    Map<String, Integer> initial_value = new HashMap<String, Integer>();
                    initial_value.put(user_id, num_commit);
                    map.put(repo_id, initial_value);
                }
            }
        }

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (Map.Entry<String, Map<String, Integer>> ele : map.entrySet()) {

            ArrayList<Double> value = new ArrayList<Double>();
            Integer max_num_commit = Collections.max(ele.getValue().values());
            Double raw_metric = ele.getValue().values().stream().mapToDouble(i -> i.doubleValue()).sum() / ele.getValue().size();
            Double heal_metric = raw_metric / max_num_commit;
            value.add(heal_metric);
            value.add(raw_metric);
            result.put(ele.getKey(), value);
        }
        return result;
    }

    public static Map<String, ArrayList<Double>> num_commit_per_project(ArrayList<JSONObject> events) {
        /*
            This function calculates the number of commits for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw number of commits for each project
         */
        Map<String, Integer> map = new HashMap<String, Integer>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();
            if (eventype.equals("PullRequestEvent")) {

                String action = MyUltils.dict_get(event, "payload/action").toString();

                if (!action.equals("closed")) {
                    continue;
                }
                Integer num_commit = Integer.valueOf(MyUltils.dict_get(event, "payload/pull_request/commits").toString());
                String repo_id = MyUltils.dict_get(event, "repo/id").toString();
                if (map.containsKey(repo_id)) {
                    Integer current_num_commit = map.get(repo_id);
                    map.replace(repo_id, current_num_commit + num_commit);
                } else {
                    map.put(repo_id, num_commit);
                }
            }
        }

        Integer max_num_commit = Collections.max(map.values()); // find max num of commits
        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        for (Map.Entry<String, Integer> ele : map.entrySet()) {
            ArrayList<Double> value = new ArrayList<Double>();
            Double raw_metric = ele.getValue().doubleValue();
            Double heal_metric = raw_metric / max_num_commit;
            value.add(heal_metric);
            value.add(raw_metric);
            result.put(ele.getKey(), value);
        }
        return result;
    }

    public static Map<String, ArrayList<Double>> num_contributor_per_project(ArrayList<JSONObject> events) {
        /*
            This function calculates the number of contributor for each project
            params:
                events: a list of json objects containing needed information retrieved from github
            return:
                a map with key being repo id and value being a list consisting of 2 elements (the first is health metric
                    and the second is raw number of contributor for each project
         */
        Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

        for (JSONObject event : events) {
            String eventype = event.get("type").toString();
//            if (eventype.equals("PushEvent")) {
            String action = MyUltils.dict_get(event, "payload/action").toString();
            String contributor_id = MyUltils.dict_get(event, "actor/id").toString();
            String repo_id = MyUltils.dict_get(event, "repo/id").toString();
            if (map.containsKey(repo_id)) {
                ArrayList<String> contributors = map.get(repo_id);
                if (!contributors.contains(contributor_id)) {
                    contributors.add((contributor_id));
                }
            } else {
                ArrayList<String> contributors = new ArrayList<String>();
                contributors.add(contributor_id);
                map.put(repo_id, contributors);
            }
        }

        Map<String, ArrayList<Double>> result = new HashMap<String, ArrayList<Double>>();
        Integer max_num_contributor = 0;
        for (Map.Entry<String, ArrayList<String>> ele : map.entrySet()) {
            Integer num_contributor = ele.getValue().size();
            if (num_contributor > max_num_contributor) {
                max_num_contributor = num_contributor;
            }
        }

        for (Map.Entry<String, ArrayList<String>> ele : map.entrySet()) {
            ArrayList<Double> value = new ArrayList<Double>();
            Double raw_metric = Double.valueOf(ele.getValue().size());
            Double heal_metric = raw_metric / max_num_contributor;
            value.add(heal_metric);
            value.add(raw_metric);
            result.put(ele.getKey(), value);
        }
        return result;
    }
}
