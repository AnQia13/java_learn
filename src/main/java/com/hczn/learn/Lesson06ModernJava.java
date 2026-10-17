package com.hczn.learn;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 第 6 课：Java 8+ 现代写法。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson06ModernJava
 * </pre>
 *
 * <p>本课覆盖：Lambda、函数式接口、方法引用、Stream、Collectors、Optional、
 * 新日期时间 API，以及 Java 16~21 的 record / 模式匹配 / switch 表达式 / 文本块。
 *
 * <p>注意：pom 里 release = 21，所以本课可以放心用这些新语法。
 */
public class Lesson06ModernJava {

    public static void main(String[] args) {
        lambdas();
        methodReferences();
        streams();
        collectors();
        optional();
        dateTime();
        modernSyntax();
    }

    // ==================== 1. Lambda 与函数式接口 ====================

    private static void lambdas() {
        section("1. Lambda 与函数式接口");

        // 以前：匿名内部类，一大坨样板代码
        Calculator anonymous = new Calculator() {
            @Override
            public int calc(int a, int b) {
                return a + b;
            }
        };
        // 现在：Lambda。只有「函数式接口」（只有一个抽象方法）才能写成 Lambda
        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;
        Calculator bigger = (a, b) -> a > b ? a : b;

        System.out.println("匿名内部类   = " + anonymous.calc(3, 4));
        System.out.println("Lambda 加    = " + add.calc(3, 4));
        System.out.println("Lambda 乘    = " + multiply.calc(3, 4));
        System.out.println("Lambda 取大  = " + bigger.calc(3, 4));

        // 标准库自带的四大函数式接口，够用 90% 的场景
        Predicate<Integer> isAdult = age -> age >= 18;              // 输入 → boolean
        Function<String, Integer> length = String::length;          // 输入 → 输出
        Consumer<String> print = System.out::println;               // 输入 → 无输出（消费）
        Supplier<String> supplier = () -> "我生产一个值";            // 无输入 → 有输出
        BiFunction<Integer, Integer, Integer> plus = Integer::sum;  // 两个输入 → 一个输出

        System.out.println("Predicate.test(20)      = " + isAdult.test(20));
        System.out.println("Function.apply(\"hello\") = " + length.apply("hello"));
        System.out.print("Consumer.accept         → ");
        print.accept("打印了一行");
        System.out.println("Supplier.get()          = " + supplier.get());
        System.out.println("BiFunction.apply(3, 4)  = " + plus.apply(3, 4));

        // 接口可以组合，这是函数式编程的基本操作
        Predicate<Integer> isChild = isAdult.negate();
        Predicate<Integer> teen = ((Predicate<Integer>) age -> age >= 13).and(age -> age <= 19);
        System.out.println("isAdult.negate().test(10) = " + isChild.test(10));
        System.out.println("13~19 岁?  test(15) = " + teen.test(15) + ", test(25) = " + teen.test(25));

        // Function 可以把多个步骤串起来
        Function<String, String> pipeline = ((Function<String, String>) String::trim)
                .andThen(String::toUpperCase)
                .andThen(s -> s + "!");
        System.out.println("andThen 串联 = " + pipeline.apply("  hello  "));
    }

    // ==================== 2. 方法引用 ====================

    private static void methodReferences() {
        section("2. 方法引用（Lambda 的简写）");

        // ① 静态方法引用：类名::静态方法
        Supplier<LocalDate> today = LocalDate::now;
        System.out.println("LocalDate::now           → " + today.get());

        // ② 实例方法引用（用参数当调用者）：类名::实例方法
        Function<String, Integer> length = String::length;
        System.out.println("String::length           → " + length.apply("hello"));

        // ③ 实例方法引用（用某个已存在的对象）
        Consumer<String> print = System.out::println;
        System.out.print("System.out::println      → ");
        print.accept("输出到我这里");

        // ④ 构造器引用：类名::new
        Function<Integer, int[]> arrayCreator = int[]::new;
        Function<String, StringBuilder> builderCreator = StringBuilder::new;
        System.out.println("int[]::new(5) 的长度     = " + arrayCreator.apply(5).length);
        System.out.println("StringBuilder::new       → " + builderCreator.apply("abc").reverse());

        // 什么时候能用方法引用：Lambda 体只是「调用一个已有方法」，且参数能直接对上
        List<String> names = List.of("小明", "小红");
        names.forEach(System.out::println);
    }

    // ==================== 3. Stream ====================

