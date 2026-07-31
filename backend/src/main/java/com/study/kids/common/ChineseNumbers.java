package com.study.kids.common;

public final class ChineseNumbers {

    private static final String[] DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

    private ChineseNumbers() {
    }

    /** 1–100 中文读法 */
    public static String toChinese(int n) {
        if (n < 0 || n > 100) {
            return String.valueOf(n);
        }
        if (n <= 10) {
            return n == 10 ? "十" : DIGITS[n];
        }
        if (n < 20) {
            return "十" + DIGITS[n % 10];
        }
        if (n < 100) {
            int tens = n / 10;
            int ones = n % 10;
            return DIGITS[tens] + "十" + (ones == 0 ? "" : DIGITS[ones]);
        }
        return "一百";
    }
}
