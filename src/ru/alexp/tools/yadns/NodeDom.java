package ru.alexp.tools.yadns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.alexp.util.Logger;

/**
 *
 * @author Александр
 */
public class NodeDom {

    private final Node root;
    private boolean isMultiLevel = false;
    private NodeDom parent;

    private String requestUri;

    public NodeDom(Node n) {
        this.root = n;
    }

    public NodeDom(Node n, boolean isMultiLevel) {
        this.root = n;
        this.isMultiLevel = isMultiLevel;
    }

    private NodeDom(Node n, boolean isMultiLevel, NodeDom parent) {
        this.root = n;
        this.isMultiLevel = isMultiLevel;
        this.parent = parent;
    }

    public Node getNode() {
        return root;
    }

    public ArrayList<NodeDom> getElementsByName(String name) {
        ArrayList<NodeDom> nodelist = new ArrayList<>();
        if (root.hasChildNodes()) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeName().matches(name)) {
                    nodelist.add(parseNode(n));
                }
                if (isMultiLevel() && n.hasChildNodes()) {
                    nodelist.addAll(parseNode(n).getElementsByName(name));
                }
            }
        }
        return nodelist;
    }

    public NodeDom getFirstElementByName(String name) {
        NodeDom response = null;
        if (root.hasChildNodes()) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeName().matches(name)) {
                    return parseNode(n);
                }
                if (isMultiLevel() && n.hasChildNodes()) {
                    response = parseNode(n).getFirstElementByName(name);
                }
            }
        }
        return response;
    }

    public boolean hasAttributes() {
        return root.hasAttributes();
    }

    public boolean hasAttribute(String key) {
        return hasAttributes() && root.getAttributes().getNamedItem(key) != null;
    }

    public Map<String, String> getAttributes() {
        Map<String, String> params = new HashMap<>();
        if (hasAttributes()) {
            NamedNodeMap nmap = root.getAttributes();
            for (int i = 0; i < nmap.getLength(); i++) {
                Node n = nmap.item(i);
                params.put(n.getNodeName(), n.getNodeValue());
            }
        }
        return params;
    }

    public String getAttribute(String key) {
        if (hasAttributes()) {
            Node n = root.getAttributes().getNamedItem(key);
            if (n != null) {
                return n.getNodeValue();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean has(String name) {
        if (root.hasChildNodes()) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeName().matches(name)) {
                    return true;
                }
                if (isMultiLevel() && n.hasChildNodes()) {
                    return parseNode(n).has(name);
                }
            }
        }
        return false;
    }

    public String val() {
        if (root.hasChildNodes()) {
            return root.getChildNodes().item(0).getNodeValue();
        } else {
            return root.getNodeValue();
        }
    }

    public NodeDom setMultiLevel(boolean b) {
        this.isMultiLevel = b;
        return this;
    }

    public NodeDom getParent() {
        if (this.parent != null) {
            return parent;
        } else {
            Node n = root.getParentNode();
            if (n == null) {
                return null;
            }
            return NodeDom.fromNode(n, this.isMultiLevel);
        }
    }

    public NodeDom getRoot() {
        if (getParent() == null) {
            return this;
        }
        return getParent().getRoot();
    }

    public String getName() {
        return root.getNodeName();
    }

    private NodeDom parseNode(Node n) {
        return new NodeDom(n, isMultiLevel(), this);
    }

    public boolean isMultiLevel() {
        return isMultiLevel;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public static NodeDom fromNode(Node n) {
        return new NodeDom(n);
    }

    public static NodeDom fromNode(Node n, boolean isMultiLevel) {
        return new NodeDom(n, isMultiLevel);
    }

    public static NodeDom parseUri(String uri, boolean isMultiLevel) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Logger.debug("Parsing uri to xml: " + uri);
        Document doc = xml.parse(uri);
        NodeDom result = NodeDom.fromNode(doc.getDocumentElement(), isMultiLevel).getRoot();
        result.requestUri = uri;
        return result;
    }

    public static NodeDom parseUri(String uri) throws ParserConfigurationException, SAXException, IOException {
        return parseUri(uri, false);
    }

    public static NodeDom getNodeByAttribute(ArrayList<NodeDom> list, String key, String value) {
        for (NodeDom node : list) {
            if (node.hasAttribute(key) && node.getAttribute(key).matches(value)) {
                return node;
            }
        }
        return null;
    }
    
    public static ArrayList<NodeDom> filterListByAttribute(ArrayList<NodeDom> list, String key, String value) {
        ArrayList<NodeDom> resp = new ArrayList<>();
        for (NodeDom node : list) {
            if (!node.hasAttribute(key) || node.getAttribute(key).matches(value)) {
                resp.add(node);
            }
        }
        return resp;
    }
}
