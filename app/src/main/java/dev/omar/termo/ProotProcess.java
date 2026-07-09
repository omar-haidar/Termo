package dev.omar.termo;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProotProcess {

    private final Context context;

    public ProotProcess(Context context) {
        this.context = context;


    }

    public Process startTerminal() throws IOException {

        String prootPath = XEnvironment.PROOT_PATH;
        String bashPath = XEnvironment.PREFIX + "bin/bash";


        List<String> command = new ArrayList<>();
        command.add(prootPath);


        command.add("-b");
        command.add(XEnvironment.DATA_DIR + ":/data/data/com.termux/files");

        command.add("-0");

        command.add(bashPath);
        command.add("--login");


        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();


        env.put("TERM", "xterm-256color");
        env.put("PREFIX", "/data/data/com.termux/files/usr");
        env.put("HOME", "/data/data/com.termux/files/home");
        env.put("BOOTSTRAP_PREFIX", "/data/data/com.termux/files/usr");
        env.put("PATH", "/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets");
        env.put("LD_LIBRARY_PATH", "/data/data/com.termux/files/usr/lib");
        env.put("TMPDIR", "/data/data/com.termux/files/usr/tmp");

        File homeDir = new File(XEnvironment.HOME);
        if (!homeDir.exists()) homeDir.mkdirs();
        pb.directory(homeDir);

        return pb.start();
    }
}