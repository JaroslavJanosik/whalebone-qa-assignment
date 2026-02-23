package cz.whalebone.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class Config {

    private static final Properties PROPS = load("config.properties");
    private static final Set<String> BROWSERS = Set.of("chromium", "firefox", "webkit", "chrome", "edge", "msedge");

    private Config() {
    }

    // API
    public static String apiBaseUrl() {
        return baseUrl("api.baseUrl");
    }

    public static int apiRetryCount() {
        return optInt("api.retry.count", 0, 10, 0);
    }

    // UI
    public static String uiBaseUrl() {
        return baseUrl("ui.baseUrl");
    }

    public static int uiTimeoutMs() {
        return reqInt("ui.timeoutMs", 1, 300_000);
    }

    public static String uiBrowser() {
        String v = opt("ui.browser", "chromium").toLowerCase(Locale.ROOT);
        if (!BROWSERS.contains(v))
            throw new IllegalStateException("ui.browser must be one of " + BROWSERS + ", got: " + v);
        return switch (v) {
            case "chrome", "edge", "msedge" -> "chromium";
            default -> v;
        };
    }

    public static String uiBrowserChannel() {
        String v = opt("ui.browser", "chromium").toLowerCase(Locale.ROOT);
        return switch (v) {
            case "chrome" -> "chrome";
            case "edge", "msedge" -> "msedge";
            default -> null;
        };
    }

    public static boolean uiHeaded() {
        String legacy = System.getProperty("headed"); // optional legacy flag
        return (legacy != null && !legacy.isBlank())
                ? bool("headed", legacy)
                : bool("ui.headed", opt("ui.headed", "false"));
    }

    public static int uiSlowMoMs() {
        return optInt("ui.slowMoMs", 0, 60_000, 0);
    }

    public static int uiViewportWidth() {
        return optInt("ui.viewport.width", 320, 8192, 1920);
    }

    public static int uiViewportHeight() {
        return optInt("ui.viewport.height", 320, 8192, 1080);
    }

    public static int uiRetryCount() {
        return optInt("ui.retry.count", 0, 10, 0);
    }

    // Artifacts
    public static String artifactsDir() {
        return opt("artifacts.dir", "playwright-artifacts");
    }

    public static boolean uiTraceOnFailure() {
        return bool("ui.trace.onFailure", opt("ui.trace.onFailure", "true"));
    }

    public static boolean uiVideoOnFailure() {
        return bool("ui.video.onFailure", opt("ui.video.onFailure", "true"));
    }

    public static boolean uiScreenshotOnFailure() {
        return bool("ui.screenshot.onFailure", opt("ui.screenshot.onFailure", "true"));
    }

    private static Properties load(String name) {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IllegalStateException(name + " not found on classpath");
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + name, e);
        }
    }

    private static String baseUrl(String key) {
        String raw = req(key).trim();
        try {
            URI uri = new URI(raw);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException(key + " must be absolute URL, got: " + raw);
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException(key + " must be valid URL, got: " + raw, e);
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    private static String req(String key) {
        String v = resolve(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing required property: " + key);
        return v;
    }

    private static String opt(String key, String def) {
        String v = resolve(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    private static int reqInt(String key, int min, int max) {
        return parseInt(key, req(key), min, max);
    }

    private static int optInt(String key, int min, int max, int def) {
        String raw = resolve(key);
        return (raw == null || raw.isBlank()) ? def : parseInt(key, raw.trim(), min, max);
    }

    private static int parseInt(String key, String raw, int min, int max) {
        try {
            int v = Integer.parseInt(raw);
            if (v < min || v > max)
                throw new IllegalStateException(key + " out of range [" + min + "," + max + "]: " + v);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " must be integer, got: " + raw, e);
        }
    }

    private static boolean bool(String key, String raw) {
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if (!v.equals("true") && !v.equals("false"))
            throw new IllegalStateException(key + " must be true/false, got: " + raw);
        return Boolean.parseBoolean(v);
    }

    // -Dkey=value > ENV KEY > config.properties
    private static String resolve(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;

        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_');
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;

        return PROPS.getProperty(key);
    }
}