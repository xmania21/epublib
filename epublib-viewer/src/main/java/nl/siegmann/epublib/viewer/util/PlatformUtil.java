package nl.siegmann.epublib.viewer.util;

public class PlatformUtil {

    private static final String OS_NAME = System.getProperty("os.name", "generic").toLowerCase();
    private static final String JAVAFX_PLATFORM = System.getProperty("javafx.platform", "").toLowerCase();

    /**
     * Determines if the application is running on a mobile OS (Android/iOS)
     * either via standard Java OS checks or Gluon FX properties.
     */
    public static boolean isMobile() {
        return OS_NAME.contains("android") || OS_NAME.contains("ios")
               || JAVAFX_PLATFORM.equals("android") || JAVAFX_PLATFORM.equals("ios");
    }

    /**
     * Determines if the application is running on a desktop OS (Windows/Mac/Linux).
     */
    public static boolean isDesktop() {
        return !isMobile();
    }
}
