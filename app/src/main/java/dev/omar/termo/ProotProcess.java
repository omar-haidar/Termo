package dev.omar.termo;

import android.content.Context;
import dev.omar.common.utils.XEnvironment;
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

        command.add("-b /dev");
        command.add("-b /data");
        command.add("-b /dev/urandom:/dev/random");
        command.add("-b /proc");
        command.add(XEnvironment.DATA_DIR);
        command.add("-b");
        command.add(XEnvironment.PREFIX);
        command.add("-b /proc/self/fd:/dev/fd");
        command.add("-b /sys");
        command.add("-r "+XEnvironment.PREFIX);
        command.add("-0");

        command.add("--link2symlink");
        command.add("--sysvipc");
        command.add("-L");

        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();

        File homeDir = new File(XEnvironment.HOME);
        if (!homeDir.exists()) homeDir.mkdirs();
        pb.directory(homeDir);

        return pb.start();
    }
}