    private static void streams() {
        section("3. Stream：流水线式处理集合");

        List<String> words = List.of("java", "go", "python", "java", "c", "rust", "go");

        // 流水线 = 源 + 中间操作（惰性）+ 终结操作（触发执行）
        System.out.print("去重 + 长度>=3 + 转大写 + 排序 → ");
        words.stream()                                   // 源
                .distinct()                              // 中间操作
                .filter(w -> w.length() >= 3)            // 中间操作
                .map(String::toUpperCase)                // 中间操作
                .sorted()                                // 中间操作
                .forEach(w -> System.out.print(w + " "));// 终结操作：到这里才真正开始跑
        System.out.println();

        // 惰性验证：没有终结操作，peek 里的打印一次都不会发生
        System.out.print("惰性验证（下面什么都没有打印才对）: ");
        words.stream().peek(w -> System.out.print("被 peek 了 "));
        System.out.println("← 确实没执行");

        System.out.println("count()                        = " + words.stream().filter(w -> w.length() > 2).count());
        System.out.println("distinct().count()             = " + words.stream().distinct().count());
        System.out.println("anyMatch(\"java\")              = " + words.stream().anyMatch("java"::equals));
        System.out.println("findFirst(长度>=4)             = " + words.stream().filter(w -> w.length() >= 4).findFirst().orElse("-"));
        System.out.println("skipped 2 + limit 2            = " + words.stream().skip(2).limit(2).collect(Collectors.toList()));
        System.out.println("reduce 拼接                    = " + words.stream().distinct().sorted()
                .reduce((a, b) -> a + "+" + b).orElse(""));

        // 数字流：不装箱，性能更好，还自带 sum/average/max
        System.out.println("1..100 求和                    = " + IntStream.rangeClosed(1, 100).sum());
        System.out.println("1..5 平方和                    = " + IntStream.rangeClosed(1, 5).map(n -> n * n).sum());
        System.out.println("1..10 平均                     = " + IntStream.rangeClosed(1, 10).average().orElse(0));
        System.out.println("1..20 中偶数个数                = " + IntStream.rangeClosed(1, 20).filter(n -> n % 2 == 0).count());
        System.out.println("最大值                         = " + IntStream.of(3, 9, 2).max().orElse(-1));

        // flatMap：把「流里套着集合」摊平成一维
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        System.out.println("flatMap 摊平                   = "
                + nested.stream().flatMap(List::stream).collect(Collectors.toList()));

        // 无限流 + limit：斐波那契
        System.out.println("斐波那契前 10 个                = "
                + Stream.iterate(new int[]{0, 1}, t -> new int[]{t[1], t[0] + t[1]})
                        .limit(10).map(t -> t[0]).collect(Collectors.toList()));

        // 数组也能变流
        int[] array = {5, 3, 1};
        System.out.println("数组转流求和                   = " + java.util.Arrays.stream(array).sum());

        // 重要：Stream 是一次性的，用完就废
        Stream<String> oneShot = words.stream();
        oneShot.count();
        try {
            oneShot.count();
        } catch (IllegalStateException e) {
            System.out.println("Stream 重复使用 → " + e.getMessage());
        }
    }

    // ==================== 4. Collectors ====================

    private static void collectors() {
        section("4. Collectors：把流收成想要的结构");

        List<Student> students = List.of(
                new Student("小明", 18, "男"),
                new Student("小红", 20, "女"),
                new Student("小刚", 18, "男"),
                new Student("小美", 22, "女"));

        System.out.println("收集成 List        = " + students.stream().map(Student::name).collect(Collectors.toList()));
        System.out.println("收集成 Set         = " + students.stream().map(Student::gender).collect(Collectors.toSet()));
        System.out.println("joining 拼接       = " + students.stream().map(Student::name).collect(Collectors.joining("、", "【", "】")));

        // groupingBy：分组，最常用的终结操作之一
        System.out.println("按性别分组         = " + students.stream().collect(Collectors.groupingBy(Student::gender)));
        System.out.println("分组只取名字       = " + students.stream()
                .collect(Collectors.groupingBy(Student::gender, Collectors.mapping(Student::name, Collectors.toList()))));
        System.out.println("分组计数           = " + students.stream()
                .collect(Collectors.groupingBy(Student::gender, Collectors.counting())));

        // partitioningBy：按条件二分（一定有两组：true / false）
        System.out.println("按 age>=20 分区    = " + students.stream()
                .collect(Collectors.partitioningBy(s -> s.age() >= 20, Collectors.mapping(Student::name, Collectors.toList()))));

        // 数值统计
        System.out.println("平均年龄           = " + students.stream().collect(Collectors.averagingInt(Student::age)));
        IntSummaryStatistics stat = students.stream().collect(Collectors.summarizingInt(Student::age));
        System.out.println("年龄统计           = 最小 " + stat.getMin() + " / 最大 " + stat.getMax()
                + " / 平均 " + stat.getAverage() + " / 个数 " + stat.getCount());

        // toMap：key 一定要唯一，否则必须给合并函数
        Map<String, Integer> byName = students.stream()
                .collect(Collectors.toMap(Student::name, Student::age));
        System.out.println("toMap 姓名→年龄    = " + byName);

        Map<String, String> namesByGender = students.stream()
                .collect(Collectors.toMap(Student::gender, Student::name, (a, b) -> a + "/" + b));  // key 冲突时的合并规则
        System.out.println("toMap 冲突合并     = " + namesByGender);

        System.out.println("按年龄降序取名字   = " + students.stream()
                .sorted(Comparator.comparingInt(Student::age).reversed().thenComparing(Student::name))
                .map(Student::name).collect(Collectors.toList()));
    }

