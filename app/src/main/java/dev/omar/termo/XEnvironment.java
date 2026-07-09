package dev.omar.termo;

import android.app.Application;
import android.system.ErrnoException;
import android.system.Os;

import androidx.annotation.NonNull;

import java.io.File;

public final class XEnvironment {

    public static final String DATA_DIR = "/data/data/dev.omar.termo/files/";
    public static final String LOCAL_DIR = DATA_DIR + "local/";
    public static final String PROOT_PATH = LOCAL_DIR + "proot";
    public static final String TALLOC_PATH = LOCAL_DIR + "libtalloc.so.2";
    public static final String LOADER_PATH = LOCAL_DIR + "loader";
    public static final String HOME = DATA_DIR + "home/";
    public static final String PREFIX = DATA_DIR + "usr/";

    public static void init(Application application) {
        makeDirIfNeed(LOCAL_DIR);
        makeDirIfNeed(PREFIX);
        makeDirIfNeed(HOME);

        try {
            Os.chmod(HOME,755);
            Os.chmod(PREFIX,755);
            Os.chmod(LOCAL_DIR,755);
            Os.chmod(PROOT_PATH,755);

        } catch (ErrnoException e) {

        }
    }

    @NonNull
    private static File makeDirIfNeed(String path){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
}
