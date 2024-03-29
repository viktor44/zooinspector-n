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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.zookeeper.inspector.manager.Pair;
import org.apache.zookeeper.inspector.manager.ZookeeperProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * The connection properties dialog. This is used to determine the settings for
 * connecting to a zookeeper instance
 */
@Slf4j
public class ZooInspectorConnectionPropertiesDialog extends JDialog {

	private JTextField connectStringText;
	private JTextField sessionTimeoutText;
	private JTextField encriptionManagerText;
	private JTextField authSchemeText;
	private JTextField authDataText;
	private JCheckBox sslCheck;
	private JTextField truststoreLocationText;
	private JPasswordField truststorePasswordText;
	private JTextField keystoreLocationText;
	private JPasswordField keystorePasswordText;

	/**
	 * @param lastConnectionProps
	 *            - the last connection properties used. if this is the first
	 *            conneciton since starting the applications this will be the
	 *            default settings
	 * @param connectionPropertiesTemplateAndLabels
	 *            - the connection properties and labels to show in this dialog
	 * @param zooInspectorPanel
	 *            - the {@link ZooInspectorPanel} linked to this dialog
	 */
	public ZooInspectorConnectionPropertiesDialog(ZookeeperProperties lastConnectionProps, ZookeeperProperties defaultConnectionProps, final ZooInspectorPanel zooInspectorPanel) {

		setLayout(new BorderLayout());
		setTitle("Connection Settings");
		setModal(true);
		setResizable(true);
		setPreferredSize(new Dimension(600, 400));

		connectStringText = new JTextField();
		sessionTimeoutText = new JTextField();
		encriptionManagerText = new JTextField();
		authSchemeText = new JTextField();
		authDataText = new JTextField();
		sslCheck = new JCheckBox("SSL");
		truststoreLocationText = new JTextField();
		truststorePasswordText = new JPasswordField();
		keystoreLocationText = new JTextField();
		keystorePasswordText = new JPasswordField();

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tabbedPane.addTab("Properties", createOptionsPanel());
		tabbedPane.addTab("Security", createSecurityPanel());
		tabbedPane.addTab("Advanced", createAdvancedPanel());

		loadConnectionProps(lastConnectionProps != null ? lastConnectionProps : defaultConnectionProps);

		JPanel buttonsPanel = createButtonsPanel(zooInspectorPanel);
		
		add(tabbedPane, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(getParent());
	}
	
	private JPanel createOptionsPanel() {
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridBagLayout());

		int row = 0;
		panel.add(new JLabel("Connect String"), createGridBagConstraints(0, row, 0, 0));
		panel.add(connectStringText, createGridBagConstraints(1, row, 1, 0));

		row++;
		panel.add(new JLabel("Session Timeout"), createGridBagConstraints(0, row, 0, 0));
		panel.add(sessionTimeoutText, createGridBagConstraints(1, row, 1, 0));

		row++;
		GridBagConstraints gbc1 = createGridBagConstraints(0, row, 1, 1);
		gbc1.gridwidth = GridBagConstraints.REMAINDER;
		gbc1.gridheight = GridBagConstraints.REMAINDER;
		panel.add(Box.createVerticalGlue(), gbc1);
		
		return panel;
	}
	
	private void doSslCheckClick(boolean selected) {
		truststoreLocationText.setEnabled(selected);
		truststorePasswordText.setEnabled(selected);
		keystoreLocationText.setEnabled(selected);
		keystorePasswordText.setEnabled(selected);
	}
	
