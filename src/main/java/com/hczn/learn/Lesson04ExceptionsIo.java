package com.hczn.learn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 第 4 课：异常与 IO / NIO。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson04ExceptionsIo
 * </pre>
 *
 * <p>本课覆盖：异常体系、受检 vs 非受检、try/catch/finally、try-with-resources、
 * 自定义异常与异常链、文件读写、缓冲流、NIO.2 的文件操作。
 *
 * <p>所有文件操作都在系统临时目录里进行，跑完自动清理，不会污染你的工程。
 */
public class Lesson04ExceptionsIo {

    /** 本次运行使用的临时工作目录，main 的 finally 里会删掉。 */
    private static Path workDir;

    public static void main(String[] args) throws IOException {
        try {
            exceptionHierarchy();
            tryCatchFinally();
            checkedVsUnchecked();
            customException();
            tryWithResources();
            fileBasics();
            bufferedReaderDemo();
            nioWalkDemo();
            exercises();
        } finally {
            if (workDir != null) {
                deleteRecursively(workDir);
                System.out.println();
                System.out.println("（已清理临时目录 " + workDir + "）");
            }
        }
    }

    // ==================== 1. 异常体系 ====================

    private static void exceptionHierarchy() {
        section("1. 异常体系");

        System.out.println("Throwable");
        System.out.println("├── Error                ← JVM 级严重问题（OutOfMemoryError），程序一般救不了");
        System.out.println("└── Exception            ← 程序可以处理的异常");
        System.out.println("    ├── RuntimeException 及其子类 = 非受检异常（Unchecked）");
        System.out.println("    └── 其他（IOException 等）   = 受检异常（Checked）");
        System.out.println();

        try {
            throw new IllegalStateException("我是被造出来的异常");
        } catch (Exception e) {
            System.out.println("getClass()      = " + e.getClass().getName());
            System.out.println("getMessage()    = " + e.getMessage());
            System.out.println("toString()      = " + e);
            System.out.println("getCause()      = " + e.getCause() + "（没包装其他异常时是 null）");
            StackTraceElement top = e.getStackTrace()[0];
            System.out.println("出事的代码位置   = " + top);
        }
    }

    // ==================== 2. try / catch / finally ====================

    private static void tryCatchFinally() {
        section("2. try / catch / finally");

        System.out.println("safeDivide(10, 2) = " + safeDivide(10, 2));
        System.out.println("safeDivide(10, 0) = " + safeDivide(10, 0));

        System.out.println("alwaysFinally() = " + alwaysFinally() + "  ← 注意 finally 在 return 之前执行");

        // 多异常捕获：类型用 | 连接，此时变量 e 是 final，不能再赋值
        try {
            int[] arr = new int[2];
            System.out.println(arr[5]);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            System.out.println("多异常捕获命中: " + e.getClass().getSimpleName());
        } catch (RuntimeException e) {
            System.out.println("兜底的 catch 必须写在具体类型的后面");
        }
    }

    private static String safeDivide(int a, int b) {
        try {
            return String.valueOf(a / b);
        } catch (ArithmeticException e) {           // 整数除零抛 ArithmeticException
            return "除零错误（" + e.getMessage() + "）";
        }
    }

    private static String alwaysFinally() {
        try {
            return "try 的返回值";
        } finally {
            System.out.println("  → finally 一定执行，即使上面已经 return");
            // 千万不要在 finally 里写 return，它会「吃掉」try 的返回值和异常
        }
    }

    // ==================== 3. 受检 vs 非受检 ====================

    private static void checkedVsUnchecked() {
        section("3. 受检异常 vs 非受检异常");

        // 受检异常：编译器强制你 try-catch 或者往上 throws
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();      // 规范做法：恢复中断标志，别吞掉
            System.out.println("被中断");
        }

        // 非受检异常：编译器不管，运行时才可能抛
        System.out.println("NullPointerException 是 RuntimeException 的子类吗？ "
                + RuntimeException.class.isAssignableFrom(NullPointerException.class));

