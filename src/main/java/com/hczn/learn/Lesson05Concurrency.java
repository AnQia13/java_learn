package com.hczn.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 第 5 课：多线程与并发。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson05Concurrency
 * </pre>
 *
 * <p>本课覆盖：创建线程、竞态条件、synchronized、volatile、原子类、线程池、
 * 并发集合、死锁的成因与避免。
 *
 * <p>本课所有线程都有明确终点，并且不会真的死锁（用 tryLock 超时），可以放心多跑几次。
 */
public class Lesson05Concurrency {

    public static void main(String[] args) throws Exception {
        threadBasics();
        raceCondition();
        synchronizedFix();
        volatileDemo();
        atomicDemo();
        executorDemo();
        concurrentCollections();
        deadlockExplain();
    }

    // ==================== 1. 创建线程 ====================

    private static void threadBasics() throws InterruptedException {
        section("1. 创建线程：start() 和 run() 的区别");

        // 方式一：继承 Thread（不推荐，Java 是单继承，extends 的名额被占了）
        Thread byExtends = new Thread() {
            @Override
            public void run() {
                System.out.println("  [继承 Thread  ] 运行在 " + Thread.currentThread().getName());
            }
        };
        byExtends.setName("线程-A");

        // 方式二：把自己要做的事写成 Runnable 传给 Thread（推荐，职责分离）
        Runnable task = () -> System.out.println("  [实现 Runnable] 运行在 " + Thread.currentThread().getName());
        Thread byRunnable = new Thread(task, "线程-B");

        byExtends.start();          // start() 才真正开一个新线程
        byRunnable.start();
        byExtends.join();           // 等它跑完再往下走
        byRunnable.join();

        System.out.print("  [直接调 run() ] ");
        new Thread(task, "线程-C").run();        // run() 只是普通方法调用，还在 main 线程里
        System.out.println("  ↳ 上面打印的是 main，说明 run() 没有开新线程（新手最常混淆的点）");

        System.out.println("  当前线程 = " + Thread.currentThread().getName()
                + "，CPU 核心数 = " + Runtime.getRuntime().availableProcessors());
    }

    // ==================== 2. 竞态条件 ====================

