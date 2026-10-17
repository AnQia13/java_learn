package com.hczn.learn;

import java.util.HashMap;
import java.util.Map;

/**
 * 第 7 课配套：几个「纯函数」工具方法。
 *
 * <p>为什么单独抽这个类：单元测试最好测「输入确定 → 输出确定」的纯逻辑，
 * 不依赖数据库、网络、当前时间、文件系统。这样的测试跑得快、不会偶尔失败。
 *
 * <p>对应测试：{@code src/test/java/com/hczn/learn/TextUtilsTest.java}
 */
public final class TextUtils {

    /** 工具类：私有构造器，防止被 new 出来（没有实例状态，new 出来也没用）。 */
    private TextUtils() {
        throw new AssertionError("工具类不允许实例化");
    }

    public static String reverse(String text) {
        requireNonNull(text);
        return new StringBuilder(text).reverse().toString();
    }

    public static boolean isPalindrome(String text) {
        requireNonNull(text);
        int left = 0;
        int right = text.length() - 1;
        while (left < right) {
            if (text.charAt(left++) != text.charAt(right--)) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, Integer> wordCount(String text) {
        requireNonNull(text);
        Map<String, Integer> count = new HashMap<>();
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return count;
        }
        for (String word : trimmed.split("\\s+")) {      // \\s+ 连续空白算一个分隔符
            count.merge(word, 1, Integer::sum);
        }
        return count;
    }

    public static int max(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("数组不能为 null 或空");
        }
        int result = numbers[0];
        for (int number : numbers) {
            if (number > result) {
                result = number;
            }
        }
        return result;
    }

    private static void requireNonNull(String text) {
        if (text == null) {
            throw new IllegalArgumentException("入参不能为 null");
        }
    }
}