        // 常见的非受检异常家族
        try {
            String s = null;
            s.length();
        } catch (NullPointerException e) {
            System.out.println("空指针: " + e.getClass().getSimpleName());
        }
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            System.out.println("数字格式错误: " + e.getClass().getSimpleName() + " → " + e.getMessage());
        }
        try {
            int[] a = new int[1];
            a[10] = 1;
        } catch (IndexOutOfBoundsException e) {
            System.out.println("下标越界: " + e.getClass().getSimpleName());
        }

        System.out.println("经验：能补救的用异常往上抛，不能补救的程序 bug 让它尽快暴露（别到处 catch 后什么都不做）");
    }

    // ==================== 4. 自定义异常与异常链 ====================

    private static void customException() {
        section("4. 自定义异常与异常链");

        Account account = new Account(100);

        try {
            account.withdraw(500);
        } catch (InsufficientBalanceException e) {       // 受检异常，必须处理
            System.out.println("业务异常: " + e.getMessage() + "，当前余额 " + account.getBalance());
        }

        try {
            account.deposit(-10);
        } catch (InvalidAmountException e) {             // 非受检异常
            System.out.println("参数异常: " + e.getMessage());
        }

        try {
            loadConfig("/etc/app/config.yml");
        } catch (InvalidAmountException e) {
            System.out.println("包装后的异常: " + e.getMessage());
            // 异常链：把底层原因带上来，排查问题时能一路看到根因
            System.out.println("  根因(cause): " + e.getCause());
        }
    }

    /** 从底层异常「包装」成业务异常，注意要把 cause 传进去。 */
    private static void loadConfig(String path) {
        try {
            throw new IOException("配置文件不存在");
        } catch (IOException e) {
            throw new InvalidAmountException("读取配置失败: " + path, e);   // ✅ 保留原因
            // ❌ 反例：throw new InvalidAmountException("读取配置失败"); ← 原因丢失，排查困难
        }
    }

    // ==================== 5. try-with-resources ====================

    private static void tryWithResources() {
        section("5. try-with-resources：自动关资源");

        // 只要实现了 AutoCloseable，写在 try(...) 里就会自动 close
        try (Res res1 = new Res("资源 A");
             Res res2 = new Res("资源 B")) {
            res1.use();
            res2.use();
        } catch (IOException e) {
            System.out.println("出错了: " + e.getMessage());
        }
        System.out.println("→ 离开 try 块后，close() 被自动调用，而且顺序是「后进先出」");
        System.out.println("→ 即使 try 里抛异常，close() 也一定会执行，比手写 finally 更不容易出错");
    }

    // ==================== 6. 文件读写基础 ====================

    private static void fileBasics() throws IOException {
        section("6. 文件读写（NIO.2 的 Files 工具类）");

        Path dir = ensureWorkDir();
        Path file = dir.resolve("note.txt");

        // 写文件：Files.write 会自动创建文件（目录必须先存在）
        Files.write(file, "第一行\n第二行\n".getBytes(StandardCharsets.UTF_8));
        System.out.println("写入完成: " + file.getFileName());

        // 读文件：按行读（最常用）
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        System.out.println("readAllLines = " + lines);

        // 读整个文件为一个字符串（JDK 11+）
        System.out.println("readString = " + Files.readString(file).trim().replace("\n", " | "));

        // 追加内容
        Files.write(file, "第三行\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        System.out.println("追加后共 " + Files.readAllLines(file).size() + " 行");

        // 文件信息
        System.out.println("exists=" + Files.exists(file) + ", size=" + Files.size(file) + " 字节"
                + ", isDirectory=" + Files.isDirectory(file));

        // 复制 / 移动
        Path copy = dir.resolve("note-copy.txt");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("复制成功: " + Files.exists(copy));

        Path moved = dir.resolve("note-moved.txt");
        Files.move(copy, moved, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("移动后原名还在吗: " + Files.exists(copy) + "，新名字在吗: " + Files.exists(moved));

        // 创建多级目录（父目录不存在也能一次建好）
        Path nested = dir.resolve("a/b/c");
        Files.createDirectories(nested);
        System.out.println("多级目录创建成功: " + Files.isDirectory(nested));

        System.out.println("→ 小文件用 readString / readAllLines 最省事，大文件才需要流式读取");
    }

    // ==================== 7. 缓冲流 ====================

    private static void bufferedReaderDemo() throws IOException {
        section("7. 缓冲流：一行一行读，适合大文件");

        Path file = ensureWorkDir().resolve("note.txt");

        // 字符流 + 缓冲：Files.newBufferedReader 内部帮你套好了 InputStreamReader
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int no = 1;
            while ((line = reader.readLine()) != null) {      // 读到文件末尾返回 null
                System.out.println("第 " + no++ + " 行: " + line);
            }
        }

        // 写出去也一样，BufferedWriter 攒够一批再真正写盘，比一行一次快很多
        Path out = ensureWorkDir().resolve("buffered.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= 3; i++) {
                writer.write("第 " + i + " 条记录");
                writer.newLine();                            // 跨平台换行
            }
        }
        System.out.println("BufferedWriter 写出 " + Files.readAllLines(out).size() + " 行");
    }

    // ==================== 8. NIO.2 遍历目录 ====================

    private static void nioWalkDemo() throws IOException {
        section("8. Files.walk / Files.list 遍历目录");

        Path dir = ensureWorkDir();

        // 只看一层
        System.out.print("一层: ");
        try (Stream<Path> list = Files.list(dir)) {              // Stream 也要关闭！
            list.map(p -> p.getFileName().toString()).sorted().forEach(name -> System.out.print(name + " "));
        }
        System.out.println();

        // 递归遍历所有层级
        List<String> relative = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(Files::isRegularFile)                    // 只要普通文件
                    .map(p -> dir.relativize(p).toString())       // 转成相对路径，输出好看
                    .sorted()
                    .forEach(relative::add);
        }
        System.out.println("递归找到 " + relative.size() + " 个文件:");
        relative.forEach(path -> System.out.println("  " + path));
        System.out.println("→ 注意：Files.list / walk 返回的 Stream 必须用 try-with-resources 关掉，否则会泄漏文件句柄");
    }

    // ==================== 9. 练习 ====================

    private static void exercises() throws IOException {
        section("9. 练习");

        Path file = ensureWorkDir().resolve("students.txt");
        Files.write(file, "小明,18\n小红,20\n小刚,22\n".getBytes(StandardCharsets.UTF_8));

        System.out.println("① 解析 CSV 并求平均年龄 = " + averageAge(file));
        System.out.println("② 找出年龄最大的人 = " + oldestStudent(file));
        System.out.println("③ 统计文件行数 = " + countLines(file));
    }

    /** ① 解析每行「姓名,年龄」求平均。 */
    private static double averageAge(Path file) throws IOException {
        int sum = 0;
        int count = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {                       // 跳过空行（isBlank 是 JDK 11+）
                continue;
            }
            String[] parts = line.split(",");
            sum += Integer.parseInt(parts[1]);
            count++;
        }
        return count == 0 ? 0 : (double) sum / count;
    }

    /** ② 找年龄最大的那一行。 */
    private static String oldestStudent(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .max(Comparator.comparingInt(line -> Integer.parseInt(line.split(",")[1])))
                .orElse("（没有数据）");
    }

    /** ③ 用流式读取统计行数，大文件也不会把内存撑爆。 */
    private static int countLines(Path file) throws IOException {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }

    // ==================== 工具方法 ====================

    private static Path ensureWorkDir() throws IOException {
        if (workDir == null) {
            workDir = Files.createTempDirectory("java-learn-io-");
            System.out.println("（临时工作目录: " + workDir + "）");
        }
        return workDir;
    }

    /** 递归删除目录：先删深层文件，再删空目录。 */
    private static void deleteRecursively(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())        // 倒序 = 先文件后目录
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.out.println("删除失败: " + path);
                        }
                    });
        } catch (IOException e) {
            System.out.println("清理临时目录失败: " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 配套类型
    // ==================================================================

    /** 模拟一个需要关闭的资源。 */
    static class Res implements AutoCloseable {
        private final String name;

        Res(String name) {
            this.name = name;
            System.out.println("  打开 " + name);
        }

        void use() throws IOException {
            System.out.println("  使用 " + name);
        }

        @Override
        public void close() {
            System.out.println("  关闭 " + name);
        }
    }

    static class Account {
        private double balance;

        Account(double balance) {
            this.balance = balance;
        }

        double getBalance() {
            return balance;
        }

        /** 余额不足 → 受检异常，强制调用方处理。 */
        void withdraw(double amount) throws InsufficientBalanceException {
            if (amount > balance) {
                throw new InsufficientBalanceException("余额不足，需要 " + amount + " 但只有 " + balance);
            }
            balance -= amount;
        }

        /** 参数非法 → 非受检异常，说明是调用方的 bug。 */
        void deposit(double amount) {
            if (amount <= 0) {
                throw new InvalidAmountException("存款金额必须大于 0，实际是 " + amount);
            }
            balance += amount;
        }
    }

    /** 自定义受检异常：继承 Exception。 */
    static class InsufficientBalanceException extends Exception {
        InsufficientBalanceException(String message) {
            super(message);
        }
    }

    /** 自定义非受检异常：继承 RuntimeException，并支持携带原因。 */
    static class InvalidAmountException extends RuntimeException {
        InvalidAmountException(String message) {
            super(message);
        }

        InvalidAmountException(String message, Throwable cause) {
            super(message, cause);          // 把底层异常作为 cause 保留下来
        }
    }
}
