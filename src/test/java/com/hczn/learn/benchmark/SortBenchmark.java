package com.hczn.learn.benchmark;

import com.hczn.learn.Algorithms;
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

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 第 11 课配套：官方 {@code Arrays.sort} 和手写快排 / 归并的性能对比。
 *
 * <p>运行：
 * <pre>
 *   ./scripts/benchmark.sh ".*SortBenchmark.*"
 * </pre>
 *
 * <p>这里还专门放了「已排好序的数据」的变体，用来量化手写快排的最坏情况：
 * 取最右元素当基准时，有序数据会让它退化成 O(n²)。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class SortBenchmark {

    @Param({"10000"})
    private int size;

    private int[] randomSource;
    private int[] sortedSource;

    @Setup(Level.Trial)
    public void setUp() {
        Random random = new Random(42);

        randomSource = new int[size];
        for (int i = 0; i < size; i++) {
            randomSource[i] = random.nextInt(1_000_000);
        }

        sortedSource = new int[size];
        for (int i = 0; i < size; i++) {
            sortedSource[i] = i;                 // 已经有序 → 手写快排的最坏情况
        }
    }

    // 注意：每个基准方法都自己 clone 一份数据再排序。
    //
    // 因为排序是「原地」修改数组的：如果几个方法复用同一个数组，
    // 第一个排完它就有序了，后面测的全变成「对有序数组排序」，结果毫无意义。
    // 而且必须让所有方法都承担同样的克隆开销，否则对比不公平。
    // （一万个 int 的克隆大约几微秒，相比毫秒级的排序可以忽略。）

    @Benchmark
    public int[] quickSortRandom() {
        int[] copy = randomSource.clone();
        Algorithms.quickSort(copy);
        return copy;
    }

    @Benchmark
    public int[] mergeSortRandom() {
        int[] copy = randomSource.clone();
        Algorithms.mergeSort(copy);
        return copy;
    }

    @Benchmark
    public int[] arraysSortRandom() {
        int[] copy = randomSource.clone();
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public int[] quickSortAlreadySortedWorstCase() {
        int[] copy = sortedSource.clone();
        Algorithms.quickSort(copy);              // 有序数据 + 取最右为基准 = O(n²)
        return copy;
    }

    @Benchmark
    public int[] arraysSortAlreadySorted() {
        int[] copy = sortedSource.clone();
        Arrays.sort(copy);
        return copy;
    }
}
