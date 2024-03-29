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
package org.apache.zookeeper.inspector.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.inspector.ZooInspector;
import org.apache.zookeeper.inspector.encryption.BasicDataEncryptionManager;
import org.apache.zookeeper.inspector.encryption.DataEncryptionManager;
import org.apache.zookeeper.retry.ZooKeeperRetry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A default implementation of {@link ZooInspectorManager} for connecting to
 * zookeeper instances
 */
@Slf4j
public class ZooInspectorManagerImpl implements ZooInspectorManager {
    private static final String A_VERSION = "ACL Version";
    private static final String C_TIME = "Creation Time";
    private static final String C_VERSION = "Children Version";
    private static final String CZXID = "Creation ID";
    private static final String DATA_LENGTH = "Data Length";
    private static final String EPHEMERAL_OWNER = "Ephemeral Owner";
    private static final String M_TIME = "Last Modified Time";
    private static final String MZXID = "Modified ID";
    private static final String NUM_CHILDREN = "Number of Children";
    private static final String PZXID = "Node ID";
    private static final String VERSION = "Data Version";
    private static final String ACL_PERMS = "Permissions";
    private static final String ACL_SCHEME = "Scheme";
    private static final String ACL_ID = "Id";
    private static final String SESSION_STATE = "Session State";
    private static final String SESSION_ID = "Session ID";

    private static final String DEFAULT_ENCRYPTION_MANAGER = BasicDataEncryptionManager.class.getName();
    private static final int DEFAULT_TIMEOUT = 10_000;
    private static final String DEFAULT_HOSTS = "localhost:2181";
    private static final String DEFAULT_AUTH_SCHEME = "";
    private static final String DEFAULT_AUTH_VALUE = "";

    private static final String homeDir = System.getProperty("user.home");
    private static final File defaultNodeViewersFile = new File(homeDir + "/.zooinspector/defaultNodeViewers.cfg");
    private static final File defaultConnectionFile = new File(homeDir + "/.zooinspector/defaultConnectionSettings.cfg");

    private DataEncryptionManager encryptionManager;
    private String connectString;
    private int sessionTimeout;
    private ZooKeeper zooKeeper;
    private final Map<String, NodeWatcher> watchers = new HashMap<String, NodeWatcher>();
    protected boolean connected = true;
    private ZookeeperProperties lastConnectionProps;
	private String defaultEncryptionManager;
    private ZookeeperProperties defaultConnectionProps;
//    private int defaultTimeout;
//    private String defaultHosts;
//    private String defaultAuthScheme;
//    private String defaultAuthValue;
    private NodesCache nodesCache;

    /**
     * @throws IOException
     *             - thrown if the default connection settings cannot be loaded
     * 
     */
    public ZooInspectorManagerImpl() throws IOException {
        defaultConnectionProps = loadDefaultConnectionFile();
    }

    @Override
	public boolean connect(ZookeeperProperties connectionProps) {
        try {
            if (this.zooKeeper == null) {
                if (connectionProps.getConnectionString() == null || connectionProps.getSessionTimeoutMs() <= 0) {
                    throw new IllegalArgumentException(
                            "Both connect string and session timeout are required.");
                }
                if (connectionProps.getEncryptionManager() == null) {
                    this.encryptionManager = new BasicDataEncryptionManager();
                } 
                else {
                    Class<?> clazz = Class.forName(connectionProps.getEncryptionManager());

                    if (Arrays.asList(clazz.getInterfaces()).contains(DataEncryptionManager.class)) {
                        this.encryptionManager = (DataEncryptionManager) clazz.newInstance();
                    } 
                    else {
                        throw new IllegalArgumentException("Data encryption manager must implement DataEncryptionManager interface");
                    }
                }
                this.connectString = connectionProps.getConnectionString();
                this.sessionTimeout = connectionProps.getSessionTimeoutMs();
                this.zooKeeper = new ZooKeeperRetry(
                		connectionProps, 
                		new Watcher() {
			                    @Override
			                    public void process(WatchedEvent event) {
			                        if (event.getState() == KeeperState.Expired) {
			                            connected = false;
			                        }
			                    }
		                }
                );
                connected = ((ZooKeeperRetry) this.zooKeeper).testConnection();
            }
        } 
        catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
        if (!connected){
        	disconnect();
        } 
        else {
            this.nodesCache = new NodesCache(zooKeeper);
        }
        return connected;
    }