    private static void raceCondition() throws InterruptedException {
        section("2. 竞态条件：多线程一起改同一个变量");

        Counter counter = new Counter();
        int threadCount = 4;
        int perThread = 10_000;
        CountDownLatch startGate = new CountDownLatch(1);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                try {
                    startGate.await();      // 所有线程在这里等，然后一起冲，竞态更容易复现
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int j = 0; j < perThread; j++) {
                    counter.increment();
                }
            });
            threads.add(t);
            t.start();
        }

        startGate.countDown();              // 发令枪
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("期望值 = " + (threadCount * perThread));
        System.out.println("实际值 = " + counter.get() + "   ← 一般都会小于期望值，丢了更新");
        System.out.println("原因：count++ 实际是「读 → 加 1 → 写回」三步，线程可能在第 2 步之后被切换走");
    }

    // ==================== 3. synchronized ====================

    private static void synchronizedFix() throws InterruptedException {
        section("3. 修复：synchronized 与原子类");

        SyncCounter syncCounter = new SyncCounter();
        AtomicInteger atomicCounter = new AtomicInteger();
        int threadCount = 4;
        int perThread = 10_000;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    syncCounter.increment();            // 同步方法：同一时刻只允许一个线程进来
                    atomicCounter.incrementAndGet();    // 原子类：无锁 CAS 自旋
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("synchronized  = " + syncCounter.get() + "（一定准确）");
        System.out.println("AtomicInteger = " + atomicCounter.get() + "（一定准确）");
        System.out.println("区别：synchronized 会让拿不到锁的线程阻塞挂起；CAS 不阻塞，失败就重试，适合简单计数器");

        // synchronized 还能加在「代码块」上，锁粒度更细
        System.out.println("synchronized 三种用法：修饰实例方法（锁 this）、修饰静态方法（锁 Class）、修饰代码块（锁指定对象）");
    }

    // ==================== 4. volatile ====================

    private static void volatileDemo() throws InterruptedException {
        section("4. volatile：保证可见性，不保证原子性");

        FlagHolder holder = new FlagHolder();
        Thread worker = new Thread(() -> {
            long spin = 0;
            while (holder.running) {       // 没有 volatile 时，这里可能永远读到 CPU 缓存里的旧值
                spin++;
            }
            System.out.println("  工作线程收到停止信号，自旋了 " + spin + " 次");
        }, "工作线程");
        worker.setDaemon(true);            // 设成守护线程，保证即使它不停也不会卡住 JVM 退出

        worker.start();
        Thread.sleep(20);
        holder.running = false;            // volatile 写 → 立即对其他线程可见
        worker.join(2000);

        System.out.println("  可见性：volatile 保证一个线程的写立刻能被其他线程看到");
        System.out.println("  原子性：volatile 不保证！count++ 加 volatile 依然会丢更新");
        System.out.println("  适用场景：一个线程写、多个线程读的开关/状态标志");
    }

    // ==================== 5. 原子类与 CAS ====================

    private static void atomicDemo() {
        section("5. AtomicInteger 与 CAS");

        AtomicInteger value = new AtomicInteger(0);
        System.out.println("incrementAndGet() = " + value.incrementAndGet());
        System.out.println("getAndAdd(10) = " + value.getAndAdd(10) + "，之后 value = " + value.get());

        // CAS：只有当前值等于「期望值」时才改成新值
        boolean first = value.compareAndSet(11, 100);
        System.out.println("compareAndSet(11, 100) = " + first + "，现在 = " + value.get());
        boolean second = value.compareAndSet(11, 999);         // 已经是 100 了，所以失败
        System.out.println("compareAndSet(11, 999) = " + second + "，现在 = " + value.get() + "（失败不改）");

        // 手写一次 CAS 自旋：失败就重新读、重新算
        int before;
        int after;
        do {
            before = value.get();
            after = before + 1;
        } while (!value.compareAndSet(before, after));
        System.out.println("CAS 自旋 +1 后 = " + value.get());
    }

    // ==================== 6. 线程池 ====================

    private static void executorDemo() throws Exception {
        section("6. 线程池：别手动 new Thread");

        System.out.println("  为什么用线程池：线程创建/销毁成本高，且数量不可控会打满系统");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            for (int i = 1; i <= 3; i++) {
                int no = i;
                pool.execute(() -> System.out.println("  任务 " + no + " 由 "
                        + Thread.currentThread().getName() + " 执行"));
            }

            // Callable 有返回值，用 Future 取结果
            Future<Integer> future = pool.submit(() -> {
                Thread.sleep(50);
                return 6 * 7;
            });
            System.out.println("  Future.get() = " + future.get() + "（get() 会阻塞直到任务算完）");

            // 批量提交
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                int n = i;
                tasks.add(() -> n * n);
            }
            int sum = 0;
            for (Future<Integer> f : pool.invokeAll(tasks)) {
                sum += f.get();
            }
            System.out.println("  1²+2²+...+5² = " + sum);

            // CompletableFuture：异步编排，不用手动阻塞等待
            String chained = CompletableFuture.supplyAsync(() -> "hello", pool)
                    .thenApply(s -> s + " java")
                    .thenApply(String::toUpperCase)
                    .get();
            System.out.println("  CompletableFuture 链式 = " + chained);
        } finally {
            pool.shutdown();                                        // 不再接新任务，已提交的继续跑完
            boolean terminated = pool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("  线程池已优雅关闭 = " + terminated);
        }
    }

    // ==================== 7. 并发集合 ====================

    private static void concurrentCollections() throws InterruptedException {
        section("7. 并发集合：ConcurrentHashMap");

        int threadCount = 8;
        int perThread = 5_000;

        Map<String, Integer> unsafe = new HashMap<>();            // 普通 HashMap：并发写会丢更新
        Map<String, Integer> safe = new ConcurrentHashMap<>();     // 并发安全：读不加锁，写用 CAS / 桶级锁

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    // 只用一个 key，避免触发扩容，从而稳定复现「丢更新」
                    Integer current = unsafe.get("count");
                    unsafe.put("count", current == null ? 1 : current + 1);

                    safe.merge("count", 1, Integer::sum);         // CHM 的 merge 是原子操作
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        int expected = threadCount * perThread;
        System.out.println("期望 = " + expected);
        System.out.println("HashMap(get+put)        = " + unsafe.get("count") + "   ← 丢了更新");
        System.out.println("ConcurrentHashMap.merge = " + safe.get("count") + "   （正确）");

        // computeIfAbsent 在 CHM 里也是原子的，常用来做「分组缓存」
        Map<String, List<String>> groups = new ConcurrentHashMap<>();
        groups.computeIfAbsent("A", key -> new CopyOnWriteArrayList<>()).add("x");
        groups.computeIfAbsent("A", key -> new CopyOnWriteArrayList<>()).add("y");
        System.out.println("computeIfAbsent 分组 = " + groups);
        System.out.println("其他：CopyOnWriteArrayList 适合「读多写极少」，写时整体复制一份，代价高但读完全无锁");
    }

    // ==================== 8. 死锁 ====================

    private static void deadlockExplain() throws InterruptedException {
        section("8. 死锁：成因与避免");

        System.out.println("死锁需要 4 个条件同时成立：");
        System.out.println("  互斥      —— 一把锁同一时刻只能被一个线程持有");
        System.out.println("  持有并等待 —— 拿着 A 还在等 B");
        System.out.println("  不可抢占  —— 抢不到就被无限期阻塞");
        System.out.println("  循环等待  —— 线程1 等线程2 的锁，线程2 又在等线程1 的锁");
        System.out.println("打破任意一个即可。最容易打破的是「循环等待」：所有线程按同一顺序加锁");
        System.out.println();

        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();

        // 故意让两个线程以相反顺序申请锁，但用 tryLock 超时，所以只会「放弃」，不会真的死锁
        Thread t1 = new Thread(() -> transferWithTimeout(lockA, lockB), "线程-1");
        Thread t2 = new Thread(() -> transferWithTimeout(lockB, lockA), "线程-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println();
        System.out.println("→ tryLock(超时) 打破了「不可抢占」：等不到就放弃，并释放自己已经持有的锁");
        System.out.println("→ 真实项目里更推荐：统一加锁顺序（比如按对象 id 排序后再锁）");
    }

    private static void transferWithTimeout(ReentrantLock first, ReentrantLock second) {
        String name = Thread.currentThread().getName();
        try {
            if (!first.tryLock(200, TimeUnit.MILLISECONDS)) {
                System.out.println("  " + name + " 连第一把锁都没等到，直接放弃");
                return;
            }
            try {
                Thread.sleep(10);                        // 放大「持有并等待」的窗口
                if (second.tryLock(200, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("  " + name + " 成功拿到两把锁");
                    } finally {
                        second.unlock();
                    }
                } else {
                    System.out.println("  " + name + " 等第二把锁超时 → 放弃并回退（避免死锁）");
                }
            } finally {
                first.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 配套类型
    // ==================================================================

    /** 不加任何保护的计数器：演示竞态条件。 */
    static class Counter {
        private int count;

        void increment() {
            count++;                 // 读 → 加 → 写，三步不是原子的
        }

        int get() {
            return count;
        }
    }

    /** 用 synchronized 保护的计数器。 */
    static class SyncCounter {
        private int count;

        synchronized void increment() {      // 相当于给这个方法加了一把锁（锁的是 this）
            count++;
        }

        synchronized int get() {
            return count;
        }
    }

    /** volatile 标志位。 */
    static class FlagHolder {
        volatile boolean running = true;
    }
}
