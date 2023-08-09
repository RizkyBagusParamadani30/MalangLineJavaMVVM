package com.example.malanglinejavamvvm.utilities;

import android.content.Context;
import android.graphics.Color;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.PointTransport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Service {

    public interface IServiceInterface {
        void onPointsObtained(ArrayList<Line> lines, ArrayList<Interchange> interchanges);

        void onPointsRequestError(String error);
    }


    public static void getManagedPoints(Context context, final IServiceInterface callback) {

        String rawManagedPointsJson = readRawString(context, R.raw.managedpoints);

        try {
            JSONObject response = new JSONObject(rawManagedPointsJson);
            JSONArray linesJson = response.getJSONArray("lines");

            ArrayList<Line> lines = new ArrayList<>();

            for (int i = 0; i < linesJson.length(); i++) {
                JSONObject lineJson = linesJson.getJSONObject(i);
                /* "idline": "1", "name": "AL", "direction": "O", "color": "#FF0000", "path": [] */
                Line line = new Line();
                line.id = Integer.valueOf(lineJson.getString("idline"));
                line.name = lineJson.getString("name");
                line.direction = lineJson.getString("direction");
                line.color = Color.parseColor(lineJson.getString("color"));

                JSONArray pathJson = lineJson.getJSONArray("path");

                LinkedList<PointTransport> path = new LinkedList<>();

                for (int j = 0; j < pathJson.length(); j++) {
                    JSONObject pointJson = pathJson.getJSONObject(j);
                    /*
                        String id, double lat, double lng, boolean stop, int idLine,
                        String lineName, String direction, String color, int sequence,
                        String adjacentPoints, String interchanges
                     */
                    PointTransport point = new PointTransport(
                            pointJson.getString("idpoint"),
                            Double.valueOf(pointJson.getString("lat")),
                            Double.valueOf(pointJson.getString("lng")),
                            pointJson.getString("stop").equals("1"),
                            line.id,
                            line.name,
                            line.direction,
                            "#" + Integer.toHexString(line.color),
                            Integer.valueOf(pointJson.getString("sequence")),
                            null,
                            null
                    );
                    path.add(point);
                }
                line.path = path;
                lines.add(line);

            }

            ArrayList<Interchange> interchanges = new ArrayList<>();

            JSONArray interchangesJson = response.getJSONArray("interchanges");

            for (int i = 0; i < interchangesJson.length(); i++) {

                JSONObject interchangeJson = interchangesJson.getJSONObject(i);

                Interchange interchange = new Interchange();
                interchange.idInterchange = interchangeJson.getString("idinterchange");
                interchange.name = interchangeJson.getString("name");

                Set<String> pointIds = new HashSet<>();

                JSONArray pointsJson = interchangeJson.getJSONArray("points");

                for (int j = 0; j < pointsJson.length(); j++) {
                    JSONObject pointJson = pointsJson.getJSONObject(j);
                    PointTransport point = new PointTransport();
                    /*
                    "idline": "1",
                    "idpoint": "717",
                    "sequence": "281",
                    "stop": "1",
                    "idinterchange": "4"
                     */
                    point.id = pointJson.getString("idpoint");
                    pointIds.add(point.id);
                }

                interchange.pointIds.addAll(pointIds);
                interchanges.add(interchange);

            }

            if (callback != null)
                callback.onPointsObtained(lines, interchanges);
        } catch (JSONException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onPointsRequestError(e.getMessage());
        }

    }

    private static String readRawString(Context context, int rawResourceId) {
        InputStream inputStream = context.getResources().openRawResource(rawResourceId);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }

}
