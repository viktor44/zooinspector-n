/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.inspector;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.zookeeper.inspector.gui.IconResource;
import org.apache.zookeeper.inspector.gui.ZooInspectorPanel;
import org.apache.zookeeper.inspector.manager.ZooInspectorManagerImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZooInspector {
	
	public static final String APP_NAME = "ZooInspector-N";
	
	public static IconResource iconResource;
	
    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            final JFrame frame = new JFrame(APP_NAME);
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            iconResource = new IconResource();
            final ZooInspectorPanel zooInspectorPanel = new ZooInspectorPanel(new ZooInspectorManagerImpl(), iconResource);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    zooInspectorPanel.disconnect(true);
                }
            });
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
    		frame.setIconImages(
    				List.of(
    			    		toolkit.createImage(ZooInspector.class.getResource("/icons/app_16.png")),
    			    		toolkit.createImage(ZooInspector.class.getResource("/icons/app_24.png")),
    			    		toolkit.createImage(ZooInspector.class.getResource("/icons/app_32.png")),
    			    		toolkit.createImage(ZooInspector.class.getResource("/icons/app_64.png")),
    			    		toolkit.createImage(ZooInspector.class.getResource("/icons/app_128.png"))
    				)
    		);
            frame.setLocationByPlatform(true);
            frame.setContentPane(zooInspectorPanel);
            frame.setSize(800, 600);
            frame.setVisible(true);
            
            JOptionPane.setRootFrame(frame);
        } 
        catch (Exception e) {
            log.error("Error occurred loading " + APP_NAME, e);
            JOptionPane.showMessageDialog(
            		null,
            		APP_NAME + " failed to start: " + e.getMessage(), 
            		"Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
