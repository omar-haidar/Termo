package dev.omar.termo.utils;

import android.content.Context;

import android.os.SystemClock;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ResourceUtils;

import com.blankj.utilcode.util.ZipUtils;
import dev.omar.common.models.Result;
import dev.omar.common.utils.XEnvironment;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class TerminalInstaller {

    public interface ProgressListener {
        void onProgressUpdate(String message, int value);
    }

    public static CompletableFuture<Result> checkAndInstallTerminal(
            Context context, ProgressListener listener) {
        if (new File(XEnvironment.PREFIX,".packages_installed").exists()) {
            return CompletableFuture.completedFuture(Result.success());
        }
        return installBusybox(context, listener)
                .thenCompose(
                        (result) -> {
                            if (result.success) {
                                return installPackages(context, listener);
                            } else {
                                return CompletableFuture.completedFuture(result);
                            }
                        })
                .thenCompose(
                        (result) -> {
                            if (result.success) {
                                try {
                                    Runtime.getRuntime()
                                            .exec("chmod -R 755 " + XEnvironment.DATA_DIR)
                                            .waitFor();
                                } catch (Exception err) {

                                }
                            }
                            return CompletableFuture.completedFuture(result);
                        });
    }

    private static CompletableFuture<Result> installBusybox(
            Context context, ProgressListener listener) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (listener != null) {
                        listener.onProgressUpdate("Start busybox installing...", 1);
                    }
                    boolean state =
                            ResourceUtils.copyFileFromAssets(
                                    ArchUtils.getArch() + "/busybox", XEnvironment.BUSYBOX_PATH);
                    ResourceUtils.copyFileFromAssets(
                            ArchUtils.getArch() + "/libhook.so", XEnvironment.LIBHOOK_PATH);
                    ResourceUtils.copyFileFromAssets(
                            ArchUtils.getArch() + "/libhook2.so", XEnvironment.LIBHOOK2_PATH);
                            ResourceUtils.copyFileFromAssets(
                            "proot", XEnvironment.PROOT_PATH);
                            ResourceUtils.copyFileFromAssets(
                            "libtalloc.so.2", XEnvironment.TALLOC_PATH);

                    if (state) {
                        setExecutablePermissions(new File(XEnvironment.BUSYBOX_PATH));
                        return Result.success();
                    } else {
                        return Result.failure("Failed to install bsybox!");
                    }
                });
    }

    private static CompletableFuture<Result> installPackages(
            Context context, ProgressListener listener) {
        return CompletableFuture.supplyAsync(
                () -> {
                    final File file = new File(context.getCacheDir(), "bootstrap.zip");
                    if (listener != null) {
                        listener.onProgressUpdate("Start usr.tar.xz forking...", 2);
                    }
                    boolean state =
                            ResourceUtils.copyFileFromAssets(
                                    ArchUtils.getArch() + "/bootstrap-aarch64.zip", file.getAbsolutePath());
                    if (state) {
                        try {
                            if (listener != null) {
                                listener.onProgressUpdate("Start user.tar.xz extracting...", 2);
                            }
        
                            /* String command = XEnvironment.BUSYBOX_PATH
                                            + " unzip "
                                            + " -d "
                                            + XEnvironment.PREFIX
                                            + file.getAbsolutePath();
                            Runtime.getRuntime().exec(command).waitFor();
                            */
                            ZipUtils.unzipFile(file,new File(XEnvironment.PREFIX));
                            FileUtils.createOrExistsFile(new File(XEnvironment.PREFIX,".packages_installed"));
                            return Result.success();
                        } catch (Exception err) {
                            return Result.failure(
                                    "Failed to extract usr.tar.xz because : " + err.getMessage());
                        }
                    } else {
                        return Result.failure("Failed to install terminal packages!");
                    }
                });
    }

    private static void setExecutablePermissions(File file) {
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
            file.setExecutable(true, false);
            file.setReadable(true, false);
            file.setWritable(true, true);
        }
    }
}
