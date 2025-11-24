package org.me.gcu.cw1;

public class ColorUtils {
    public static int getBackgroundColorForRate(double rate) {
        if (rate < 1.0) return 0xFFE0F7FA;
        else if (rate < 5.0) return 0xFFDFF0D8;
        else if (rate < 10.0) return 0xFFFFF4CC;
        else return 0xFFFFD6D6;
    }

    public static int getTextColorForRate(double rate) {
        return 0xFF000000;
    }
}
