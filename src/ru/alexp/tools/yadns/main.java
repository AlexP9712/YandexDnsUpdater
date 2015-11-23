package ru.alexp.tools.yadns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import ru.alexp.util.Config;
import ru.alexp.util.Dir;
import ru.alexp.util.Logger;
import ru.alexp.util.Net;
import ru.alexp.util.Vars;

/**
 *
 * @author Александр
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Logger.none("<!-- Yandex Dns Updater -->");
        Logger.none("<!--  (c) Alex_P  " + Calendar.getInstance().get(Calendar.YEAR) +  " -->");
        Dir.createAppDir("APPDATA", "YandexDnsUpdater");
        Vars.parseArray(args);
        Config.load();

        if (Vars.containsKey("console")) {
            try {
                String token = Vars.get("token").toString();
                String host = Vars.get("host").toString();
                String expression = Vars.get("expression").toString();
                String content = Vars.get("content").toString();

                if (content.equalsIgnoreCase("$myip")) {
                    Logger.print("Getting ip ", Logger.Level.Info, false);
                    content = Net.query("https://l2.io/ip").replaceAll("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})", "$1");
                    System.out.println(content);
                }

                Logger.info("Getting dns records");

                Domain domain = new Domain(token, host);

                Logger.info("Loaded " + domain.getDnsRrecords().size() + " records");

                ArrayList<NodeDom> records = domain.getDnsRrecords();
                for (String rule : expression.split(";")) {
                    String[] params = rule.split("=");
                    records = NodeDom.filterListByAttribute(records, params[0], params[1]);
                }

                Logger.warn(records.size() + " records will be updated");
                if (!Vars.containsKey("nowait")) {
                    Logger.warn("You have 20 seconds to abort this program");
                    Thread.sleep(20 * 1000);
                }
                Logger.info("Updating...");

                for (NodeDom node : records) {
                    domain.updateRecord(
                            node.getAttribute("id"),
                            node.getAttribute("subdomain"),
                            content);
                }
                System.out.println();

                System.exit(0);
            } catch (Exception e) {
                Logger.error("ERROR: " + e.getMessage());
                Logger.printStacktrace(e);
            }
        }
    }
}
