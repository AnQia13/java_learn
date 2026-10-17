package com.hczn.learn;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 第 11 课：手写 ArrayList 和 HashMap，把底层摊开看。
 *
 * <p>运行：
 * <pre>
 *   ./scripts/run.sh com.hczn.learn.Lesson11Collections
 * </pre>
 *
 * <p>这一课是第 3 课（集合）和第 8 课（哈希表）的延伸：
 * 前面是「会用」，这里回答「为什么快、什么时候慢、什么时候会出 bug」。
 *
 * <p>配套实现：{@link MiniArrayList}、{@link MiniHashMap}
 */
public class Lesson11Collections {

    public static void main(String[] args) {
        why();
        arrayListGrowth();
        arrayListCost();
        failFast();
        hashMapBasics();
        hashCollision();
        hashMapResize();
        goodHashCode();
        comparison();
        summary();
    }

    // ==================== 0. 为什么 ====================

    private static void why() {
        section("0. 为什么值得看底层");
        System.out.println("面试必问、线上必踩，就这三件事：");
        System.out.println("  1. 为什么 ArrayList 随机访问快、中间插入慢？→ 数组的内存是连续的，插入要整体搬动");
        System.out.println("  2. 为什么 HashMap 平均 O(1)、但最坏 O(n)？→ 冲突时退化成链表");
        System.out.println("  3. 为什么「遍历中删除」会抛 ConcurrentModificationException？→ fail-fast 机制");
        System.out.println();
        System.out.println("下面每一节都用自己手写的 MiniArrayList / MiniHashMap 跑出来看，");
        System.out.println("结论和 JDK 完全一致。");
    }

    // ==================== 1. ArrayList 扩容 ====================

    private static void arrayListGrowth() {
        section("1. MiniArrayList：容量和元素个数是两个概念");

        MiniArrayList<Integer> list = new MiniArrayList<>();
        System.out.println("刚创建：size=" + list.size() + ", capacity=" + list.capacity());

        for (int i = 1; i <= 16; i++) {
            int before = list.capacity();
            list.add(i);
            if (list.capacity() != before) {
                System.out.println("  添加第 " + i + " 个元素时扩容：capacity " + before
                        + " → " + list.capacity() + "（1.5 倍，不够就直接顶到需求值）");
            }
        }

        System.out.println();
        System.out.println("最终 size=" + list.size() + ", capacity=" + list.capacity() + "，内容 " + list);
        System.out.println("→ 扩容 = 新建数组 + 复制旧数据，所以「能预估大小时就传初始容量」：");
        System.out.println("   new ArrayList<>(10000) 比默认容量一路扩容少复制十几次");
    }

    // ==================== 2. 随机访问 vs 中间插入 ====================

    private static void arrayListCost() {
        section("2. 下标访问 O(1)，中间插入 O(n)");

        int n = 20_000;

        // 预热，别让 JIT 编译时间混进来
        MiniArrayList<Integer> warmup = new MiniArrayList<>();
        for (int i = 0; i < 2_000; i++) {
            warmup.add(0, i);
        }

        MiniArrayList<Integer> atFront = new MiniArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            atFront.add(0, i);                 // 每次都插到最前面 → 每次都要把全部元素右移
        }
        long frontMs = (System.nanoTime() - start) / 1_000_000;

