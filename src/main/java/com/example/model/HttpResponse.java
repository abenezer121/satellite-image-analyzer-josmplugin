package com.example.model;

import java.util.List;


public class HttpResponse {
    public List<List<double[]>> lines;  


    public HttpResponse(List<List<double[]>> lines) {
        this.lines = lines;
    }

    
    public List<List<double[]>> getLines() {
        return lines;
    }

    public void setLines(List<List<double[]>> lines) {
        this.lines = lines;
    }
}
