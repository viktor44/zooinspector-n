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
package org.apache.zookeeper.inspector.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.zookeeper.inspector.ZooInspector;
import org.apache.zookeeper.inspector.logger.LoggerFactory;

/**
 * The About Dialog for the application
 */
public class ZooInspectorAboutDialog extends JDialog {
    /**
     * @param frame
     *            - the Frame from which the dialog is displayed
     */
    public ZooInspectorAboutDialog(Frame frame, IconResource iconResource) {
        super(frame);
        this.setLayout(new BorderLayout());
        this.setIconImage(iconResource.get(IconResource.ICON_INFORMATION, "About " + ZooInspector.APP_NAME).getImage());
        this.setTitle("About " + ZooInspector.APP_NAME);
        this.setModal(true);
        this.setResizable(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JEditorPane aboutPane = new JEditorPane();
        aboutPane.setEditable(false);
        aboutPane.setOpaque(false);
        aboutPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        aboutPane.addHyperlinkListener((event) -> {
                if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
               	    try {
						Desktop.getDesktop().browse(event.getURL().toURI());
					}
					catch (IOException | URISyntaxException ex) {
						LoggerFactory.getLogger().error("", ex);
					}
                }
        });        
        URL aboutURL = ZooInspectorAboutDialog.class.getResource("about.html");
        try {
            aboutPane.setPage(aboutURL);
        } 
        catch (IOException e) {
            LoggerFactory.getLogger().error("Error loading about.html, file may be corrupt", e);
        }
        panel.add(aboutPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(600, 300));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                ZooInspectorAboutDialog.this.dispose();
            }
        });
        buttonsPanel.add(okButton);
        this.add(panel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(getParent());
    }
}