        MiniArrayList<Integer> atBack = new MiniArrayList<>(n + 1);
        start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            atBack.add(i);                     // 追加到末尾 → 直接放到 size 位置
        }
        long backMs = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += atBack.get(i);              // 下标访问：算一下地址就拿到
        }
        long getMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("插入 " + n + " 个元素：");
        System.out.printf("  每次都插到最前面 add(0, i) : %4d ms   ← O(n²)，整体搬了约 %d 次元素%n",
                frontMs, (long) n * (n - 1) / 2);
        System.out.printf("  追加到末尾 add(i)          : %4d ms   ← 均摊 O(1)%n", backMs);
        System.out.printf("  按下标遍历 get(i)          : %4d ms   ← O(n)，总和的校验值 %d%n", getMs, sum);
        System.out.println();
        System.out.println("→ 数组在内存里是连续的，所以「按下标算地址」是 O(1)；");
        System.out.println("  但「往中间塞」必须把后面全部右移，最坏 O(n)。");
        System.out.println("→ LinkedList 头插是 O(1)，可它按下标找元素又要 O(n)，天下没有免费的午餐。");
    }

    // ==================== 3. fail-fast ====================

    private static void failFast() {
        section("3. fail-fast：为什么遍历时不能直接删");

        MiniArrayList<String> names = new MiniArrayList<>();
        names.add("alice");
        names.add("bob");
        names.add("carol");

        try {
            for (String name : names) {
                if (name.equals("alice")) {         // 删「第一个」
                    names.remove("alice");          // 错！集合被改了，迭代器不知道
                }
            }
            System.out.println("居然没抛异常？（不应该）");
        } catch (ConcurrentModificationException e) {
            System.out.println("  抛异常了：" + e.getMessage());
            System.out.println("  （单线程也会抛，「Concurrent」这个词有点误导人）");
        }

        // 更坑的是：换个位置删就不抛了 —— 代码带着隐性 bug 上线了
        MiniArrayList<String> trap = new MiniArrayList<>();
        trap.add("alice");
        trap.add("bob");
        trap.add("carol");
        for (String name : trap) {
            if (name.equals("bob")) {               // 删「倒数第二个」
                trap.remove("bob");
            }
        }
        System.out.println();
        System.out.println("但删倒数第二个元素时，居然什么异常都没有，结果是 " + trap);
        System.out.println("  → 删完 size 变小了，hasNext() 先返回 false，压根没走到检查那一步。");
        System.out.println("  → 所以「没抛异常」绝不等于「写对了」，这类 bug 最容易漏测。");

        // 正确姿势：用迭代器自己的 remove
        MiniArrayList<String> fixed = new MiniArrayList<>();
        fixed.add("alice");
        fixed.add("bob");
        fixed.add("carol");

        Iterator<String> iterator = fixed.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals("bob")) {
                iterator.remove();                  // 迭代器删完会同步 modCount，所以不抛
            }
        }
        System.out.println();
        System.out.println("正确做法（迭代器 remove）之后的集合：" + fixed);
        System.out.println();
        System.out.println("原理：集合里有个 modCount，每次结构改动 +1；");
        System.out.println("      迭代器创建时记住这个值，每次 next 前比一下，不一致就抛。");
        System.out.println("      JDK 里推荐用 list.removeIf(...)，比你手写循环更不容易错。");
        System.out.println("      ⚠ fail-fast 只是「尽早报错」，它不保证并发安全，别当成同步手段。");
    }

    // ==================== 4. HashMap 基本操作 ====================

    private static void hashMapBasics() {
        section("4. MiniHashMap：数组 + 链表");

        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        System.out.println("初始 " + String.format("容量=%d, 阈值=%d", map.capacity(), map.threshold()));

        Map<String, Integer> scores = Map.of("alice", 90, "bob", 85, "carol", 95, "dave", 88);
        scores.forEach(map::put);
        System.out.println("放入 4 个元素后：" + map);
        System.out.println();
        System.out.println("桶分布（索引 = hash & (容量-1)）：");
        System.out.print(map.describeBuckets());
        System.out.println();
        System.out.println("查 bob = " + map.get("bob") + "（理想情况下只看一个桶，O(1)）");
        System.out.println("改 bob = " + map.put("bob", 100) + " 是旧值，现在 = " + map.get("bob"));
        System.out.println("删 carol = " + map.remove("carol") + "，size = " + map.size());
        System.out.println("查不存在的 = " + map.get("nobody") + "（注意：null 既表示「没有」也表示「值是 null」）");

        // 放一个 null key
        map.put(null, -1);
        System.out.println("放一个 null key：" + map);
        System.out.println("  → hash 取 0，照样能定位。JDK HashMap 允许一个 null key，Hashtable 不允许。");
    }

    // ==================== 5. 哈希冲突 ====================

    private static void hashCollision() {
        section("5. 冲突：坏 hashCode 能把哈希表变成链表");

        MiniHashMap<BadKey, Integer> bad = new MiniHashMap<>();
        for (int i = 1; i <= 8; i++) {
            bad.put(new BadKey("k" + i), i);
        }
        System.out.println("用 hashCode() 永远返回 1 的 BadKey 塞 8 个元素：");
        System.out.print(bad.describeBuckets());
        System.out.println("  插入时发生冲突次数 = " + bad.collisionCount());
        System.out.println("  → 8 个元素全挤在一个桶里，get 要沿着链表一个个比，退化成了 O(n)");
        System.out.println();

        MiniHashMap<GoodKey, Integer> good = new MiniHashMap<>();
        for (int i = 1; i <= 8; i++) {
            good.put(new GoodKey("k" + i), i);
        }
        System.out.println("换成正常 hashCode 的 GoodKey：");
        System.out.print(good.describeBuckets());
        System.out.println("  插入时发生冲突次数 = " + good.collisionCount());
        System.out.println("  → 只冲突了 " + good.collisionCount()
                + " 次，基本均匀散开了（8 个元素放进 16 个桶，有点冲突是正常的，叫「生日悖论」）");
        System.out.println();
        System.out.println("⚠ 这也是为什么规定「重写 equals 必须重写 hashCode」：");
        System.out.println("  equals 相等 → hashCode 必须相等，否则 put 进去就 get 不出来了。");
        System.out.println("  JDK 8 之后：桶里链表长度 > 8 且总容量 ≥ 64，会把链表转成红黑树，");
        System.out.println("  最坏查询从 O(n) 降到 O(log n) —— 这是防御「恶意 hash 攻击」的重要改动。");
    }

    // ==================== 6. 扩容 ====================

    private static void hashMapResize() {
        section("6. 扩容：容量翻倍 + 全部元素重新分配");

        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        System.out.println("初始 容量=" + map.capacity() + "，阈值=" + map.threshold()
                + "（16 × 0.75 = 12，放满 12 个就会扩）");

        for (int i = 1; i <= 13; i++) {
            int before = map.capacity();
            map.put("key" + i, i);
            if (map.capacity() != before) {
                System.out.println("  插入第 " + i + " 个时扩容：容量 " + before + " → " + map.capacity()
                        + "，新阈值 " + map.threshold() + "（扩容 O(n)，所有元素重算下标）");
            }
        }

        System.out.println();
        System.out.println("最终：容量=" + map.capacity() + "，元素=" + map.size());
        System.out.println();
        System.out.println("→ 结论：已知大概要放多少数据时，直接 `new HashMap<>(expectedSize / 0.75 + 1)`，");
        System.out.println("  一次到位，避免十几次扩容。JDK 里叫 `Maps.newHashMapWithExpectedSize`（Guava）。");
        System.out.println("→ 顺带一个坑：初始容量 1000 会被向上取成 1024（2 的幂），");
        System.out.println("  因为只有 2 的幂才能用 `hash & (capacity - 1)` 代替取模。");
    }

    // ==================== 7. 好的 hashCode ====================

    private static void goodHashCode() {
        section("7. 怎么写好的 equals / hashCode，以及可变 key 的坑");

        System.out.println("规矩：");
        System.out.println("  1. equals 相等 → hashCode 必须相等（反过来不要求）");
        System.out.println("  2. 两个方法必须基于「同一批参与比较的字段」");
        System.out.println("  3. 参与 hash 的字段不要用可变对象，或至少别在放进去之后改它");
        System.out.println("  4. 用 IDE / record / Objects.equals + Objects.hash 生成，别手写");
        System.out.println();

        // 可变 key 的经典 bug
        MiniHashMap<MutableKey, String> map = new MiniHashMap<>();
        MutableKey key = new MutableKey("alice");
        map.put(key, "这是 alice 的值");
        System.out.println("放入 key(name=alice) 后，get 能拿到吗？ " + map.get(key));

        key.setName("bob");                     // 把 key 改了
        System.out.println("把 key 的 name 从 alice 改成 bob 之后再 get：");
        System.out.println("  map.get(key) = " + map.get(key) + "    ← 找不到了！");
        System.out.println("  map.size()   = " + map.size() + "    ← 但元素还在里面，成了「永远取不出来的垃圾」");
        System.out.println();
        System.out.println("原因：hashCode 变了 → 它现在去的是另一个桶，而元素还留在老桶里。");
        System.out.println("所以：String、Integer 这些不可变类天生适合当 key，");
        System.out.println("      自定义对象当 key 时，最好设计成不可变的。");
        System.out.println("      （第 3 课讲过这个坑，这里用自己写的哈希表复现了一遍）");
    }

    // ==================== 8. 和 JDK 对比 ====================

    private static void comparison() {
        section("8. 和 JDK 的差距在哪");

        MiniArrayList<Integer> mine = new MiniArrayList<>();
        ArrayList<Integer> jdk = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mine.add(i * 10);
            jdk.add(i * 10);
        }
        System.out.println("MiniArrayList : " + mine);
        System.out.println("JDK ArrayList : " + jdk);
        System.out.println("行为一致。JDK 多出来的部分：");
        System.out.println("  · 懒分配（第一次 add 才真的 new 数组）、序列化支持");
        System.out.println("  · subList / toArray / removeIf / sort / 批量 addAll");
        System.out.println("  · 更聪明的扩容（大数组时走 ArraysSupport.newLength，避免溢出）");

        System.out.println();
        MiniHashMap<String, Integer> myMap = new MiniHashMap<>();
        HashMap<String, Integer> jdkMap = new HashMap<>();
        myMap.put("a", 1);
        myMap.put("b", 2);
        jdkMap.put("a", 1);
        jdkMap.put("b", 2);
        System.out.println("MiniHashMap  : " + myMap.toString());
        System.out.println("JDK HashMap  : " + jdkMap);
        System.out.println("结构一样（数组 + 链表）。JDK 多出来的部分：");
        System.out.println("  · 链表转红黑树（桶内 > 8 且总量 ≥ 64）");
        System.out.println("  · 扩容时 lo/hi 拆分，不用逐个重算下标");
        System.out.println("  · 序列化、keySet/values/entrySet 视图、forEach/computeIfAbsent 等一堆方法");
        System.out.println();

        System.out.println("还有两点必须记住：");
        System.out.println("  1) ArrayList 和 HashMap 都不是线程安全的。多线程读写要么加锁，");
        System.out.println("     要么用 Collections.synchronizedXxx / CopyOnWriteArrayList / ConcurrentHashMap。");
        System.out.println("  2) 判断「该用哪个」只看操作模式：查得多用 ArrayList，头尾增删多用 LinkedList/ArrayDeque；");
        System.out.println("     需要按 key 查用 HashMap，需要有序用 TreeMap，需要插入顺序用 LinkedHashMap。");
    }

    // ==================== 9. 小结 ====================

    private static void summary() {
        section("9. 小结与练习");
        System.out.println("ArrayList = 动态数组：");
        System.out.println("  随机访问 O(1)｜末尾追加均摊 O(1)｜中间增删 O(n)｜扩容 1.5 倍并复制");
        System.out.println("HashMap = 数组 + 链表（+ 红黑树）：");
        System.out.println("  平均 O(1)｜最坏 O(n)｜容量 2 的幂 + 扰动函数｜负载因子 0.75｜扩容 2 倍");
        System.out.println();
        System.out.println("练习（都在源码上改，改完跑 ./mvnw test 看有没有坏事）：");
        System.out.println("  ① MiniArrayList 支持构造时传入集合，以及写一个 toArray()。");
        System.out.println("  ② 给 MiniArrayList 加 removeIf(Predicate)，注意实现里别踩 fail-fast。");
        System.out.println("  ③ 把 MiniHashMap 的头插法改成尾插法，说说为什么 JDK 8 这么改。");
        System.out.println("  ④ 给 MiniHashMap 加一个「桶内链表超过 8 就打印警告」，观察需要多少元素才会触发。");
        System.out.println("  ⑤ 用 JMH 量一下 MiniHashMap 与 JDK HashMap 的 get 差距：");
        System.out.println("     ./scripts/benchmark.sh \".*MapBenchmark.*\"");
        System.out.println();
        System.out.println("想看「正经的微基准测试」长什么样，就打开");
        System.out.println("  src/test/java/com/hczn/learn/benchmark/  —— 那里有三个 JMH 基准类，");
        System.out.println("对比的就是第 8 课的算法和第 11 课的数据结构。");
    }

    // ==================== 用于演示的 Key 类型 ====================

    /** 故意写坏的 key：hashCode 恒定，全部落到同一个桶。 */
    static final class BadKey {
        private final String name;

        BadKey(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BadKey && ((BadKey) other).name.equals(name);
        }

        @Override
        public int hashCode() {
            return 1;                    // 就这一行，把哈希表变成了链表
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** 正常写法的 key：用 Objects.hash 生成，分布均匀。 */
    static final class GoodKey {
        private final String name;

        GoodKey(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof GoodKey && ((GoodKey) other).name.equals(name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** 可变 key：放进去之后再改字段，hashCode 就变了，元素会「丢」在表里。 */
    static final class MutableKey {
        private String name;

        MutableKey(String name) {
            this.name = name;
        }

        void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MutableKey && ((MutableKey) other).name.equals(name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }
}
