package dev.omar.termo.utils;

import android.os.Build;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.Nullable;
import java.util.Arrays;

public final class ArchUtils {

    public static boolean isAarch64() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains("arm64-v8a");
    }

    public static boolean isArmv7a() {
        return Arrays.asList(Build.SUPPORTED_ABIS).contains("armeabi-v7a");
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    public static boolean isAndroid12() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    @Nullable
    public static String getArch() {
        if (ArchUtils.isAarch64()) {
            return "arm64-v8a";
        } else if (ArchUtils.isArmv7a()) {
            return "armeabi-v7a";
        }
        return null;
    }
}
