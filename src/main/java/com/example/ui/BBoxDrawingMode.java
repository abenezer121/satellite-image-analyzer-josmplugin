package com.example.ui;

import com.example.model.BoundingBox;
import com.example.service.CoordinateFetcher;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BBoxDrawingMode implements MapViewPaintable {
    private Point startPoint;
    private Point currentPoint;
    private boolean isDrawing = false;
    private MouseAdapter mouseAdapter;
    private MapView mapView;

    public BBoxDrawingMode(MapView mv) {
        this.mapView = mv;
        this.mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    startPoint = e.getPoint();
                    currentPoint = startPoint;
                    isDrawing = true;
                    mapView.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    currentPoint = e.getPoint();
                    mapView.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawing && e.getButton() == MouseEvent.BUTTON1) {
                    isDrawing = false;
                    
                    // Convert screen coordinates to LatLon
                    LatLon startLatLon = mapView.getLatLon(startPoint.x, startPoint.y);
                    LatLon endLatLon = mapView.getLatLon(currentPoint.x, currentPoint.y);
                    
                    // Create bounding box
                    BoundingBox bbox = new BoundingBox(
                        Math.min(startLatLon.lat(), endLatLon.lat()),
                        Math.max(startLatLon.lat(), endLatLon.lat()),
                        Math.min(startLatLon.lon(), endLatLon.lon()),
                        Math.max(startLatLon.lon(), endLatLon.lon())
                    );

                
                    deactivate();
                    
                   
                    com.example.service.CoordinateFetcher.fetchCoordinatesAndDraw(bbox);
                }
            }
        };
    }

    public void activate() {
        mapView.addMouseListener(mouseAdapter);
        mapView.addMouseMotionListener(mouseAdapter);
        mapView.addTemporaryLayer(this);
        mapView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void deactivate() {
        mapView.removeMouseListener(mouseAdapter);
        mapView.removeMouseMotionListener(mouseAdapter);
        mapView.removeTemporaryLayer(this);
        mapView.setCursor(null);
        startPoint = null;
        currentPoint = null;
        isDrawing = false;
        mapView.repaint();
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        if (isDrawing && startPoint != null && currentPoint != null) {
            g.setColor(new Color(0, 0, 255, 50));
            int x = Math.min(startPoint.x, currentPoint.x);
            int y = Math.min(startPoint.y, currentPoint.y);
            int width = Math.abs(currentPoint.x - startPoint.x);
            int height = Math.abs(currentPoint.y - startPoint.y);
            g.fillRect(x, y, width, height);
            
            g.setColor(Color.BLUE);
            g.drawRect(x, y, width, height);
        }
    }
}
