package com.hczn.learn;

import java.util.Objects;

/**
 * 第 11 课配套：手写一个 HashMap，把「数组 + 链表」的结构摊开。
 *
 * <p>JDK 8 的 {@code HashMap} 就是这个结构，多出来的东西是：
 * 红黑树（桶里链表长度超过 8 且总量 ≥ 64 时转成树）、扩容时的 lo/hi 拆分优化、
 * 以及并发场景下的各种防御。原理层面，本类和它是一回事。
 *
 * <p>四个关键点：
 * <ol>
 *   <li>容量永远是 2 的幂 → 可以用 {@code hash & (capacity - 1)} 代替取模，位运算快得多</li>
 *   <li>{@code hash} 会先做「高低位异或」扰动，否则低位相同的 key 会全挤在一个桶里</li>
 *   <li>冲突用链表解决（拉链法）；冲突太多就退化成 O(n)</li>
 *   <li>元素数超过 capacity × 0.75 就扩容成 2 倍并重新分配所有元素</li>
 * </ol>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class MiniHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;

    /**
     * 负载因子 0.75 是权衡出来的：太小 → 桶很空、浪费内存、扩容频繁；
     * 太大 → 冲突变多、链表变长、查询变慢。0.75 是「空间和时间都还不错」的经验值。
     */
    private static final float LOAD_FACTOR = 0.75f;

    /** 桶里的一个节点（链表节点）。 */
    static class Node<K, V> {
        final int hash;          // 缓存 hash 值，比较时先比 int 再比 equals，能省掉大量 equals 调用
        final K key;
        V value;
        Node<K, V> next;         // 同一个桶里的下一个节点

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;       // 扩容阈值 = capacity * LOAD_FACTOR
    private int collisionCount;  // 教学统计：插入时发现桶里已经有元素的次数

    public MiniHashMap() {
        this(DEFAULT_CAPACITY);
    }

    public MiniHashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("初始容量必须为正数: " + initialCapacity);
        }
        int capacity = roundUpToPowerOfTwo(initialCapacity);
        this.table = newTable(capacity);
        this.threshold = (int) (capacity * LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    private Node<K, V>[] newTable(int capacity) {
        return (Node<K, V>[]) new Node<?, ?>[capacity];
    }

    /** 把任意容量向上取成 2 的幂，保证能用 & 代替 %。 */
    private static int roundUpToPowerOfTwo(int value) {
        int capacity = 1;
        while (capacity < value) {
            capacity <<= 1;
        }
        return capacity;
    }

    // ==================== 核心：hash 与定位 ====================

    /**
     * 扰动函数：把高 16 位异或到低 16 位上。
     *
     * <p>为什么要这么做？因为定位只用到了 hash 的低几位（capacity - 1 是掩码）。
     * 如果一批 key 的低位都一样、只有高位不同（比如都是 16 的倍数），
     * 不扰动的话它们会全部落到同一个桶里，哈希表直接退化成链表。
     */
    private static int spread(Object key) {
        int hash = (key == null) ? 0 : key.hashCode();
        return hash ^ (hash >>> 16);
    }

    /** 位运算代替取模：只有 capacity 是 2 的幂时这个等价关系才成立。 */
    private static int indexFor(int hash, int capacity) {
        return hash & (capacity - 1);
    }

    // ==================== 增删改查 ====================

    /** 返回旧值；key 不存在则返回 null。 */
    public V put(K key, V value) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);

        if (table[index] != null) {
            collisionCount++;        // 这个桶里已经有东西了，说明发生了哈希冲突
        }

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            // 先比 hash 再比 equals：hash 不同就绝对不相等，能省掉大量昂贵的 equals
            if (node.hash == hash && Objects.equals(node.key, key)) {
                V old = node.value;
                node.value = value;
                return old;
            }
        }

        // 头插法。JDK 7 就是头插，JDK 8 改成了尾插：
        // 因为并发扩容时头插会形成环形链表，get 时就死循环了（历史上有名的 bug）。
        table[index] = new Node<>(hash, key, value, table[index]);
        size++;

        if (size > threshold) {
            resize();
        }
        return null;
    }

    public V get(K key) {
        int hash = spread(key);
        for (Node<K, V> node = table[indexFor(hash, table.length)]; node != null; node = node.next) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return node.value;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        int hash = spread(key);
        for (Node<K, V> node = table[indexFor(hash, table.length)]; node != null; node = node.next) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return true;
            }
        }
        return false;
    }

    public V remove(K key) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);
        Node<K, V> previous = null;

        for (Node<K, V> node = table[index]; node != null; previous = node, node = node.next) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                if (previous == null) {
                    table[index] = node.next;      // 删的是桶里的头节点
                } else {
                    previous.next = node.next;     // 让前驱跳过自己
                }
                size--;
                return node.value;
            }
        }
        return null;
    }

    // ==================== 扩容 ====================

    /**
     * 扩容：容量翻倍 + 所有元素重新分配。
     *
     * <p>这是 HashMap 最贵的操作，O(n)。所以在能预估大小时，
     * 用 {@code new HashMap<>(size / 0.75 + 1)} 指定初始容量，避免反复扩容。
     *
     * <p>课里为了看清楚过程，这里是「老老实实重新算每个元素的下标」。
     * JDK 8 做了个优化：扩容后元素要么留在原位，要么刚好移动「oldCapacity」距离，
     * 所以它把一条链表拆成 lo / hi 两半，不用逐个重算。
     */
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length << 1;

        Node<K, V>[] newTable = newTable(newCapacity);
        for (Node<K, V> head : oldTable) {
            Node<K, V> node = head;
            while (node != null) {
                Node<K, V> next = node.next;                    // 先存后继，因为马上要改 node.next
                int index = indexFor(node.hash, newCapacity);
                node.next = newTable[index];                    // 挂到新桶的头部
                newTable[index] = node;
                node = next;
            }
        }

        table = newTable;
        threshold = (int) (newCapacity * LOAD_FACTOR);
    }

    // ==================== 观察用 ====================

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return table.length;
    }

    public int threshold() {
        return threshold;
    }

    public int collisionCount() {
        return collisionCount;
    }

    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
        collisionCount = 0;
    }

    /** 最长的桶内链长度。等于 1 说明基本没冲突；接近 size 说明 hashCode 写坏了。 */
    public int longestChainLength() {
        int longest = 0;
        for (Node<K, V> head : table) {
            int length = 0;
            for (Node<K, V> node = head; node != null; node = node.next) {
                length++;
            }
            if (length > longest) {
                longest = length;
            }
        }
        return longest;
    }

    /** 打印每个桶里挂了几个节点，用来直观看冲突和分布。 */
    public String describeBuckets() {
        StringBuilder builder = new StringBuilder();
        int usedBuckets = 0;

        for (int i = 0; i < table.length; i++) {
            int length = 0;
            for (Node<K, V> node = table[i]; node != null; node = node.next) {
                length++;
            }
            if (length > 0) {
                builder.append("    桶[").append(i).append("] → ").append(length).append(" 个\n");
                usedBuckets++;
            }
        }

        return String.format("容量=%d, 元素=%d, 用到的桶=%d/%d, 最长链=%d%n%s",
                table.length, size, usedBuckets, table.length, longestChainLength(), builder);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Node<K, V> head : table) {
            for (Node<K, V> node = head; node != null; node = node.next) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(node.key).append('=').append(node.value);
                first = false;
            }
        }
        return builder.append('}').toString();
    }
}
