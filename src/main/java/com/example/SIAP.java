package com.example;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Logging;

import com.example.ui.BBoxDrawingMode;


public class SIAP extends Plugin {

    private JMenuItem myShiftGMenuItem;
    private BBoxDrawingMode bboxDrawingMode;
    private MapFrame currentMapFrame;

    public SIAP(PluginInformation info) {
        super(info);
        Logging.info("MySimplePlugin loaded.");
    }

    private void startBBoxDrawing() {
        if (currentMapFrame != null && currentMapFrame.mapView != null) {
            if (bboxDrawingMode != null) {
                bboxDrawingMode.deactivate();
            }
            bboxDrawingMode = new BBoxDrawingMode(currentMapFrame.mapView);
            bboxDrawingMode.activate();
        }
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        currentMapFrame = newFrame;

        if (newFrame != null && oldFrame == null) {
            if (myShiftGMenuItem == null) {
                myShiftGMenuItem = new JMenuItem("Draw Bounding Box for Highway");
                KeyStroke shiftGKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK);
                myShiftGMenuItem.setAccelerator(shiftGKeyStroke);

                myShiftGMenuItem.addActionListener(e -> {
                    Logging.info("Shift+G clicked!");
                    startBBoxDrawing();
                });

                MainApplication.getMenu().toolsMenu.add(myShiftGMenuItem);
                Logging.info("MySimplePlugin: Added Shift+G menu item.");
            }
        } else if (newFrame == null && oldFrame != null) {
            if (myShiftGMenuItem != null) {
                MainApplication.getMenu().toolsMenu.remove(myShiftGMenuItem);
                myShiftGMenuItem = null;
                Logging.info("MySimplePlugin: Removed Shift+G menu item.");
            }
            if (bboxDrawingMode != null) {
                bboxDrawingMode.deactivate();
                bboxDrawingMode = null;
            }
        }
    }
}
