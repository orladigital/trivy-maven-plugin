package br.com.orla.utils;

import static junit.framework.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class OSArchitectureTest {

    @Test
    public void given_win_should_return_windows_os() {
        System.setProperty("os.name", "win");

        var osArch = OSDetector.getOS();
        assertEquals(OS.WINDOWS, osArch);
    }

    @Test
    public void given_nix_should_return_unix_os() {
        System.setProperty("os.name", "Linux");

        var osArch = OSDetector.getOS();
        assertEquals(OS.UNIX, osArch);
    }

    @Test
    public void given_mac_should_return_mac_os() {
        System.setProperty("os.name", "Mac OS X");

        var osArch = OSDetector.getOS();
        assertEquals(OS.MAC_OSX, osArch);
    }
}