    @Override
    public boolean disconnect() {
        try {
            if (this.zooKeeper != null) {
                this.zooKeeper.close();
                this.zooKeeper = null;
                connected = false;
                removeWatchers(this.watchers.keySet());
                return true;
            }
        } 
        catch (Exception e) {
            log.error("Error occurred while disconnecting from ZooKeeper server", e);
        }
        return false;
    }

    @Override
    public List<String> getChildren(String nodePath) {
        if (connected) {
            return nodesCache.getChildren(nodePath);
        }
        return null;

    }

    @Override
    public String getData(String nodePath) {
        if (connected) {
            try {
                if (nodePath.length() == 0) {
                    nodePath = "/";
                }
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    return this.encryptionManager.decryptData(
                    				zooKeeper.getData(nodePath, false, s)
                    		);
                }
            } 
            catch (Exception e) {
                log.error("Error occurred getting data for node: " + nodePath, e);
            }
        }
        return null;
    }

    @Override
    public String getNodeChild(String nodePath, int childIndex) {
        if (connected) {
             return this.nodesCache.getNodeChild(nodePath, childIndex);
        }
        return null;
    }

    @Override
    public int getNodeIndex(String nodePath) {
        if (connected) {
            int index = nodePath.lastIndexOf("/");
            if (index == -1
                    || (!nodePath.equals("/") && nodePath.charAt(nodePath.length() - 1) == '/')) {
                throw new IllegalArgumentException("Invalid node path: " + nodePath);
            }
            String parentPath = nodePath.substring(0, index);
            String child = nodePath.substring(index + 1);
            if (parentPath != null && parentPath.length() > 0) {
                List<String> children = this.nodesCache.getChildren(parentPath);
                if (children != null) {
                    return children.indexOf(child);
                }
            }
        }
        return -1;
    }

    @Override
    public List<Map<String, String>> getACLs(String nodePath) {
        List<Map<String, String>> returnACLs = new ArrayList<Map<String, String>>();
        if (connected) {
            try {
                if (nodePath.length() == 0) {
                    nodePath = "/";
                }
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    List<ACL> acls = zooKeeper.getACL(nodePath, s);
                    for (ACL acl : acls) {
                        Map<String, String> aclMap = new LinkedHashMap<String, String>();
                        aclMap.put(ACL_SCHEME, acl.getId().getScheme());
                        aclMap.put(ACL_ID, acl.getId().getId());
                        StringBuilder sb = new StringBuilder();
                        int perms = acl.getPerms();
                        boolean addedPerm = false;
                        if ((perms & Perms.READ) == Perms.READ) {
                            sb.append("Read");
                            addedPerm = true;
                        }
                        if (addedPerm) {
                            sb.append(", ");
                        }
                        if ((perms & Perms.WRITE) == Perms.WRITE) {
                            sb.append("Write");
                            addedPerm = true;
                        }
                        if (addedPerm) {
                            sb.append(", ");
                        }
                        if ((perms & Perms.CREATE) == Perms.CREATE) {
                            sb.append("Create");
                            addedPerm = true;
                        }
                        if (addedPerm) {
                            sb.append(", ");
                        }
                        if ((perms & Perms.DELETE) == Perms.DELETE) {
                            sb.append("Delete");
                            addedPerm = true;
                        }
                        if (addedPerm) {
                            sb.append(", ");
                        }
                        if ((perms & Perms.ADMIN) == Perms.ADMIN) {
                            sb.append("Admin");
                            addedPerm = true;
                        }
                        aclMap.put(ACL_PERMS, sb.toString());
                        returnACLs.add(aclMap);
                    }
                }
            } 
            catch (InterruptedException e) {
                log.error("Error occurred retrieving ACLs of node: {}", e);
            } 
            catch (KeeperException e) {
                log.error("Error occurred retrieving ACLs of node: {}", nodePath, e);
            }
        }
        return returnACLs;
    }

    @Override
    public Map<String, String> getNodeMeta(String nodePath) {
        Map<String, String> nodeMeta = new LinkedHashMap<String, String>();
        if (connected) {
            try {
                if (nodePath.length() == 0) {
                    nodePath = "/";
                }
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");

                    nodeMeta.put(A_VERSION, String.valueOf(s.getAversion()));
                    nodeMeta.put(C_TIME, format.format(new Date(s.getCtime())));
                    nodeMeta.put(C_VERSION, String.valueOf(s.getCversion()));
                    nodeMeta.put(CZXID, String.valueOf(s.getCzxid()));
                    nodeMeta.put(DATA_LENGTH, String.valueOf(s.getDataLength()));
                    nodeMeta.put(EPHEMERAL_OWNER, String.valueOf(s.getEphemeralOwner()));
                    nodeMeta.put(M_TIME, format.format(new Date(s.getMtime())));
                    nodeMeta.put(MZXID, String.valueOf(s.getMzxid()));
                    nodeMeta.put(NUM_CHILDREN, String.valueOf(s.getNumChildren()));
                    nodeMeta.put(PZXID, String.valueOf(s.getPzxid()));
                    nodeMeta.put(VERSION, String.valueOf(s.getVersion()));
                }
            } 
            catch (Exception e) {
                log.error("Error occurred retrieving meta data for node: {}", nodePath, e);
            }
        }
        return nodeMeta;
    }

    @Override
    public int getNumChildren(String nodePath) {
        if (connected) {
            try {
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    return s.getNumChildren();
                }
            } 
            catch (Exception e) {
                log.error("Error occurred getting the number of children of node: {}", nodePath, e);
            }
        }
        return -1;
    }

    @Override
    public boolean hasChildren(String nodePath) {
        return getNumChildren(nodePath) > 0;
    }

    @Override
    public boolean isAllowsChildren(String nodePath) {
        if (connected) {
            try {
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    return s.getEphemeralOwner() == 0;
                }
            } 
            catch (Exception e) {
                log.error("Error occurred determining whether node is allowed children: {}", nodePath, e);
            }
        }
        return false;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @seeorg.apache.zookeeper.inspector.manager.ZooInspectorReadOnlyManager#
