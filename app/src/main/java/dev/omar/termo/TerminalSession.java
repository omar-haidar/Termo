package dev.omar.termo;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TerminalSession {

    private final Context context;
    private final String filesDir;
    private final String usrDir;

    public TerminalSession(Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir().getAbsolutePath();
        this.usrDir = filesDir + "/usr";
    }

    public Process startTerminal() throws IOException {
        // 1. تحديد مسار تنفيذ أداة proot ومفسر الأوامر bash داخل حزمتك
        String prootPath = usrDir + "/bin/proot";
        String bashPath = usrDir + "/bin/bash";

        // 2. بناء أمر PRoot لتزييف المسارات
        // الفكرة: نقوم بعمل الخيار -b لربط مسار تطبيقك الحقيقي بالمسار الوهمي الذي تبحث عنه الحزم
        List<String> command = new ArrayList<>();
        command.add(prootPath);

        // تزييف المسار الرئيسي (جعل com.termux يشير إلى dev.omar.termo)
        command.add("-b");
        command.add(filesDir + ":/data/data/com.termux/files");

        // خيارات إضافية لـ proot لضمان استقرار الصلاحيات والروابط الرمزية داخل أندرويد
        command.add("-0"); // لتزييف صلاحيات الـ Root (مهم جداً لعمل بعض حزم apt بسلاسة)

        // تحديد مسار التشغيل النهائي وهو مفسر الـ bash
        command.add(bashPath);
        command.add("--login");

        // 3. إعداد البيئة (Environment Variables)
        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();

        // هنا نقوم بتهيئة المتغيرات لتبدو وكأنها داخل بيئة Termux الرسمية تماماً
        env.put("TERM", "xterm-256color");
        env.put("PREFIX", "/data/data/com.termux/files/usr");
        env.put("HOME", "/data/data/com.termux/files/home");
        env.put("BOOTSTRAP_PREFIX", "/data/data/com.termux/files/usr");
        env.put("PATH", "/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets");
        env.put("LD_LIBRARY_PATH", "/data/data/com.termux/files/usr/lib");
        env.put("TMPDIR", "/data/data/com.termux/files/usr/tmp");

        // تحديد مسار العمل الافتراضي (المجلد الذي يفتح عليه المحاكي)
        File homeDir = new File(filesDir + "/home");
        if (!homeDir.exists()) homeDir.mkdirs();
        pb.directory(homeDir);

        // 4. تشغيل العملية
        return pb.start();
    }
}