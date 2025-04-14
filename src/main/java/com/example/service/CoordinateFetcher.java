package com.example.service;

import com.example.model.BoundingBox;
import com.example.model.HttpResponse;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import com.example.utils.JsonParser;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class CoordinateFetcher {

    public static void fetchCoordinatesAndDraw(BoundingBox bbox) {
        new Thread(() -> {
            try {
                String urlStr = String.format("http://localhost:3000/coordinates?minLat=%f&maxLat=%f&minLon=%f&maxLon=%f",
                        bbox.minLat, bbox.maxLat, bbox.minLon, bbox.maxLon);

                Logging.info("Fetching coordinates from: " + urlStr);

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed with HTTP status: " + conn.getResponseCode());
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseStr = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseStr.append(line);
                }
                in.close();

                String json = responseStr.toString().trim();

                List<JsonParser.Token> tokens = JsonParser.getTokens(json);
                JsonParser.Parser parser = new JsonParser.Parser(tokens);
                Object result = parser.parse();
                if (result instanceof List) {
                    List<?> lines = (List<?>) result;
                
                    for (int i = 0; i < lines.size(); i++) {
                        System.out.println("Line " + (i + 1) + ":");
                
                        Object lineObj = lines.get(i);
                        if (lineObj instanceof List) {
                            List<?> points = (List<?>) lineObj;
                            List<double[]> coordinates = new ArrayList<>();
                            for (int j = 0; j < points.size(); j++) {
                                Object pointObj = points.get(j);
                                if (pointObj instanceof List) {
                                    List<?> coords = (List<?>) pointObj;
                                    if (coords.size() == 2) {
                                        double lat = ((Number) coords.get(0)).doubleValue();
                                        double lon = ((Number) coords.get(1)).doubleValue();
                                        double []coord = {lat , lon};
                                        coordinates.add(coord);
                                    }
                                }
                            }
                            SwingUtilities.invokeLater(() -> createHighway(coordinates));
                        }
                    }
                } else {
                   throw new Error("Invalid json");
                }
                
               
               
            } catch (Exception ex) {
                Logging.error("Error fetching coordinates: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        "Failed to fetch data:\n" + ex.getMessage()));
            }
        }).start();
    }

    private static void createHighway(List<double[]> coordinates) {
        DataSet dataSet = MainApplication.getLayerManager().getEditDataSet();
        if (dataSet == null) {
            Logging.warn("No active data set found.");
            return;
        }

        List<Node> nodes = new ArrayList<>();
        List<Command> commands = new ArrayList<>();

        for (double[] coord : coordinates) {
            Node node = new Node(new LatLon(coord[0], coord[1]));
            nodes.add(node);
            commands.add(new AddCommand(dataSet, node));
        }

        Way way = new Way();
        for (Node node : nodes) {
            way.addNode(node);
        }

        way.put("highway", "motorway");
        way.put("josm_color", "pink"); 
        commands.add(new AddCommand(dataSet, way));

        UndoRedoHandler.getInstance().add(new SequenceCommand("Create Highway", commands));
    }
}
