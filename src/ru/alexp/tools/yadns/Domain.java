package ru.alexp.tools.yadns;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import ru.alexp.util.Logger;

/**
 *
 * @author Александр
 */
public class Domain {

    private static final String domainCheckerUri = "https://pddimp.yandex.ru/nsapi/get_domain_records.xml?token=%1s&domain=%2s";
    private static final String domainUpdateRecordUri = "https://pddimp.yandex.ru/nsapi/edit_a_record.xml?token=%1s&domain=%2s&subdomain=%3s&record_id=%4s&content=%5s";

    private final String token;
    private final String name;
    private NodeDom configs;
    private boolean isForceMode = false;

    public Domain(String token, String name) throws ParserConfigurationException, SAXException, IOException, DomainException {
        this.token = token;
        this.name = name;
        initDonainInfo();
    }

    private void initDonainInfo() throws ParserConfigurationException, SAXException, IOException, DomainException {
        Logger.debug("Getting records of " + name);
        NodeDom prop = NodeDom.parseUri(String.format(domainCheckerUri,
                URLEncoder.encode(token, "UTF-8"),
                URLEncoder.encode(name, "UTF-8")), true);
        if (!prop.getFirstElementByName("error").val().equalsIgnoreCase("ok")) {
            throw new DomainException(prop.getFirstElementByName("error").val());
        }
        NodeDom DomainProp = prop.getFirstElementByName("domain").setMultiLevel(false);
        if (!DomainProp.getFirstElementByName("name").val().equalsIgnoreCase(name)) {
            throw new DomainException(name + " not equal with " + DomainProp.getFirstElementByName("name").val() + " from response");
        }
        this.configs = DomainProp.getFirstElementByName("response");
    }

    public ArrayList<NodeDom> getDnsRrecords() {
        return configs.getElementsByName("record");
    }

    public void updateRecord(String id, String subdomain, String content) throws Exception, DomainException {
        Logger.debug("Updating record of " + name + ". Record id: " + id + ". Content: " + content);
        if (isForceMode || !NodeDom.getNodeByAttribute(getDnsRrecords(), "id", id).val().equals(content)) {
            NodeDom prop = NodeDom.parseUri(String.format(
                    domainUpdateRecordUri,
                    URLEncoder.encode(token, "UTF-8"),
                    URLEncoder.encode(name, "UTF-8"),
                    URLEncoder.encode(subdomain, "UTF-8"),
                    URLEncoder.encode(id, "UTF-8"),
                    URLEncoder.encode(content, "UTF-8")), true);
            if (!prop.getFirstElementByName("error").val().equalsIgnoreCase("ok")) {
                throw new DomainException(prop.getFirstElementByName("error").val());
            }
        } else {
            Logger.debug("No need to update that record. Content already setted.");
        }
    }

    public void setForceMode(boolean b) {
        this.isForceMode = b;
    }
}
