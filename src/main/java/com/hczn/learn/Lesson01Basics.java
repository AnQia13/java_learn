package com.hczn.learn;

import java.util.Arrays;

/**
 * 第 1 课：语言基础。
 *
 * <p>运行方式：
 * <pre>
 *   javac -d target/classes $(find src/main/java -name '*.java')
 *   java -cp target/classes com.hczn.learn.Lesson01Basics
 * </pre>
 *
 * <p>本课覆盖：基本类型、运算符、流程控制、数组、方法、String、包装类。
 * 每一小节都以 {@code section(...)} 打印标题，方便对照输出结果。
 */
public class Lesson01Basics {

    public static void main(String[] args) {
        basicTypes();
        operators();
        controlFlow();
        arrays();
        methods();
        strings();
        wrapperClasses();
        exercises();
    }

    // ==================== 1. 基本类型 ====================

    private static void basicTypes() {
        section("1. 基本类型（8 种）");

        // 整数：byte(1字节) short(2) int(4) long(8)
        byte b = 127;              // -128 ~ 127
        short s = 32000;
        int i = 2_000_000_000;     // 下划线只是给人看的，编译器会忽略
        long l = 9_000_000_000L;   // 超出 int 范围必须加 L，否则编译报错

        // 浮点：float(4) double(8)，小数默认是 double
        float f = 3.14f;           // float 字面量必须加 f
        double d = 3.14159265358979;

        // 字符与布尔
        char c = 'A';              // 单引号，本质是 16 位无符号整数
        boolean flag = true;

        System.out.println("byte=" + b + ", short=" + s + ", int=" + i + ", long=" + l);
        System.out.println("float=" + f + ", double=" + d);
        System.out.printf("char=%c (对应编码=%d), boolean=%b%n", c, (int) c, flag);

        // var：Java 10+ 的局部变量类型推断，类型在编译期就确定了，不是动态类型
        var message = "类型是 String";   // 等价于 String message = "..."
        var count = 10;                  // 等价于 int count = 10
        System.out.println("var: " + message + ", count=" + count);

        // 注意：浮点数有精度问题，金额计算请用 BigDecimal
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2));   // 不是精确的 0.3
    }

    // ==================== 2. 运算符 ====================

    private static void operators() {
        section("2. 运算符");

        // 整数除法会截断小数部分，这是新手最常见的坑
        System.out.println("7 / 2 = " + (7 / 2));       // 3，不是 3.5
        System.out.println("7 % 2 = " + (7 % 2));       // 1
        System.out.println("7.0 / 2 = " + (7.0 / 2));   // 3.5，有 double 参与就按浮点算

        // 负数取模：结果的符号跟被除数一致
        System.out.println("-7 % 2 = " + (-7 % 2));     // -1

        // ++i 先自增再取值；i++ 先取值再自增
        int i = 5;
        System.out.println("i++ 返回 " + (i++) + "，之后 i=" + i);  // 5, 6
        System.out.println("++i 返回 " + (++i) + "，之后 i=" + i);  // 7, 7

        // 短路运算：&& 左边为 false 就不再看右边
        int[] nums = {1, 2, 3};
        int index = 2;
        boolean safe = index < nums.length && nums[index] > 0;  // 顺序反过来可能越界
        System.out.println("短路运算结果=" + safe);

        // 三元运算符
        int score = 75;
        String level = score >= 60 ? "及格" : "不及格";
        System.out.println(score + " 分 → " + level);

        // 位运算（了解即可）
        System.out.println("6 & 3 = " + (6 & 3) + ", 6 | 3 = " + (6 | 3) + ", 6 ^ 3 = " + (6 ^ 3));
        System.out.println("1 << 3 = " + (1 << 3) + "（左移等于乘 2 的幂）");
    }

    // ==================== 3. 流程控制 ====================

    private static void controlFlow() {
        section("3. 流程控制");

        // if / else if / else
        int age = 20;
        if (age < 18) {
            System.out.println("未成年");
        } else if (age < 60) {
            System.out.println("成年");
        } else {
            System.out.println("老年");
        }

        // switch：匹配 byte/short/char/int/String/enum
        int week = 3;
        switch (week) {
            case 1:
                System.out.println("周一");
                break;              // 忘了 break 会「贯穿」执行下一个 case
            case 3:
                System.out.println("周三");
                break;
            default:
                System.out.println("其他");
        }

        // for / 增强 for / while / do-while
        int sum = 0;
        for (int n = 1; n <= 5; n++) {
            sum += n;
        }
        System.out.println("1..5 求和 = " + sum);

        int[] arr = {10, 20, 30};
        for (int value : arr) {     // 增强 for：只读遍历，无法改数组元素
            System.out.print(value + " ");
        }
        System.out.println();

        int n = 0;
        while (n < 3) {             // 先判断后执行
            n++;
        }
        do {                        // 先执行后判断，至少执行一次
            n++;
        } while (n < 3);
        System.out.println("while/do-while 结果 n=" + n);

        // break 跳出整个循环，continue 只跳过本次
        for (int k = 1; k <= 6; k++) {
            if (k % 2 == 0) {
                continue;           // 跳过偶数
            }
            if (k > 5) {
                break;              // 到 5 之后停止
            }
            System.out.print(k + " ");
        }
        System.out.println();
    }

    // ==================== 4. 数组 ====================

    private static void arrays() {
        section("4. 数组");

        int[] a = new int[3];           // 默认值 0
        int[] b = {1, 2, 3};            // 字面量初始化
        int[] c = new int[]{4, 5, 6};

        System.out.println("b.length = " + b.length);   // length 是属性，没有括号

        // 直接打印数组只会得到地址，要用 Arrays.toString
        System.out.println("直接打印: " + b);
        System.out.println("Arrays.toString: " + Arrays.toString(b));

        // 二维数组：数组的数组
        int[][] matrix = {{1, 2}, {3, 4}, {5, 6}};
        System.out.println("matrix[1][1] = " + matrix[1][1]);
        System.out.println("二维: " + Arrays.deepToString(matrix));

        // 越界会抛 ArrayIndexOutOfBoundsException，这是运行时异常
        try {
            System.out.println(c[10]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("访问 c[10] 抛出: " + e.getClass().getSimpleName());
        }

        // 排序、查找、填充
        int[] d = {5, 2, 9, 1};
        Arrays.sort(d);
        System.out.println("排序后: " + Arrays.toString(d));
        System.out.println("9 的下标 = " + Arrays.binarySearch(d, 9));  // 必须先排序
    }

    // ==================== 5. 方法 ====================

    private static void methods() {
        section("5. 方法");

        System.out.println("add(3, 4) = " + add(3, 4));
        System.out.println("add(3.5, 4.5) = " + add(3.5, 4.5));   // 重载：按参数类型选择
        System.out.println("sum(1,2,3) = " + sum(1, 2, 3));
        System.out.println("sum() = " + sum());                   // 可变参数可以为空

        // 值传递：基本类型传的是副本，改不到原变量
        int x = 10;
        changePrimitive(x);
        System.out.println("调用后 x = " + x + "（没被改变）");

        // 对象传的是「引用的副本」：改内容有效，改指向无效
        int[] data = {1, 2};
        changeArrayContent(data);
        System.out.println("改内容后 data = " + Arrays.toString(data));   // 变了
        changeArrayReference(data);
        System.out.println("改指向后 data = " + Arrays.toString(data));   // 没变
    }

    private static int add(int a, int b) {
        return a + b;
    }

    private static double add(double a, double b) {   // 方法重载：同名不同参
        return a + b;
    }

    private static int sum(int... numbers) {          // 可变参数，本质是数组
        int total = 0;
        for (int n : numbers) {
            total += n;
        }
        return total;
    }

    private static void changePrimitive(int v) {
        v = 999;                                     // 只改了副本
    }

    private static void changeArrayContent(int[] arr) {
        arr[0] = 999;                                // 改的是同一个数组对象
    }

    private static void changeArrayReference(int[] arr) {
        arr = new int[]{7, 7};                       // 只改了局部引用
    }

    // ==================== 6. String ====================

    private static void strings() {
        section("6. String");

        String s1 = "hello";
        String s2 = "hello";
        String s3 = new String("hello");

        // == 比较的是引用地址；equals 比较的是内容
        System.out.println("s1 == s2 : " + (s1 == s2));          // true，字符串常量池复用
        System.out.println("s1 == s3 : " + (s1 == s3));          // false，new 会创建新对象
        System.out.println("s1.equals(s3) : " + s1.equals(s3));  // true，比较内容 → 永远用 equals

        // 字符串不可变，所有"修改"其实都返回新对象
        String upper = s1.toUpperCase();
        System.out.println("toUpperCase 后 s1 仍是: " + s1 + "，新对象: " + upper);

        // 常用方法
        System.out.println("length=" + s1.length() + ", charAt(1)=" + s1.charAt(1));
        System.out.println("substring(1,3)=" + s1.substring(1, 3));   // 左闭右开
        System.out.println("contains('ell')=" + s1.contains("ell"));
        System.out.println("replace=" + s1.replace("l", "L"));
        System.out.println("split: " + Arrays.toString("a,b,c".split(",")));
        System.out.println("trim: '" + "  hi  ".trim() + "'");
        System.out.println("format: " + String.format("%s 今年 %d 岁", "小明", 18));

        // 循环里拼接字符串要用 StringBuilder，否则每次都创建新对象
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i).append(i < 4 ? "-" : "");
        }
        System.out.println("StringBuilder: " + sb);

        // 判断空：isEmpty() 判长度，isBlank() 还判是否只有空白（Java 11+）
        System.out.println("\"\".isEmpty()=" + "".isEmpty() + ", \"  \".isBlank()=" + "  ".isBlank());
    }

    // ==================== 7. 包装类与装箱 ====================

    private static void wrapperClasses() {
        section("7. 包装类与装箱/拆箱");

        // 基本类型 → 包装类：自动装箱
        Integer boxed = 100;
        int unboxed = boxed;                 // 自动拆箱
        System.out.println("boxed=" + boxed + ", unboxed=" + unboxed);

        Integer a = 127;
        Integer b = 127;
        Integer c = 128;
        Integer d = 128;
        // Integer 缓存了 -128~127，范围内复用同一对象，超出范围则新建
        System.out.println("127 == 127 → " + (a == b));   // true
        System.out.println("128 == 128 → " + (c == d));   // false ← 坑！
        System.out.println("用 equals 才对 → " + c.equals(d));

        // 拆箱时如果对象是 null 会抛 NullPointerException
        Integer nullValue = null;
        try {
            int crash = nullValue;
            System.out.println(crash);
        } catch (NullPointerException e) {
            System.out.println("null 拆箱抛出: NullPointerException");
        }

        // 字符串 ↔ 数字
        int parsed = Integer.parseInt("123");
        String str = String.valueOf(456);
        System.out.println("parseInt(\"123\")=" + parsed + ", String.valueOf(456)=" + str);
        System.out.println("Integer.MAX_VALUE=" + Integer.MAX_VALUE);
    }

    // ==================== 8. 练习 ====================

    private static void exercises() {
        section("8. 练习（先自己写，再看参考答案）");

        System.out.println("① 求 1~100 中所有奇数的和 → " + sumOfOdds(100));
        System.out.println("② 反转字符串 \"abcde\" → " + reverse("abcde"));
        System.out.println("③ 判断 \"上海自来水来自海上\" 是否回文 → " + isPalindrome("上海自来水来自海上"));
        System.out.println("④ 求数组 {3,1,4,1,5,9,2,6} 的最大值 → " + max(new int[]{3, 1, 4, 1, 5, 9, 2, 6}));
    }

    private static int sumOfOdds(int limit) {
        int total = 0;
        for (int i = 1; i <= limit; i += 2) {   // 直接每次加 2，比判断 %2 更快
            total += i;
        }
        return total;
    }

    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    private static boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {                   // 双指针从两端向中间靠拢
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    private static int max(int[] nums) {
        int result = nums[0];
        for (int n : nums) {
            if (n > result) {
                result = n;
            }
        }
        return result;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }
}
