package br.com.orla.utils;

public class OSDetector {

    public static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OS.UNIX;
        } else if ("Mac OS X".equalsIgnoreCase(osName)) {
            return OS.MAC_OSX;
        } else {
            throw new OsDetectionException("Unrecognized OS: " + osName);
        }
    }
}
