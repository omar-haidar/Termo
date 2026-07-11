package dev.omar.common.models;

import android.os.Bundle;

public class Result {

    public final boolean success;
    public final String message;

    public final Throwable exception;

    public final Bundle data;

    public Result(boolean success, String message, Throwable exception) {
        this(success, message, exception, null);
    }

    public Result(boolean success, String message) {
        this(success, message, null, null);
    }

    public Result(boolean success, String message, Bundle data) {
        this(success, message, null, data);
    }

    public Result(boolean success, String message, Throwable exception, Bundle data) {
        this.success = success;
        this.message = message;
        this.exception = exception;
        this.data = data;
    }

    public static Result success() {
        return new Result(true, "Success.");
    }

    public static Result success(Bundle data) {
        return new Result(true, "Success.", data);
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }

    public static Result failure(Throwable exception, Bundle data) {
        return new Result(false, "Failure.", exception, data);
    }
}
