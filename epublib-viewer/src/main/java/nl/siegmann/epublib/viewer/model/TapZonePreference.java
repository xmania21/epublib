package nl.siegmann.epublib.viewer.model;

/**
 * Configurable tap zone presets for the reader canvas.
 * <p>
 * Each preset implements {@link #resolve(double, double, double, double)} to
 * map a tap coordinate to a {@link TapAction} without any branching in the
 * controller. Preferences are persisted under the key {@code tapZoneLayout}.
 * </p>
 */
public enum TapZonePreference {

    /**
     * Classic three-column split: Left=Prev | Centre=Settings | Right=Next.
     * Default preset, matching KOReader / Moon+ Reader behaviour.
     */
    CLASSIC_LCR("Classic (L / C / R)") {
        @Override
        public TapAction resolve(double x, double y, double width, double height) {
            if (x < width * 0.35) return TapAction.PREV;
            if (x > width * 0.65) return TapAction.NEXT;
            return TapAction.SETTINGS;
        }
    },

    /**
     * Two-column split: Left=Prev | Right=Next.
     * Settings panel is accessible via the toolbar only.
     */
    SPLIT_LR("Split (L / R)") {
        @Override
        public TapAction resolve(double x, double y, double width, double height) {
            return x < width * 0.5 ? TapAction.PREV : TapAction.NEXT;
        }
    },

    /**
     * Kindle-inspired split: narrow 25% side zones, wide 50% centre for settings.
     */
    KINDLE_STYLE("Kindle-style") {
        @Override
        public TapAction resolve(double x, double y, double width, double height) {
            if (x < width * 0.25) return TapAction.PREV;
            if (x > width * 0.75) return TapAction.NEXT;
            return TapAction.SETTINGS;
        }
    },

    /**
     * Portrait-thumb-friendly vertical split: Top=Settings | Bottom=Next.
     * Long-press on the bottom half is handled separately as Prev.
     */
    TOP_BOTTOM("Top / Bottom") {
        @Override
        public TapAction resolve(double x, double y, double width, double height) {
            return y < height * 0.5 ? TapAction.SETTINGS : TapAction.NEXT;
        }
    };

    // -------------------------------------------------------------------------

    private final String displayName;

    TapZonePreference(String displayName) {
        this.displayName = displayName;
    }

    /** Human-readable label shown in the settings UI. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a tap at ({@code x}, {@code y}) inside a canvas of the given
     * {@code width} × {@code height} to a {@link TapAction}.
     *
     * @param x      horizontal tap coordinate (scene-relative pixels)
     * @param y      vertical tap coordinate (scene-relative pixels)
     * @param width  total canvas width in pixels
     * @param height total canvas height in pixels
     * @return the action that should be triggered
     */
    public abstract TapAction resolve(double x, double y, double width, double height);

    // -------------------------------------------------------------------------

    /** Actions that a tap zone can trigger. */
    public enum TapAction {
        PREV,
        NEXT,
        SETTINGS
    }
}
