package com.hczn.learn.benchmark;

import com.hczn.learn.Algorithms;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 第 11 课配套：用 JMH 量「暴力 O(n²)」和「哈希表 O(n)」差多少。
 *
 * <p>运行：
 * <pre>
 *   ./scripts/benchmark.sh ".*TwoSumBenchmark.*"
 * </pre>
 *
 * <p>JMH（Java Microbenchmark Harness）是 OpenJDK 官方的微基准框架，它替我们解决了
 * 手写 {@code System.nanoTime()} 计时避不开的一堆坑：
 * <ul>
 *   <li>JIT 预热：先跑几轮热身，等代码被编译成机器码再开始测量</li>
 *   <li>死代码消除：把结果放进 Blackhole，防止 JIT 觉得「这方法白算了」直接删掉</li>
 *   <li>常量折叠 / 循环展开：参数从外部注入，不让编译器提前算完</li>
 *   <li>独立进程：每个基准 fork 一个新 JVM，互不干扰</li>
 *   <li>统计输出：平均值、标准差、置信区间，而不是一个孤零零的数字</li>
 * </ul>
 */
@State(Scope.Benchmark)                     // 所有线程共享同一份状态
@BenchmarkMode(Mode.AverageTime)            // 测「一次调用平均耗时」
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)           // 预热 2 轮，每轮 1 秒
@Measurement(iterations = 3, time = 1)      // 正式测量 3 轮，每轮 1 秒
@Fork(1)                                    // fork 1 个独立 JVM
public class TwoSumBenchmark {

    @Param({"500", "2000"})
    private int size;

    private int[] data;
    private int unreachableTarget;

    @Setup
    public void setUp() {
        Random random = new Random(42);
        data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = random.nextInt(1_000_000);     // 全是正数
        }
        // 目标值给 -1：一定无解，两种实现都必须把组合全部试一遍。
        // 这才是能公平对比的「最坏情况」——如果提前命中，暴力解可能反而更快。
        unreachableTarget = -1;
    }

    @Benchmark
    public int[] bruteForce() {
        return Algorithms.twoSumBruteForce(data, unreachableTarget);
    }

    @Benchmark
    public int[] hashTable() {
        return Algorithms.twoSum(data, unreachableTarget);
    }
}
