package com.API.Documents_Management.Utils;

public class FormatUtils {

    public static String formatFileSize(long sizeInBytes) {
        double sizeInKB = sizeInBytes / 1024.0;
        if (sizeInKB < 1024) {
            return String.format("%.0f KB", sizeInKB);  // with KO if < 1MB
        } else {
            double sizeInMB = sizeInKB / 1024.0;
            return String.format("%.0f MB", sizeInMB);  // else with  MB
        }
    }
}
