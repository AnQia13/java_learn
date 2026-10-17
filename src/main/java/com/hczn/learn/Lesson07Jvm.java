package com.hczn.learn;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 第 7 课：JVM 与工程化（上）—— JVM 运行时。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson07Jvm
 * </pre>
 *
 * <p>本课覆盖：内存结构（堆/栈/方法区）、栈溢出、字符串常量池、GC 与四种引用、
 * 类加载与双亲委派、反射。
 *
 * <p>配套：{@link TextUtils} + {@code src/test/java/.../TextUtilsTest.java} 演示单元测试。
 * 命令行打包（javac / java / jar / Maven 生命周期）见 docs/java-learning-path.md。
 */
public class Lesson07Jvm {

    public static void main(String[] args) throws Exception {
        runtimeInfo();
        stackAndHeap();
        heapGrowth();
        stackOverflow();
        stringPool();
        gcAndReferences();
        classLoading();
        reflection();
        summary();
    }

    // ==================== 1. 运行期信息 ====================

    private static void runtimeInfo() {
        section("1. 运行期信息与内存参数");

        Runtime runtime = Runtime.getRuntime();
        long mb = 1024 * 1024;
        System.out.println("CPU 核心数        = " + runtime.availableProcessors());
        System.out.println("JVM 已申请总内存   = " + runtime.totalMemory() / mb + " MB  （-Xms 控制初始值）");
        System.out.println("其中空闲           = " + runtime.freeMemory() / mb + " MB");
        System.out.println("最大可申请         = " + runtime.maxMemory() / mb + " MB  （-Xmx 控制上限）");
        System.out.println("正在使用的线程数    = " + Thread.activeCount());
        System.out.println();

        System.out.println("java.version       = " + System.getProperty("java.version"));
        System.out.println("java.vm.name       = " + System.getProperty("java.vm.name"));
        System.out.println("os.name / arch     = " + System.getProperty("os.name") + " / " + System.getProperty("os.arch"));
        System.out.println("file.encoding      = " + System.getProperty("file.encoding"));
        System.out.println("user.dir           = " + System.getProperty("user.dir"));
        System.out.println("java.class.path    = " + truncate(System.getProperty("java.class.path"), 140));

        System.out.println();
        System.out.println("可以自己加参数观察变化，比如把最大堆限制成 64MB 再跑一遍：");
        System.out.println("  java -Xmx64m -cp target/classes com.hczn.learn.Lesson07Jvm");
    }

    // ==================== 2. 堆与栈 ====================

    private static void stackAndHeap() {
        section("2. 内存结构：堆、栈、方法区");

        System.out.println("┌──────────────── 每个线程独享 ────────────────┐");
        System.out.println("│ 虚拟机栈：方法调用帧、基本类型局部变量、对象引用   │");
        System.out.println("│ 本地方法栈、程序计数器                          │");
        System.out.println("└────────────────────────────────────────────┘");
        System.out.println("┌──────────────── 所有线程共享 ────────────────┐");
        System.out.println("│ 堆：new 出来的对象、数组（GC 的主战场）          │");
        System.out.println("│ 方法区 / 元空间：类的结构、静态变量、常量池       │");
        System.out.println("│   （JDK 8 起「永久代」被「元空间」取代，改用本地内存）│");
        System.out.println("└────────────────────────────────────────────┘");
        System.out.println();

        int localPrimitive = 42;                       // 值本身在栈上
        int[] array = new int[1000];                   // 数组对象在堆上，array 这个「引用」在栈上
        System.out.println("局部基本类型 localPrimitive = " + localPrimitive + " → 栈");
        System.out.println("数组长度 = " + array.length + " → 对象在堆，引用在栈");
        System.out.println();
        System.out.println("结论：方法一结束，栈帧整块弹出，栈上的东西自动没了；");
        System.out.println("      堆上的对象要等没人引用它，才可能被 GC 回收。");
    }

    // ==================== 3. 堆的增长与回收 ====================

    private static void heapGrowth() throws InterruptedException {
        section("3. 堆对象分配与 GC");

        Runtime runtime = Runtime.getRuntime();
        long mb = 1024 * 1024;

        long usedBefore = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        List<byte[]> hog = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            hog.add(new byte[100 * 1024]);             // 每个 100KB，共约 20MB
        }
        long usedAfter = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        System.out.println("分配约 20MB 的 byte[] 前，已用 " + usedBefore + " MB");
        System.out.println("分配后，已用 " + usedAfter + " MB  ← 对象都躺在堆里");

