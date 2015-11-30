package ru.alexp.tools.yadns;

import java.io.File;
import ru.alexp.util.Config;
import ru.alexp.util.Dir;
import ru.alexp.util.Logger;
import ru.alexp.util.Vars;
import ru.alexp.util.json.JSONObject;

/**
 *
 * @author Александр
 */
public class HostConfig {

    private final File config;
    private String profile;

    public static final String HOST = "host";
    public static final String TOKEN = "token";
    public static final String EXPR = "expr";
    public static final String CONTENT = "content";

    public HostConfig() {
        File f = new File(Dir.getIstanse().getApplicationDir(), "config.json");
        if (f.exists()) {
            this.config = f;
        } else {
            f = new File(".", "config.json");
            if (f.exists()) {
                this.config = f;
            } else {
                throw new RuntimeException("Configs not found");
            }
        }
        this.profile = Vars.get("profile", "default").toString();
        loadConfigs();
    }

    public HostConfig(File config) {
        if (config.exists()) {
            this.config = config;
        } else {
            throw new RuntimeException("Config not found");
        }
        this.profile = Vars.get("profile", "default").toString();
        loadConfigs();
    }

    public void loadConfigs() {
        try {
            Config.setConfigFile(config);
            Config.load();
            Logger.debug("Configs loaded from " + config.getAbsolutePath());
        } catch (Exception e) {
            Logger.printStacktrace(e);
        }
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getParam(String profile, String key) {
        try {
            JSONObject json = Config.getMap();
            JSONObject Jprofile = json.getJSONObject(profile);
            if (Jprofile.has(key)) {
                return Jprofile.optString(key);
            } else if (!profile.equalsIgnoreCase("default")) {
                return getParam("default", key);
            } else {
                return "";
            }
        } catch (Exception e) {
            Logger.printStacktrace(e);
            return "";
        }
    }

    public String getParam(String key) {
        return getParam(profile, key);
    }
}
