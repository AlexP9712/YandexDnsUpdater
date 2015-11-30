package ru.alexp.tools.yadns;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import ru.alexp.util.Dir;
import ru.alexp.util.Logger;
import ru.alexp.util.Vars;

/**
 *
 * @author Александр
 */
public class LogManager {

    private static RandomAccessFile writer;
    private static File logs;

    public static void init() {
        try {
            if (Vars.containsKey("logsDir")) {
                logs = new File((String) Vars.get("logsDir"));
            } else {
                logs = new File(Dir.getIstanse().getApplicationDir(), "logs");
            }
            logs.mkdirs();
            clearLogs();
            final Calendar cal = Calendar.getInstance();
            File logFile = new File(logs, cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) + ".log");
            writer = new RandomAccessFile(logFile, "rw");
            if (logFile.length() > 0) {
                writer.seek(logFile.length());
                writeToLog("\n");
            }
            Logger.addHook(line -> {
                try {
                    writeToLog(line);
                } catch (Exception e) {
                    Logger.printStacktrace(e);
                }
            });
        } catch (Exception e) {
            Logger.printStacktrace(e);
        }
    }

    public static void clearLogs() {
        File[] logFiles = logs.listFiles();
        if (logFiles.length > 30) {
            for (int i = 0; i < logFiles.length - 30; i++) {
                logFiles[i].delete();
            }
        }
    }

    public static void writeToLog(String s) {
        try {
            writer.write(s.getBytes());
        } catch (IOException ex) {
            Logger.printStacktrace(ex);
        }
    }
}