    // ==================== 5. Optional ====================

    private static void optional() {
        section("5. Optional：把「可能没有值」写进类型里");

        Optional<String> present = Optional.of("有值的");
        Optional<String> empty = Optional.empty();
        Optional<String> nullable = Optional.ofNullable(nullValue());

        System.out.println("present.get()                = " + present.get() + "（确认有值才用 get）");
        System.out.println("present.isPresent()          = " + present.isPresent());
        System.out.println("empty.isPresent()            = " + empty.isPresent());
        System.out.println("nullable.isPresent()         = " + nullable.isPresent() + "（ofNullable 允许 null）");
        System.out.println("orElse(默认值)               = " + empty.orElse("默认值"));
        System.out.println("orElseGet(惰性默认值)         = " + empty.orElseGet(() -> "需要时才计算"));
        System.out.println("map 链式                     = " + present.map(String::toUpperCase).map(s -> s + "!").orElse("-"));
        System.out.println("filter 过滤掉                 = " + present.filter(s -> s.length() > 10).orElse("被过滤掉了"));

        System.out.print("ifPresent 有值才执行         → ");
        present.ifPresent(s -> System.out.println("值是 " + s));

        try {
            empty.get();
        } catch (NoSuchElementException e) {
            System.out.println("空 Optional 上 get()         → " + e.getClass().getSimpleName());
        }
        try {
            empty.orElseThrow(() -> new IllegalStateException("这里必须有值"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow                  → " + e.getMessage());
        }

        try {
            Optional.of(nullValue());
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null)            → NullPointerException，应该用 ofNullable");
        }

        System.out.println("→ 原则：Optional 主要用作「返回值」表达可能为空；不要拿它当字段或参数类型，也不要用 isPresent()+get() 写成一长串");
    }

    // ==================== 6. 新日期时间 API ====================

    private static void dateTime() {
        section("6. 新日期时间 API（java.time）");

        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalTime time = LocalTime.of(14, 30, 0);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        System.out.println("LocalDate = " + date + ", LocalTime = " + time + ", LocalDateTime = " + dateTime);
        System.out.println("今天 = " + LocalDate.now() + "，此刻 = " + LocalTime.now().withNano(0));

        // 全部不可变：所有「修改」都返回新对象，原对象不动
        System.out.println("plusDays(10)      = " + date.plusDays(10) + "（原对象仍是 " + date + "）");
        System.out.println("minusMonths(1)    = " + date.minusMonths(1));
        System.out.println("withDayOfMonth(1) = " + date.withDayOfMonth(1));
        System.out.println("是否闰年 = " + date.isLeapYear() + "，当年第 " + date.getDayOfYear() + " 天，星期 "
                + date.getDayOfWeek());

        // 日期比较用 isBefore / isAfter，不要用 == 和 equals 混着来
        System.out.println("isBefore(今天)?   = " + date.isBefore(LocalDate.now()));

        // 计算间隔
        LocalDate other = LocalDate.of(2024, 4, 25);
        Period period = Period.between(date, other);
        System.out.println("相差 " + period.getYears() + " 年 " + period.getMonths() + " 月 " + period.getDays() + " 天");
        System.out.println("相差天数 = " + ChronoUnit.DAYS.between(date, other)
                + "，相差月数 = " + ChronoUnit.MONTHS.between(date, other));

        Duration duration = Duration.between(dateTime, dateTime.plusHours(3).plusMinutes(45));
        System.out.println("相差 " + duration.toMinutes() + " 分钟 / " + duration.toHours() + " 小时");

        // 格式化与解析
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter custom = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        System.out.println("ISO 格式      = " + date.format(iso));
        System.out.println("自定义格式    = " + dateTime.format(custom));
        System.out.println("解析 ISO      = " + LocalDate.parse("2024-03-15", iso));
        System.out.println("解析自定义    = " + LocalDate.parse("2024年03月15日", DateTimeFormatter.ofPattern("yyyy年MM月dd日")));

        // 时区：同一时刻，在不同时区显示不同
        ZonedDateTime shanghai = ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).withNano(0);
        ZonedDateTime newYork = shanghai.withZoneSameInstant(ZoneId.of("America/New_York"));
        DateTimeFormatter withOffset = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (XXX)");
        System.out.println("上海 = " + shanghai.format(withOffset) + "，纽约 = " + newYork.format(withOffset));

        System.out.println("→ 老 Date/Calendar 可变、非线程安全、月份从 0 开始；java.time 全部不可变、线程安全、月份从 1 开始");
    }

