package dev.omar.common.utils;

import android.app.Application;
import android.system.ErrnoException;
import android.system.Os;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class XEnvironment {

    public static final String DATA_DIR = "/data/data/dev.omar.termo/files/";
    public static final String LOCAL_DIR = DATA_DIR + "local/";
    public static final String BUSYBOX_PATH = LOCAL_DIR + "busybox";
    public static final String PROOT_PATH = LOCAL_DIR + "proot";
    public static final String TALLOC_PATH = LOCAL_DIR + "libtalloc.so.2";
    public static final String LOADER_PATH = LOCAL_DIR + "loader";
    public static final String HOME = DATA_DIR + "home/";
    public static final String PREFIX = DATA_DIR + "usr/";

    public static final String LIBHOOK2_PATH = LOCAL_DIR + "libhook2.so";
    public static final String LIBHOOK_PATH = LOCAL_DIR + "libhook.so";

    public static void init(Application application) {
        makeDirIfNeed(LOCAL_DIR);
        makeDirIfNeed(PREFIX);
        makeDirIfNeed(HOME);

        try {
            Os.chmod(HOME, 755);
            Os.chmod(PREFIX, 755);
            Os.chmod(LOCAL_DIR, 755);
            Os.chmod(PROOT_PATH, 755);

        } catch (ErrnoException e) {

        }
    }

    @NonNull
    private static File makeDirIfNeed(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static Map<String, String> getEnvironment() {
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("TERM", "xterm-256color");
        environment.put("PREFIX", XEnvironment.PREFIX);
        environment.put("HOME", XEnvironment.HOME);
        environment.put("BOOTSTRAP_PREFIX", XEnvironment.PREFIX);
        environment.put(
                "PATH",
                "/system/bin/:/data/data/dev.omar.termo/files/usr/bin:/data/data/dev.omar.termo/files/usr/bin/applets");
        environment.put(
                "LD_LIBRARY_PATH",
                XEnvironment.LOCAL_DIR
                        + ":/data/data/dev.omar.termo/files/usr/lib:/data/data/dev.omar.termo/files/usr/libexec");
        environment.put("TMPDIR", "/data/data/dev.omar.termo/files/usr/tmp");
        environment.put("BUSYBOX", XEnvironment.BUSYBOX_PATH);
        environment.put("PROOT", XEnvironment.PROOT_PATH);
        environment.put("LD_PRELOAD", XEnvironment.LIBHOOK_PATH);
        return environment;
    }

    public static String[] envToProps(Map<String, String> environment) {
        String[] env = new String[environment.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            env[index] = entry.getKey() + "=" + entry.getValue();
            index++;
        }
        return env;
    }
}
