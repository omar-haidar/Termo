package dev.omar.termo;

import android.content.Context;
import android.content.res.AssetManager;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetsInstaller {
    private static final String TAG = "AssetsInstaller";

    public static CompletableFuture<Boolean> install(Context context) {
        return installAssets(context)
                .thenCompose((isSuccess) -> {
                    if (!isSuccess) return CompletableFuture.completedFuture(false);
                    return extractBootstrap(context, new File(context.getCacheDir(), "bootstrap.zip"));
                }).thenApply(extractSuccess -> {
                    Log.i(TAG, "Asset extraction state : " + extractSuccess);
                    if (extractSuccess) {
                        try {
                            Os.chmod(XEnvironment.PREFIX,755);
                        } catch (ErrnoException e) {
                            Log.e(TAG,e.getMessage());
                        }
                        FileUtils.createOrExistsFile(XEnvironment.LOCAL_DIR + "assets_installed");                    }
                    return extractSuccess;
                });
    }


    private static void setExecutablePermissions(@NonNull File file) {
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

    private static CompletableFuture<Boolean> installAssets(Context context) {
        return CompletableFuture.supplyAsync(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                try {
                    Log.i(TAG, "Asset extraction started...");
                    final AssetManager assetsManager = context.getAssets();
                    copy(assetsManager.open("proot"), new FileOutputStream(new File(XEnvironment.PROOT_PATH)));
                    copy(assetsManager.open("libtalloc.so.2"), new FileOutputStream(new File(XEnvironment.TALLOC_PATH)));
                    copy(assetsManager.open("loader"), new FileOutputStream(new File(XEnvironment.LOADER_PATH)));
                    copy(assetsManager.open("bootstrap-aarch64.zip"), new FileOutputStream(new File(context.getCacheDir(), "bootstrap.zip")));
                    Log.i(TAG, "Asset extraction Finished...");
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Asset extraction failed : " + e.getMessage());
                    return false;
                }
            }
        });
    }

    private static CompletableFuture<Boolean> extractBootstrap(Context context, File zipFile) {
        return CompletableFuture.supplyAsync(() -> {
            Log.i(TAG, "Bootstrap extraction started...");
            File destDir = new File(XEnvironment.PREFIX);
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
                Log.i(TAG, "Bootstrap extraction finished!");
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Bootstrap extraction failed : " + e.getMessage());
                return false;
            }
        });
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int len;
        byte[] buff = new byte[2048];
        while ((len = in.read(buff)) != -1) {
            out.write(buff, 0, len);
        }
        out.flush();
        out.close();
    }

}