//     * getSessionMeta()
//     */
//    @Override
//    public Map<String, String> getSessionMeta() {
//        Map<String, String> sessionMeta = new LinkedHashMap<String, String>();
//        try {
//            if (zooKeeper != null) {
//
//                sessionMeta.put(SESSION_ID, String.valueOf(zooKeeper.getSessionId()));
//                sessionMeta.put(SESSION_STATE, String.valueOf(zooKeeper.getState().toString()));
//                sessionMeta.put(CONNECT_STRING, this.connectString);
//                sessionMeta.put(SESSION_TIMEOUT, String.valueOf(this.sessionTimeout));
//            }
//        } 
//        catch (Exception e) {
//            LoggerFactory.getLogger().error(
//                    "Error occurred retrieving session meta data.", e);
//        }
//        return sessionMeta;
//    }

    @Override
    public boolean createNode(String parent, String nodeName) {
        if (connected) {
            try {
                String[] nodeElements = nodeName.split("/");
                for (String nodeElement : nodeElements) {
                    String node = parent + "/" + nodeElement;
                    Stat s = zooKeeper.exists(node, false);
                    if (s == null) {
                        zooKeeper.create(node, this.encryptionManager
                                .encryptData(null), Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                        parent = node;
                    }
                }
                return true;
            } 
            catch (Exception e) {
                log.error("Error occurred creating node: {}/{}", parent, nodeName, e);
            }
        }
        return false;
    }

    @Override
    public boolean deleteNode(String nodePath) {
        if (connected) {
            try {
                Stat s = zooKeeper.exists(nodePath, false);
                if (s != null) {
                    List<String> children = zooKeeper.getChildren(nodePath,
                            false);
                    for (String child : children) {
                        String node = nodePath + "/" + child;
                        deleteNode(node);
                    }
                    zooKeeper.delete(nodePath, -1);
                }
                return true;
            } 
            catch (Exception e) {
                log.error("Error occurred deleting node: {}", nodePath, e);
            }
        }
        return false;
    }

    @Override
    public boolean setData(String nodePath, String data) {
        if (connected) {
            try {
                zooKeeper.setData(nodePath, this.encryptionManager.encryptData(data), -1);
                return true;
            } 
            catch (Exception e) {
                log.error("Error occurred setting data for node: {}", nodePath, e);
            }
        }
        return false;
    }

    @Override
    public ZookeeperProperties getDefaultConnectionProperties() {
    	return defaultConnectionProps;
    	
//        Map<String, List<String>> template = new LinkedHashMap<String, List<String>>();
//        template.put(CONNECT_STRING, Arrays.asList( defaultHosts ));
//        template.put(SESSION_TIMEOUT, Arrays.asList( defaultTimeout ));
//        template.put(DATA_ENCRYPTION_MANAGER, Arrays.asList( defaultEncryptionManager ));
//        template.put(AUTH_SCHEME_KEY, Arrays.asList( defaultAuthScheme ));
//        template.put(AUTH_DATA_KEY, Arrays.asList( defaultAuthValue ));
//        Map<String, String> labels = new LinkedHashMap<String, String>();
//        labels.put(CONNECT_STRING, "Connect String");
//        labels.put(SESSION_TIMEOUT, "Session Timeout");
//        labels.put(DATA_ENCRYPTION_MANAGER, "Data Encryption Manager");
//        labels.put(AUTH_SCHEME_KEY, "Authentication Scheme");
//        labels.put(AUTH_DATA_KEY, "Authentication Data");
//        return new Pair<Map<String, List<String>>, Map<String, String>>(template, labels);
    }

    @Override
    public void addWatchers(Collection<String> selectedNodes, NodeListener nodeListener) {
        // add watcher for each node and add node to collection of
        // watched nodes
        if (connected) {
            for (String node : selectedNodes) {
                if (!watchers.containsKey(node)) {
                    try {
                        watchers.put(node, new NodeWatcher(node, nodeListener, zooKeeper));
                    } 
                    catch (Exception e) {
                        log.error("Error occurred adding node watcher for node: {}", node, e);
                    }
                }
            }
        }
    }

    @Override
    public void removeWatchers(Collection<String> selectedNodes) {
        // remove watcher for each node and remove node from
        // collection of watched nodes
        if (connected) {
            for (String node : selectedNodes) {
                if (watchers.containsKey(node)) {
                    NodeWatcher watcher = watchers.remove(node);
                    if (watcher != null) {
                        watcher.stop();
                    }
                }
            }
        }
    }

    /**
     * A Watcher which will re-add itself every time an event is fired
     */
    public class NodeWatcher implements Watcher {

        private final String nodePath;
        private final NodeListener nodeListener;
        private final ZooKeeper zookeeper;
        private boolean closed = false;

        /**
         * @param nodePath
         *            - the path to the node to watch
         * @param nodeListener
         *            the {@link NodeListener} for this node
         * @param zookeeper
         *            - a {@link ZooKeeper} to use to access zookeeper
         * @throws InterruptedException
         * @throws KeeperException
         */
        public NodeWatcher(String nodePath, NodeListener nodeListener,
                ZooKeeper zookeeper) throws KeeperException,
                InterruptedException {
            this.nodePath = nodePath;
            this.nodeListener = nodeListener;
            this.zookeeper = zookeeper;
            Stat s = zooKeeper.exists(nodePath, this);
            if (s != null) {
                zookeeper.getChildren(nodePath, this);
            }
        }

        @Override
        public void process(WatchedEvent event) {
            if (!closed) {
                try {
                    if (event.getType() != EventType.NodeDeleted) {

                        Stat s = zooKeeper.exists(nodePath, this);
                        if (s != null) {
                            zookeeper.getChildren(nodePath, this);
                        }
                    }
                } 
                catch (Exception e) {
                    log.error("Error occurred re-adding node watcherfor node {}", nodePath, e);
                }
                nodeListener.processEvent(event.getPath(), event.getType().name(), null);
            }
        }

        public void stop() {
            this.closed = true;
        }

    }

    @Override
    public List<String> loadNodeViewersFile(File selectedFile) throws IOException {
        List<String> result = new ArrayList<String>();

        try (BufferedReader reader = getReaderForFile(selectedFile)) {
            if(reader == null) {
                return result;
            }

            String line = "";
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        result.add(line);
                    }
                }
            }
        }

        return result;
    }

    private ZookeeperProperties loadDefaultConnectionFile() throws IOException {
    	ZookeeperProperties props = new ZookeeperProperties();

    	props.setConnectionString(DEFAULT_HOSTS);
    	props.setSessionTimeoutMs(DEFAULT_TIMEOUT);
    	props.setEncryptionManager(DEFAULT_ENCRYPTION_MANAGER);
    	props.setAuthScheme(DEFAULT_AUTH_SCHEME);
    	props.setAuthData(DEFAULT_AUTH_VALUE);
    	
        try(BufferedReader reader = getReaderForFile(defaultConnectionFile)) {
            //If reader is null, it's OK.  Default values will get set below.
            if(reader != null) {
                props.load(reader);
            }
        }
        
        return props;
    }

    @Override
    public void saveDefaultConnectionFile(ZookeeperProperties props) throws IOException {
        File defaultDir = defaultConnectionFile.getParentFile();
        if (!defaultDir.exists()) {
            if (!defaultDir.mkdirs()) {
                throw new IOException(
                        "Failed to create configuration directory: " + defaultDir.getAbsolutePath());
            }
        }
        if (!defaultConnectionFile.exists()) {
            if (!defaultConnectionFile.createNewFile()) {
                throw new IOException(
                        "Failed to create default connection file: "
                                + defaultConnectionFile.getAbsolutePath());
            }
        }
        FileWriter writer = new FileWriter(defaultConnectionFile);
        try {
            props.store(writer, "Default connection for " + ZooInspector.APP_NAME);
        } 
        finally {
            writer.close();
        }
    }

    @Override
    public void saveNodeViewersFile(File selectedFile, List<String> nodeViewersClassNames) throws IOException {
        if (!selectedFile.exists()) {
            if (!selectedFile.createNewFile()) {
                throw new IOException(
                        "Failed to create node viewers configuration file: "
                                + selectedFile.getAbsolutePath());
            }
        }
        FileWriter writer = new FileWriter(selectedFile);
        try {
            BufferedWriter buff = new BufferedWriter(writer);
            try {
                for (String nodeViewersClassName : nodeViewersClassNames) {
                    buff.append(nodeViewersClassName);
                    buff.append("\n");
                }
            } 
            finally {
                buff.flush();
                buff.close();
            }
        } 
        finally {
            writer.close();
        }
    }

    @Override
    public void setDefaultNodeViewerConfiguration(
            List<String> nodeViewersClassNames) throws IOException {
        File defaultDir = defaultNodeViewersFile.getParentFile();
        if (!defaultDir.exists()) {
            if (!defaultDir.mkdirs()) {
                throw new IOException(
                        "Failed to create configuration directory: "
                                + defaultDir.getAbsolutePath());
            }
        }
        saveNodeViewersFile(defaultNodeViewersFile, nodeViewersClassNames);
    }

    @Override
    public List<String> getDefaultNodeViewerConfiguration() throws IOException {
        List<String> defaultNodeViewers = loadNodeViewersFile(defaultNodeViewersFile);
        if (defaultNodeViewers.isEmpty()) {
            log.warn("List of default node viewers is empty");
        }
        return defaultNodeViewers;
    }

    private static BufferedReader getReaderForFile(File file) {
        //check the filesystem first
        if (file.exists()) {
            try {
                return new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        //fall back to checking the CLASSPATH with only the filename
        //(for cases where the file exists in src/main/resources)
        InputStream classpathStream = ZooInspectorManagerImpl.class.getClassLoader()
        									.getResourceAsStream(file.getName());
        if (classpathStream != null) {
            return new BufferedReader(new InputStreamReader(classpathStream));
        }

        //couldn't find the file anywhere
        return null;
    }

    @Override
    public ZookeeperProperties getLastConnectionProps() {
		return lastConnectionProps;
	}

    @Override
	public void setLastConnectionProps(ZookeeperProperties lastConnectionProps) {
		this.lastConnectionProps = lastConnectionProps;
	}
}
