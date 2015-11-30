package ru.alexp.tools.yadns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import ru.alexp.util.Config;
import ru.alexp.util.Logger;
import ru.alexp.util.Vars;

/**
 *
 * @author Александр
 */
public class ConsoleSupport {

    private static HostConfig hconf;
    private String myip;

    public void run() {
        try {
            String[] profiles = Vars.get("profiles", "default").toString().split(",");
            for (String profile : profiles) {
                try {
                    Logger.info("Loading " + profile + " profile.");
                    hconf.setProfile(profile);
                    Domain domain = new Domain(hconf.getParam(HostConfig.TOKEN),
                            hconf.getParam(HostConfig.HOST));
                    Logger.info("Loaded " + domain.getDnsRrecords().size() + " records.");
                    String[] exprs = hconf.getParam(HostConfig.EXPR).split(";");
                    Logger.info("Loaded " + exprs.length + " rules." + (exprs.length == 0 ? " Skipping." : ""));
                    if (exprs.length != 0) {
                        ArrayList<NodeDom> records = domain.getDnsRrecords();

                        for (String expr : exprs) {
                            String[] ex = expr.split("=");
                            records = NodeDom.filterListByAttribute(records, ex[0], ex[1]);
                        }

                        Logger.warn(records.size() + " records will be updated.");
                        if (!(Vars.containsKey("nowait") || hconf.getParam("nowait").equalsIgnoreCase("true"))) {
                            Logger.warn("You haму 20 seconds to abort this programm.");
                            try {
                                Thread.sleep(20 * 1000);
                            } catch (InterruptedException e) {
                                Logger.printStacktrace(e);
                            }
                        }
                        Logger.warn("Updating ...");
                        for (NodeDom record : records) {
                            String content = hconf.getParam(HostConfig.CONTENT);
                            if (content.matches("^\\$?myip$")) {
                                content = getMyIP();
                            }
                            domain.updateRecord(
                                    record.getAttribute("id"),
                                    record.getAttribute("subdomain"),
                                    content);
                        }
                    }
                } catch (ParserConfigurationException | SAXException | IOException | DomainException | NullPointerException e) {
                    Logger.printStacktrace(e);
                }
            }
        } catch (Exception e) {
            Logger.printStacktrace(e);
        }
    }

    private String getMyIP() {
        if (myip == null) {
            Logger.info("Getting ip.");
            myip = MyIp.getIp();
            Logger.info("Your ip is " + myip);
        }
        return myip;
    }

    public static void start() {

        if (Vars.containsKey("src")) {
            hconf = new HostConfig(new File(Vars.get("src").toString()));
        } else {
            hconf = new HostConfig();
        }

        int delay = Integer.MAX_VALUE;
        if (Vars.containsKey("delay") || Config.contains("delay")) {
            try {
                delay = Integer.parseInt(Vars.get("delay").toString());
            } catch (Exception e) {
                try {
                    delay = Integer.parseInt(Config.get("delay").toString());
                } catch (Exception ex) {
                    Logger.printStacktrace(e);
                    Logger.printStacktrace(ex);
                }
            }
        }

        if (delay > 0) {
            Logger.info("Delay is " + delay + " seconds.");
            do {
                hconf.loadConfigs();
                try {
                    new ConsoleSupport().run();
                } catch (Throwable t) {
                    Logger.printStacktrace(t);
                }
                if (delay != Integer.MAX_VALUE) {
                    Logger.info("Waiting " + delay + " seconds.");
                    Logger.writeLine();
                }
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException e) {
                    Logger.printStacktrace(e);
                    System.exit(1);
                }
                System.gc();
            } while (!Thread.interrupted() || delay != Integer.MAX_VALUE);
        } else {
            throw new RuntimeException("Delay must be > 0!");
        }
    }
}
