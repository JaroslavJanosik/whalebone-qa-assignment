package cz.whalebone.util;

public final class Stopwatch {
    private final long startNs;

    private Stopwatch() {
        this.startNs = System.nanoTime();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public long elapsedMs() {
        return (System.nanoTime() - startNs) / 1_000_000L;
    }
}