    // ==================== 7. Java 16~21 新语法 ====================

    private static void modernSyntax() {
        section("7. Java 16~21 新语法");

        // record（JDK 16+）：一行定义不可变数据类，自动生成构造器、访问器、equals/hashCode/toString
        Point point = new Point(3, 4);
        System.out.println("record 对象          = " + point + "，访问器 x() = " + point.x() + ", y() = " + point.y());
        System.out.println("record equals        = " + point.equals(new Point(3, 4))
                + "，hashCode 相同 = " + (point.hashCode() == new Point(3, 4).hashCode()));

        // instanceof 模式匹配（JDK 16+）：判断 + 转型一步完成，不用再强转
        Object object = "我是字符串";
        if (object instanceof String s) {
            System.out.println("instanceof 模式匹配  = " + s.toUpperCase() + "（长度 " + s.length() + "）");
        }

        // switch 表达式（JDK 14+）：-> 不需要 break，可以直接当表达式赋值
        int day = 3;
        String dayType = switch (day) {
            case 1, 2, 3, 4, 5 -> "工作日";
            case 6, 7 -> "周末";
            default -> "未知";
        };
        System.out.println("switch 表达式        = " + dayType);

        // switch 模式匹配 + record 解构（JDK 21）：sealed 保证不漏分支，编译器帮你检查
        for (Shape shape : List.of(new Circle(2.5), new Rectangle(3, 4))) {
            String description = switch (shape) {
                case Circle(double radius) -> "圆，半径 " + radius
                        + "，面积 " + String.format("%.2f", Math.PI * radius * radius);
                case Rectangle(double width, double height) -> "矩形 " + width + "x" + height + "，面积 " + (width * height);
            };
            System.out.println("switch 解构 record   = " + description);
        }
        System.out.println("→ sealed 接口 + record + switch 模式匹配，可以做到「新增一个子类，编译器就报错提醒你补分支」");

        // 文本块（JDK 15+）：多行字符串不用再拼 \n
        String json = """
                {
                  "name": "小明",
                  "age": 18
                }""";
        System.out.println("文本块:");
        System.out.println(json);

        // var（JDK 10+）：局部变量类型推断，编译期就定死类型，不是 JS 那种动态类型
        var map = Map.of("a", 1, "b", 2);
        for (var entry : map.entrySet()) {
            System.out.println("var 遍历: " + entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("→ var 只用于局部变量，能让右侧类型自解释时再考虑用，别滥用");
    }

    private static String nullValue() {
        return null;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 配套类型
    // ==================================================================

    /** 函数式接口：只有一个抽象方法，可以写 Lambda 或方法引用。 */
    @FunctionalInterface
    interface Calculator {
        int calc(int a, int b);
    }

    /** record：一行搞定不可变数据类。 */
    record Student(String name, int age, String gender) {
    }

    record Point(int x, int y) {
    }

    /** sealed：限定只允许这几个子类，配合 switch 模式匹配可以实现穷尽检查。 */
    sealed interface Shape permits Circle, Rectangle {
    }

    record Circle(double radius) implements Shape {
    }

    record Rectangle(double width, double height) implements Shape {
    }
}
