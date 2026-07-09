package dev.omar.termo;

import android.content.Context;
import android.system.Os;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BootstrapInstaller {

    private static final String TAG = "BootstrapInstaller";
    private static final String PROOT_URL = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/aarch64/proot";

    public static  CompletableFuture<Boolean> downloadProot(){
        return CompletableFuture.supplyAsync(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                try {
                    URL url = new URL(PROOT_URL);
                    Log.i(TAG,"Connecting to "+PROOT_URL+"...") ;
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    Log.i(TAG,"Connecting successfully!") ;
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG,"ErrorResponseCode : "+connection.getResponseCode()) ;
                        return false;
                    }

                    Log.i(TAG,"Start download proot...") ;
                    File proot = new File("/data/data/dev.omar.termo/files/proot");
                    try (InputStream input = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream output = new FileOutputStream(proot)) {
                        byte[] data = new byte[4096];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                        Os.chmod(proot.getAbsolutePath(),755);
                        return proot.exists() && proot.length() > 0;
                    }
                } catch (Exception e) {
                    return false;
                }

            }
        });
    }
    private static final String BOOTSTRAP_URL = "https://github.com/termux/termux-packages/releases/download/bootstrap-2026.07.05-r1%2Bapt.android-7/bootstrap-aarch64.zip";

    private final Context context;
    private final String filesDir;

    public BootstrapInstaller(Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir().getAbsolutePath();
        Log.d(TAG, "Init BootstrapInstaller");
    }

    public CompletableFuture<Boolean> install() {
        File zipFile = new File(context.getCacheDir(), "bootstrap.zip");

        return downloadBootstrap(zipFile)
                .thenCompose(downloadSuccess -> {
                    if (!downloadSuccess) return CompletableFuture.completedFuture(false);
                    return extractBootstrap(zipFile);
                })
                .thenApply(extractSuccess -> {
                    if (extractSuccess) {
                        setExecutablePermissions(new File(filesDir + "/usr"));
                        Log.d(TAG, "تم تنزيل وفك ضغط الـ Bootstrap بنجاح!");
                    }
                    if (zipFile.exists()) zipFile.delete();
                    return extractSuccess;
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "فشلت العملية: " + throwable.getMessage());
                    if (zipFile.exists()) zipFile.delete();
                    return false;
                });
    }

    private CompletableFuture<Boolean> downloadBootstrap(File targetFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                URL url = new URL(BOOTSTRAP_URL);
                Log.i(TAG,"Connecting to "+BOOTSTRAP_URL) ;
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.i(TAG,"Connecting successfully!") ;
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG,"ErrorResponseCode : "+connection.getResponseCode()) ;
                    return false;
                }

                Log.i(TAG,"Start download bootstrap...") ;
                try (InputStream input = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream output = new FileOutputStream(targetFile)) {
                    byte[] data = new byte[4096];

                    int count;

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);

                    }
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG,"downloadBootstrap : "+e.getMessage()) ;
                return false;
            }
        });
    }

    private CompletableFuture<Boolean> extractBootstrap(File zipFile) {
        return CompletableFuture.supplyAsync(() -> {
            File destDir = new File(filesDir,"usr");
            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(context.getContentResolver().openInputStream(android.net.Uri.fromFile(zipFile))))) {
                ZipEntry ze;
                byte[] buffer = new byte[4096];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(destDir, ze.getName());
                    if (ze.isDirectory()) {
                        file.mkdirs();
                    } else {
                        File parent = file.getParentFile();
                        if (parent != null && !parent.exists()) parent.mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            int len;
                            while ((len = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zis.closeEntry();
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    private void setExecutablePermissions(File file) {
        if (file.isDirectory()) {
            file.setExecutable(true, false);
            file.setReadable(true, false);
            file.setWritable(true, true);
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) setExecutablePermissions(child);
            }
        } else {
            String path = file.getAbsolutePath();
            if (path.contains("/bin/") || path.contains("/lib/") || path.endsWith(".sh")) {
                file.setExecutable(true, false);
            }
            file.setReadable(true, false);
        }
    }
}