package ru.alexp.tools.yadns;

import java.util.Calendar;
import ru.alexp.util.Config;
import ru.alexp.util.Dir;
import ru.alexp.util.Logger;
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
        try {
            Dir.createAppDir("APPDATA", "YandexDnsUpdater");
            Vars.parseArray(args);
            Config.load();
            LogManager.init();
            Logger.none("<!-- Yandex Dns Updater -->");
            Logger.none("<!--  (c) Alex_P  " + Calendar.getInstance().get(Calendar.YEAR) + "  -->");

            if (Vars.containsKey("console")) {
                ConsoleSupport.start();
            }
        } catch (Throwable t) {
            Logger.printStacktrace(t);
        }
    }
}
