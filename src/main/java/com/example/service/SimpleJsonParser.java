package com.example.service;

import java.util.*;

public class SimpleJsonParser {

    public static List<List<double[]>> parse(String json) {
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            return parseArray(json);
        } else {
            throw new IllegalArgumentException("Invalid JSON format: Expected an array.");
        }
    }

  
    private static List<List<double[]>> parseArray(String json) {
        List<List<double[]>> lines = new ArrayList<>();
        int index = 1; // Skip opening '['
        while (index < json.length()) {
            char c = json.charAt(index);
            if (c == ']') {
                break; 
            }
            List<double[]> coordinates = parseLine(json.substring(index));
            lines.add(coordinates);
            
            
            while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != ']') {
                index++;
            }
            if (json.charAt(index) == ',') {
                index++; 
            }
        }
        return lines;
    }

   
    private static List<double[]> parseLine(String json) {
        List<double[]> coords = new ArrayList<>();
        int index = 1; // Skip opening '['
        while (index < json.length()) {
            char c = json.charAt(index);
            if (c == ']') {
                break; 
            }
            double[] coord = parseCoordinate(json.substring(index));
            coords.add(coord);
            
           
            while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != ']') {
                index++;
            }
            if (json.charAt(index) == ',') {
                index++; 
            }
        }
        return coords;
    }

 
    private static double[] parseCoordinate(String json) {
        json = json.trim();
        json = json.substring(1, json.length() - 1);
        String[] values = json.split(",");
        if (values.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format: " + json);
        }
        try {
            double lat = Double.parseDouble(values[0].trim());
            double lon = Double.parseDouble(values[1].trim());
            return new double[]{lat, lon};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + json);
        }
    }

    public static void main(String[] args) {
        String json = "[[[8.9036617, 39.0944761], [8.96155, 39.11236], [8.89830, 39.02979]]," +
                " [[8.95664, 38.91478], [8.92345, 39.05678], [8.88123, 39.08912]]]";
        
        List<List<double[]>> lines = parse(json);
        System.out.println("Parsed lines of coordinates:");
        for (List<double[]> line : lines) {
            for (double[] coord : line) {
                System.out.println(Arrays.toString(coord));
            }
        }
    }
}
