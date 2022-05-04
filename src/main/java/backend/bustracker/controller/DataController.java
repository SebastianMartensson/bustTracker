package backend.bustracker.controller;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class DataController {

    String getBusLines() {
        LinkedHashMap<Integer, ArrayList<String>> busLines = getBusLineNumbersWithStops();
        List<Integer> keys = busLines.entrySet().stream().sorted((left, right) -> Integer.compare(right.getValue().size(), left.getValue().size())).map(entry -> entry.getKey()).collect(Collectors.toList());

        LinkedHashMap<Integer, ArrayList<String>> topTen = new LinkedHashMap<>();
        HashMap<Integer, String> busLinesStop = getBusStops();
        ArrayList<String> stopNames;
        JSONArray jsonBusArray = new JSONArray();


        for (int n = 0; n < 10; n++) {
            JSONObject jbus = new JSONObject();
            stopNames = busLines.get(keys.get(n));
            for (int j = 0; j < stopNames.size(); j++) {
                stopNames.set(j, busLinesStop.get(Integer.parseInt(stopNames.get(j))));
            }
            topTen.put(keys.get(n), stopNames);
            jbus.put("stops", stopNames);
            jbus.put("lineNumber", keys.get(n));

            jsonBusArray.put(jbus);

        }

        String jsonString = new JSONArray(jsonBusArray).toString();

        return jsonString;
    }

    @ResponseBody
    private JSONArray apiCall(String urladdress) throws org.json.JSONException {
        JSONArray jsonArray;
        JSONObject jsonResponse = null;

        try {
            StringBuilder builder = new StringBuilder();
            URL url = new URL(urladdress);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                jsonResponse = new JSONObject(builder.toString());


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject root = jsonResponse;
        JSONObject responeData = new JSONObject(root.getJSONObject("ResponseData").toString());
        jsonArray = responeData.getJSONArray("Result");
        return jsonArray;
    }

    private HashMap<Integer, String> getBusStops() {
        Dotenv dotenv = Dotenv.load();
        String apiCall = "https://api.sl.se/api2/LineData.json?model=stop&DefaultTransportModeCode=BUS&key="+dotenv.get("API_KEY");
        HashMap<Integer, String> busStops = new HashMap<>();

        JSONArray jStops = apiCall(apiCall);

        for (int i = 0; i < jStops.length(); i++) {
            JSONObject stops = jStops.getJSONObject(i);
            int stopPointNumber = stops.getInt("StopPointNumber");
            String stopName = stops.getString("StopPointName");
            busStops.put(stopPointNumber, stopName);

        }
        return busStops;
    }


    private LinkedHashMap<Integer, ArrayList<String>> getBusLineNumbersWithStops() {
        LinkedHashMap<Integer, ArrayList<String>> busLines = new LinkedHashMap<>();
        Dotenv dotenv = Dotenv.load();
        String apiCall = "https://api.sl.se/api2/LineData.json?model=jour&DefaultTransportModeCode=BUS&key="+dotenv.get("API_KEY");

        JSONArray jArray = apiCall(apiCall);

        ArrayList<String> tempList = new ArrayList<>();

        int prevLine = 1;
        for (int i = 0; i <= jArray.length(); i++) {
            JSONObject line = jArray.getJSONObject(i);
            int lineNumber = line.getInt("LineNumber");
            String stops = line.getString("JourneyPatternPointNumber");
            int direction = line.getInt("DirectionCode");

            if (direction == 2) {
                if (lineNumber == prevLine) {
                    tempList.add(stops);
                    if (i == jArray.length() - 1) {
                        busLines.put(lineNumber, tempList);
                        break;
                    }
                } else {
                    busLines.put(prevLine, tempList);
                    prevLine = lineNumber;
                    tempList = new ArrayList<>();
                    tempList.add(stops);
                }
            }
        }
        return busLines;
    }
}