        hog.clear();                                   // 去掉强引用，对象变成「可回收」
        System.gc();                                   // 只是「建议」JVM 回收，不保证立刻执行
        Thread.sleep(100);
        long usedReclaimed = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        System.out.println("清空引用并提示 GC 后，已用 " + usedReclaimed + " MB");
        System.out.println();
        System.out.println("GC 要解决的核心问题：哪些堆对象「还活着」？");
        System.out.println("  主流做法是「可达性分析」：从 GC Roots（栈上引用、静态变量、常量…）出发能走到的对象才活着");
        System.out.println("  走不到的（比如上面被 clear 掉的那些 byte[]）就是垃圾，可以回收");
        System.out.println("  分代回收：新生代（Eden + Survivor，对象朝生夕死，用复制算法）");
        System.out.println("            老年代（活得久，用标记-整理算法），整堆兜底用标记-清除");
    }

    // ==================== 4. 栈溢出 ====================

    private static void stackOverflow() {
        section("4. 栈溢出：StackOverflowError");

        try {
            infiniteRecursion(0);
        } catch (StackOverflowError e) {
            System.out.println("无限递归 → " + e.getClass().getSimpleName());
            System.out.println("  注意它是 Error 不是 Exception，说明是 JVM 级别的严重问题，通常不该被业务代码捕获");
            System.out.println("  常见成因：递归没有终止条件，或者终止条件写错");
            System.out.println("  解决办法：加终止条件，或者改成循环/显式栈（用 -Xss 调大栈只能拖延，不能根治）");
        }
    }

    private static int infiniteRecursion(int depth) {
        return infiniteRecursion(depth + 1) + 1;       // 永远停不下来
    }

    // ==================== 5. 字符串常量池 ====================

    private static void stringPool() {
        section("5. 字符串常量池与 intern");

        String a = "hello";                    // 编译期常量 → 常量池
        String b = "hello";                    // 复用常量池中同一个对象
        String c = new String("hello");        // new 强制在堆上新建
        String d = c.intern();                 // 手动放进常量池，返回池里的引用

        System.out.println("a == b           → " + (a == b) + "  （常量池复用同一个对象）");
        System.out.println("a == c           → " + (a == c) + "  （new 一定新建）");
        System.out.println("a == d           → " + (a == d) + "  （intern 后指向池中同一个）");
        System.out.println("a.equals(c)      → " + a.equals(c) + "  ← 比较内容永远用 equals");

        String folded = "he" + "llo";          // 编译期就能算出来 → 还是常量
        String prefix = "he";
        String built = prefix + "llo";         // 运行期拼接 → 堆上新对象

        System.out.println("(\"he\"+\"llo\") == a → " + (folded == a) + "  （编译期常量折叠）");
        System.out.println("(prefix+\"llo\") == a → " + (built == a) + "  （运行期拼接是新对象）");
        System.out.println();
        System.out.println("常量池是「方法区/元空间」的一部分，专门缓存字符串字面量和其他常量");
    }

    // ==================== 6. GC 与四种引用 ====================

    private static void gcAndReferences() throws InterruptedException {
        section("6. 四种引用类型");

        Object strong = new Object();                                    // 强引用：死活不回收
        WeakReference<Object> weak = new WeakReference<>(new Object());  // 弱引用：下次 GC 就回收

        System.out.println("强引用还在: " + (strong != null));
        System.out.println("GC 前 weak.get() 有值吗 = " + (weak.get() != null));

        System.gc();
        Thread.sleep(100);

        Object collected = weak.get();
        System.out.println("GC 后 weak.get() = " + (collected == null ? "null（对象已被回收）" : collected));
        System.out.println();
        System.out.println("  强引用 Strong  ：普通赋值，只要可达就绝不回收");
        System.out.println("  软引用 Soft    ：内存不够时才回收 → 适合做缓存");
        System.out.println("  弱引用 Weak    ：下一次 GC 就回收 → WeakHashMap 的 key 用的就是它");
        System.out.println("  虚引用 Phantom ：回收时收到通知，用来做资源清理（Cleaner）");
        System.out.println("  finalize() 已被标记废弃，别再用它做清理，改用 try-with-resources 或 Cleaner");
    }

    // ==================== 7. 类加载 ====================

    private static void classLoading() {
        section("7. 类加载与双亲委派");

        ClassLoader app = Lesson07Jvm.class.getClassLoader();
        System.out.println("AppClassLoader（应用类加载器）   = " + app);
        System.out.println("它的 parent（平台类加载器）      = " + app.getParent());
        System.out.println("再往上（启动类加载器）           = " + app.getParent().getParent()
                + "  ← 它是 C++ 写的，Java 里看不到，所以返回 null");
        System.out.println();
        System.out.println("双亲委派：要加载一个类时，先拜托父加载器，父加载器加载不了才自己上");
        System.out.println("  好处：像 java.lang.String 这种核心类永远由启动类加载器加载，防止被伪造替换");

        System.out.println();
        System.out.println("类加载三阶段：加载 → 链接（验证 / 准备 / 解析）→ 初始化");
        System.out.println("---- 现在主动使用 StaticHolder，触发它的初始化 ----");
        System.out.println("StaticHolder.VALUE = " + StaticHolder.VALUE);
        System.out.println("再取一次 VALUE = " + StaticHolder.VALUE);
        System.out.println("── 只打印了一次初始化日志，说明「类初始化只执行一次」");

        System.out.println();
        System.out.println("小陷阱：如果是 static final 的「编译期常量」（比如 static final int X = 1），");
        System.out.println("        编译器会直接内联到调用处，根本不会触发类初始化。");
        System.out.println("        所以这里特意写成 static final int VALUE = init()，强制触发初始化。");
    }

    // ==================== 8. 反射 ====================

    private static void reflection() throws Exception {
        section("8. 反射：运行期操作类的结构");

        Class<?> clazz = Secret.class;
        System.out.println("类全名 = " + clazz.getName() + "，简单名 = " + clazz.getSimpleName());

        Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);                      // 连私有构造器都能用
        Secret secret = (Secret) constructor.newInstance("初始密码");
        System.out.println("反射创建对象 → " + secret);

        Method hidden = clazz.getDeclaredMethod("hidden");
        hidden.setAccessible(true);
        System.out.println("调用私有方法 → " + hidden.invoke(secret));

        Field code = clazz.getDeclaredField("code");
        code.setAccessible(true);
        code.set(secret, "被反射改掉了");
        System.out.println("修改私有字段 → " + secret);

        System.out.print("列出所有声明的方法: ");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.print(method.getName() + " ");
        }
        System.out.println();

        System.out.print("列出所有声明的字段: ");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.print(field.getType().getSimpleName() + " " + field.getName() + " ");
        }
        System.out.println();

        System.out.println();
        System.out.println("→ Spring 依赖注入、MyBatis 映射、JUnit 找到并调用 @Test，靠的都是反射");
        System.out.println("→ 但 private 在反射面前不是安全边界，而且反射比直接调用慢，业务代码别滥用");
    }

    // ==================== 9. 小结 ====================

    private static void summary() {
        section("9. 小结与下一步");

        System.out.println("这一课记住这几条就够用了：");
        System.out.println("  1. 栈是线程私有的（局部变量、引用），堆是共享的（对象），方法区存类结构");
        System.out.println("  2. 堆满了 → OutOfMemoryError；栈太深 → StackOverflowError");
        System.out.println("  3. 字符串比较永远用 equals，== 比的是引用");
        System.out.println("  4. 对象能不能被回收看「从 GC Roots 是否可达」，强/软/弱/虚决定回收时机");
        System.out.println("  5. 类由双亲委派机制加载，静态初始化只执行一次");
        System.out.println("  6. 反射能打破封装，框架的基石，但别在业务里乱用");
        System.out.println();
        System.out.println("「工程化」部分（javac / java / jar / Maven 生命周期 / 单元测试）见：");
        System.out.println("  docs/java-learning-path.md");
        System.out.println("  以及 src/test/java/com/hczn/learn/TextUtilsTest.java");
    }

    // ==================== 工具 ====================

    private static String truncate(String value, int max) {
        if (value == null) {
            return "null";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...(" + value.length() + " 字符)";
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 配套类型
    // ==================================================================

    /** 故意写成方法调用，避免被编译器当成编译期常量内联，从而能演示「类初始化」。 */
    static class StaticHolder {
        static final int VALUE = init();

        static int init() {
            System.out.println("  【StaticHolder 类初始化执行了】静态字段初始化只发生这一次");
            return 42;
        }
    }

    /** 有私有构造器、私有方法、私有字段，用来演示反射。 */
    static class Secret {
        private String code;

        Secret(String code) {
            this.code = code;
        }

        private String hidden() {
            return "私有方法被调用了";
        }

        @Override
        public String toString() {
            return "Secret{code='" + code + "'}";
        }
    }
}