	private JPanel createSecurityPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridBagLayout());

		int row = 0;
		panel.add(new JLabel("Authentication Scheme"), createGridBagConstraints(0, row, 0, 0));
		panel.add(authSchemeText, createGridBagConstraints(1, row, 1, 0));

		row++;
		panel.add(new JLabel("Authentication Data"), createGridBagConstraints(0, row, 0, 0));
		panel.add(authDataText, createGridBagConstraints(1, row, 1, 0));

		row++;
		GridBagConstraints gbc1 = createGridBagConstraints(0, row, 0, 0);
		gbc1.gridwidth = GridBagConstraints.REMAINDER;
		sslCheck.addActionListener((event) -> {
			doSslCheckClick(((JCheckBox)event.getSource()).isSelected());
		});
		panel.add(sslCheck, gbc1);
		
		row++;
		panel.add(new JLabel("Truststore Location"), createGridBagConstraints(0, row, 0, 0));
		panel.add(truststoreLocationText, createGridBagConstraints(1, row, 1, 0));

		row++;
		panel.add(new JLabel("Truststore Password"), createGridBagConstraints(0, row, 0, 0));
		panel.add(truststorePasswordText, createGridBagConstraints(1, row, 1, 0));

		row++;
		panel.add(new JLabel("Keystore Location"), createGridBagConstraints(0, row, 0, 0));
		panel.add(keystoreLocationText, createGridBagConstraints(1, row, 1, 0));

		row++;
		panel.add(new JLabel("Keystore Password"), createGridBagConstraints(0, row, 0, 0));
		panel.add(keystorePasswordText, createGridBagConstraints(1, row, 1, 0));
		
		row++;
		GridBagConstraints gbc0 = createGridBagConstraints(0, row, 1, 1);
		gbc0.gridwidth = GridBagConstraints.REMAINDER;
		gbc0.gridheight = GridBagConstraints.REMAINDER;
		panel.add(Box.createVerticalGlue(), gbc0);

		return panel;	
	}
	
	JPanel createAdvancedPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridBagLayout());
		
		int row = 0;
		panel.add(new JLabel("Data Encription Manager"), createGridBagConstraints(0, row, 0, 0));
		panel.add(encriptionManagerText, createGridBagConstraints(1, row, 1, 0));

		row++;
		GridBagConstraints gbc1 = createGridBagConstraints(0, row, 1, 1);
		gbc1.gridwidth = GridBagConstraints.REMAINDER;
		gbc1.gridheight = GridBagConstraints.REMAINDER;
		panel.add(Box.createVerticalGlue(), gbc1);

		return panel;
	}
	
	private JPanel createButtonsPanel(final ZooInspectorPanel zooInspectorPanel) {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonsPanel.setLayout(new GridBagLayout());
		JButton loadPropsFileButton = new JButton("Load from file");
		loadPropsFileButton.addActionListener((event) -> {
			final JFileChooser fileChooser = new JFileChooser();
			int result = fileChooser.showOpenDialog(ZooInspectorConnectionPropertiesDialog.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File propsFilePath = fileChooser.getSelectedFile();
				ZookeeperProperties props = new ZookeeperProperties();
				try {
					FileReader reader = new FileReader(propsFilePath);
					try {
						props.load(reader);
						loadConnectionProps(props);
					}
					finally {
						reader.close();
					}
				}
				catch (Exception ex) {
					log.error("An Error occurred loading connection properties from file", ex);
					JOptionPane.showMessageDialog(ZooInspectorConnectionPropertiesDialog.this, "An Error occurred loading connection properties from file", "Error", JOptionPane.ERROR_MESSAGE);
				}
//				optionsPanel.revalidate();
//				optionsPanel.repaint();
			}
		});
		GridBagConstraints c3 = createGridBagConstraints(0, 0, 0, 1);
		c3.anchor = GridBagConstraints.SOUTHWEST;
		c3.fill = GridBagConstraints.NONE;
		buttonsPanel.add(loadPropsFileButton, c3);
		JButton saveDefaultPropsFileButton = new JButton("Set As Default");
		saveDefaultPropsFileButton.addActionListener((event) -> {
			try {
				ZookeeperProperties props = getConnectionProps();
				if (props != null) {
					zooInspectorPanel.setDefaultConnectionProps(props);
				}
			}
			catch (Exception ex) {
				log.error("An Error occurred saving the default connection properties file", ex);
				JOptionPane.showMessageDialog(ZooInspectorConnectionPropertiesDialog.this, "An Error occurred saving the default connection properties file", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		GridBagConstraints c6 = createGridBagConstraints(1, 0, 1, 1);
		c6.anchor = GridBagConstraints.SOUTHWEST;
		c6.fill = GridBagConstraints.NONE;
		buttonsPanel.add(saveDefaultPropsFileButton, c6);
		JButton okButton = new JButton("OK");
		okButton.addActionListener((event) -> {
			ZookeeperProperties props = getConnectionProps();
			if (props != null) {
				ZooInspectorConnectionPropertiesDialog.this.dispose();
				zooInspectorPanel.connect(props);
			}
		});
		GridBagConstraints c4 = createGridBagConstraints(2, 0, 0, 1);
		c4.anchor = GridBagConstraints.SOUTH;
		buttonsPanel.add(okButton, c4);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((event) -> {
			ZooInspectorConnectionPropertiesDialog.this.dispose();
		});
		GridBagConstraints c5 = createGridBagConstraints(3, 0, 0, 1);
		c5.anchor = GridBagConstraints.SOUTH;
		buttonsPanel.add(cancelButton, c5);
		return buttonsPanel;
	}

	private static GridBagConstraints createGridBagConstraints(int gridx, int gridy, double weightx, double weighty) {
		return new GridBagConstraints(
							gridx, gridy, 
							1, 1, 
							weightx, weighty, 
							GridBagConstraints.WEST, 
							GridBagConstraints.HORIZONTAL, 
							new Insets(5, 5, 5, 5), 
							0, 0
					);
	}

	private void loadConnectionProps(ZookeeperProperties props) {
		if (props == null) {
			return;
		}
		connectStringText.setText(props.getConnectionString());
		sessionTimeoutText.setText(String.valueOf(props.getSessionTimeoutMs()));
		encriptionManagerText.setText(props.getEncryptionManager());
		authSchemeText.setText(props.getAuthScheme());
		authDataText.setText(props.getAuthData());
		sslCheck.setSelected(props.isClientSecure());
		truststoreLocationText.setText(props.getTruststoreLocation());
		truststorePasswordText.setText(props.getTruststorePassword());
		keystoreLocationText.setText(props.getKeystoreLocation());
		keystorePasswordText.setText(props.getKeystorePassword());
		doSslCheckClick(sslCheck.isSelected());
	}

	private ZookeeperProperties getConnectionProps() {
		ZookeeperProperties result = new ZookeeperProperties();
		
		result.setConnectionString(connectStringText.getText());
		try {
			result.setSessionTimeoutMs(Integer.valueOf(sessionTimeoutText.getText()));
		}
		catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(ZooInspectorConnectionPropertiesDialog.this, "Session Timeout is not a number", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		result.setEncryptionManager(encriptionManagerText.getText());
		result.setAuthScheme(authSchemeText.getText());
		result.setAuthData(authDataText.getText());
		result.setClientSecure(sslCheck.isSelected());
		result.setTruststoreLocation(truststoreLocationText.getText());
		result.setTruststorePassword(new String(truststorePasswordText.getPassword()));
		result.setKeystoreLocation(keystoreLocationText.getText());
		result.setKeystorePassword(new String(keystorePasswordText.getPassword()));
		
		return result;
	}
}
