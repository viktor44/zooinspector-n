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

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @see http://standards.freedesktop.org/icon-naming-spec/icon-naming-spec-latest.html
 * I tried to take icons that are available in the Tango icon set
 * 
 * Icon source http://tango.freedesktop.org/releases/
 */
public class IconResource {

	// 24x24
    public static final String ICON_ChangeNodeViewers = "actions/preferences-system";
    public static final String ICON_INFORMATION = "status/info";
    public static final String ICON_SAVE = "actions/document-save";
    public static final String ICON_UP = "actions/up";
    public static final String ICON_DOWN = "actions/down";
    public static final String ICON_ADD = "actions/add";
    public static final String ICON_REMOVE = "actions/remove";
    public static final String ICON_START = "actions/media-playback-start";
    public static final String ICON_STOP = "actions/media-playback-stop";
    public static final String ICON_DOCUMENT_ADD = "actions/document-new";
    public static final String ICON_REFRESH = "actions/view-refresh";
    public static final String ICON_TRASH = "places/user-trash";
    // better: actions/help-about, but not in tango
    public static final String ICON_HELP_ABOUT = "status/info";

    // 16x16
    public static final String ICON_TREE_LEAF = "mimetypes/text-x-generic";
    public static final String ICON_TREE_OPEN = "places/folder-open";
    public static final String ICON_TREE_CLOSE = "places/folder";

    private static final String DEFAULT_THEME = "Tango";
    private static final String FALLBACK_ICON = "face-surprise";

    private String theme = DEFAULT_THEME;

    public URL find(String name) {
        String iconPath = buildIconPath(name);
        URL iconUrl = getClass().getResource(iconPath);
        if (null != iconUrl) {
        	return iconUrl;
        }

        if (!name.equals(FALLBACK_ICON)) {
        	return find(FALLBACK_ICON);
        }
        return null;
    }

    public ImageIcon get(String name, String description) {
        URL iconUrl = find(name);
        if (iconUrl == null) {
            ImageIcon icon = new ImageIcon();
            icon.setDescription(description);
            return icon;
        } 
        else {
            return new ImageIcon(iconUrl, description);
        }
    }

    private String buildIconPath(String name) {
        return "/icons/" + theme + "/" + name + ".png";
    }
}
