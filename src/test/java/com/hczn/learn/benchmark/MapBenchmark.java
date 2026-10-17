package com.hczn.learn.benchmark;

import com.hczn.learn.MiniHashMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 第 11 课配套：手写 MiniHashMap 和 JDK HashMap 的查找性能对比。
 *
 * <p>运行：
 * <pre>
 *   ./scripts/benchmark.sh ".*MapBenchmark.*"
 * </pre>
 *
 * <p>预期：两者在同一个数量级。因为我们实现了核心结构（数组 + 链表 + 扰动函数 + 扩容），
 * JDK 多出来的主要是红黑树（只在长链时才有用）、更大的方法内联机会和一些边界优化。
 *
 * <p>如果你把 MiniHashMap 的 {@code spread} 去掉、或者把 hashCode 写坏，
 * 再跑这个基准，就能亲眼看到「O(1) 变 O(n)」。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class MapBenchmark {

    @Param({"1000", "100000"})
    private int size;

    private MiniHashMap<Integer, Integer> miniMap;
    private HashMap<Integer, Integer> jdkMap;

    /**
     * 每个线程自己一份 key 游标，用一个 {@link Scope#Thread} 的状态来提供下一个要查的 key。
     *
     * <p>为什么不直接写 {@code map.get(random.nextInt())}？
     * 因为随机数生成本身的开销可能比被测的 get 还大，那就变成「在测随机数生成器」了。
     * 预先把 key 存好、按顺序轮着取，被测代码才是唯一的开销。
     */
    @State(Scope.Thread)
    public static class KeyCursor {
        private Integer[] keys;
        private int cursor;

        @Setup(Level.Trial)
        public void setUp(MapBenchmark benchmark) {
            this.keys = benchmark.keys;
        }

        Integer next() {
            Integer key = keys[cursor];
            cursor++;
            if (cursor == keys.length) {
                cursor = 0;
            }
            return key;
        }
    }

    private Integer[] keys;

    @Setup(Level.Trial)
    public void setUp() {
        Random random = new Random(42);
        keys = new Integer[size];

        // 初始容量给足（size / 0.75 + 1），避免测量过程中扩容干扰结果
        int capacityHint = (int) (size / 0.75f) + 1;
        miniMap = new MiniHashMap<>(capacityHint);
        jdkMap = new HashMap<>(capacityHint);

        for (int i = 0; i < size; i++) {
            Integer key = random.nextInt();
            keys[i] = key;
            miniMap.put(key, i);
            jdkMap.put(key, i);
        }
    }

    @Benchmark
    public Integer miniHashMapGet(KeyCursor cursor) {
        return miniMap.get(cursor.next());
    }

    @Benchmark
    public Integer jdkHashMapGet(KeyCursor cursor) {
        return jdkMap.get(cursor.next());
    }
}
