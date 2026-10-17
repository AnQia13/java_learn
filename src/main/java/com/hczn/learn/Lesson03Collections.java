package com.hczn.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 第 3 课：集合与泛型。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson03Collections
 * </pre>
 *
 * <p>本课覆盖：List / Set / Map 三大接口与常用实现、HashMap 内部原理、
 * 遍历中删除元素的坑、Comparable 与 Comparator、泛型与通配符。
 */
public class Lesson03Collections {

    public static void main(String[] args) {
        listDemo();
        setDemo();
        mapDemo();
        hashMapPrinciple();
        failFastDemo();
        sortingDemo();
        genericsDemo();
        exercises();
    }

    // ==================== 1. List ====================

    private static void listDemo() {
        section("1. List：有序、可重复、有下标");

        List<String> array = new ArrayList<>();     // 底层是数组：按下标查快
        List<String> linked = new LinkedList<>();   // 底层是双向链表：头尾增删快

        linked.add("小明");
        linked.add("小红");
        linked.add("小刚");
        array.add("小明");
        array.add("小红");
        array.add("小刚");

        array.add(1, "插队的人");                    // 中间插入：ArrayList 要把后面的元素整体后移
        System.out.println("ArrayList(插队后) : " + array);
        System.out.println("LinkedList        : " + linked);

        System.out.println("get(0)=" + array.get(0) + ", indexOf(小红)=" + array.indexOf("小红")
                + ", contains(小明)=" + array.contains("小明"));

        array.set(0, "改名了");                       // 替换
        array.remove("小红");                         // 注意：remove(Object) 按元素删
        array.remove(0);                              // 注意：remove(int) 按下标删（重载！）
        System.out.println("增删改后: " + array + "，size=" + array.size() + ", isEmpty=" + array.isEmpty());

        // List<Integer> 的经典歧义：remove(1) 删的是下标 1，remove(Integer.valueOf(1)) 删的是元素 1
        List<Integer> numbers = new ArrayList<>(Arrays.asList(10, 20, 30));
        numbers.remove(1);                            // 删下标
        System.out.println("remove(1) 后 = " + numbers + "（删的是下标 1）");

        // 三种遍历方式
        List<String> names = new ArrayList<>(Arrays.asList("a", "b", "c"));
        System.out.print("for 下标: ");
        for (int i = 0; i < names.size(); i++) {
            System.out.print(names.get(i) + " ");
        }
        System.out.print("| 增强 for: ");
        for (String name : names) {
            System.out.print(name + " ");
        }
        System.out.print("| forEach+Lambda: ");
        names.forEach(name -> System.out.print(name + " "));
        System.out.println();

        // subList 返回的是「视图」而不是拷贝，改它会影响原 List
        List<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> view = nums.subList(1, 4);      // 左闭右开 → [2, 3, 4]
        view.set(0, 99);
        System.out.println("改 subList 视图后原 List = " + nums);

        // List.of 建的是不可变列表（JDK 9+）
        List<String> immutable = List.of("x", "y");
        try {
            immutable.add("z");
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of 不可修改 → " + e.getClass().getSimpleName());
        }
        // Arrays.asList 是「长度固定」：能改元素，不能增删
        List<String> fixed = Arrays.asList("x", "y");
        fixed.set(0, "X");
        try {
            fixed.add("z");
        } catch (UnsupportedOperationException e) {
            System.out.println("Arrays.asList 长度固定 → " + e.getClass().getSimpleName());
        }
    }

    // ==================== 2. Set ====================

    private static void setDemo() {
        section("2. Set：不重复");

        Set<String> hashSet = new HashSet<>();           // 无序（按哈希），最快
        Set<String> linkedSet = new LinkedHashSet<>();   // 保持插入顺序
        Set<String> treeSet = new TreeSet<>();           // 自动排序

        for (String lang : new String[]{"java", "python", "go", "java", "c"}) {
            hashSet.add(lang);
            linkedSet.add(lang);
            treeSet.add(lang);
        }
        System.out.println("HashSet       : " + hashSet);
        System.out.println("LinkedHashSet : " + linkedSet);   // 和插入顺序一致
        System.out.println("TreeSet       : " + treeSet);      // 字典序

        System.out.println("重复 add 返回 " + hashSet.add("java") + "（false 表示已存在）");

        // 去重经典用法：List → LinkedHashSet → List
        List<String> withDup = Arrays.asList("a", "b", "a", "c", "b");
        List<String> unique = new ArrayList<>(new LinkedHashSet<>(withDup));
        System.out.println("去重并保序: " + unique);

        // TreeSet 还能做范围查询，比手动遍历方便
        TreeSet<Integer> scores = new TreeSet<>(Arrays.asList(88, 95, 60, 72, 100));
        System.out.println("最高=" + scores.last() + ", 最低=" + scores.first()
                + ", >=80 的=" + scores.tailSet(80) + ", 降序=" + scores.descendingSet());
    }

    // ==================== 3. Map ====================

    private static void mapDemo() {
        section("3. Map：键值对");

        Map<String, Integer> ages = new HashMap<>();
        ages.put("小明", 18);
        ages.put("小红", 20);
        ages.put("小刚", 22);

        Integer old = ages.put("小明", 19);              // key 相同 → 覆盖，返回被覆盖的旧值
        System.out.println("put 覆盖时返回旧值: " + old + "，现在 get(小明)=" + ages.get("小明"));
        System.out.println("get(不存在的 key) = " + ages.get("路人") + "（null）");
        System.out.println("getOrDefault = " + ages.getOrDefault("路人", -1));
        System.out.println("containsKey(小红) = " + ages.containsKey("小红"));

        ages.putIfAbsent("小明", 100);                   // 已存在 → 不覆盖
        ages.computeIfAbsent("小李", key -> 30);         // 不存在 → 用函数算出值放进去
        System.out.println("putIfAbsent / computeIfAbsent 后: " + ages);

        // 遍历方式一：keySet + get（不推荐，多一次哈希查找）
        System.out.print("keySet: ");
        for (String key : ages.keySet()) {
            System.out.print(key + "=" + ages.get(key) + " ");
        }
        // 遍历方式二：entrySet（推荐，一次拿到 key 和 value）
        System.out.print("| entrySet: ");
        for (Map.Entry<String, Integer> entry : ages.entrySet()) {
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        // 遍历方式三：forEach + Lambda（最简洁）
        System.out.print("| forEach: ");
        ages.forEach((key, value) -> System.out.print(key + "=" + value + " "));
        System.out.println();

        System.out.println("keySet=" + ages.keySet() + ", values=" + ages.values());

        int total = 0;
        for (int value : ages.values()) {
            total += value;
        }
        System.out.println("年龄总和 = " + total);

        // 统计计数的三种写法：merge 最简洁
        Map<String, Integer> count = new HashMap<>();
        for (String word : new String[]{"a", "b", "a", "a", "b"}) {
            count.merge(word, 1, Integer::sum);          // 不存在放 1，存在则 +1
        }
        System.out.println("merge 计数结果 = " + count);

        // LinkedHashMap 记住插入顺序，TreeMap 按 key 排序
        Map<String, Integer> ordered = new LinkedHashMap<>(ages);
        Map<String, Integer> sorted = new TreeMap<>(ages);
        System.out.println("LinkedHashMap = " + ordered);
        System.out.println("TreeMap(按 key 排序) = " + sorted);
    }

    // ==================== 4. HashMap 原理 ====================

    private static void hashMapPrinciple() {
        section("4. HashMap 内部原理（面试高频）");

        System.out.println("HashMap ≈ 数组 + 链表 + 红黑树");
        System.out.println("① 算 hash ：key.hashCode() 再做高位扰动，让分布更均匀，减少碰撞");
        System.out.println("② 定位桶 ：hash & (数组长度 - 1)，等价于 hash % 长度，但位运算更快");
        System.out.println("③ 放元素 ：桶为空 → 直接放；有元素 → 形成链表挂在桶上");
        System.out.println("④ 树化   ：链表长度 > 8 且数组长度 >= 64 → 转红黑树，查询从 O(n) 降到 O(log n)");
        System.out.println("⑤ 扩容   ：元素数 > 容量 × 0.75（负载因子）→ 容量翻倍，所有元素重新分布");
        System.out.println("    所以预估元素个数时可以 new HashMap<>(预计数量 / 0.75 + 1) 来避免反复扩容");
        System.out.println();

        // 用「可变对象」当 key 的坑：参与 hashCode 的字段被改了，就再也找不回来了
        Map<MutableKey, String> map = new HashMap<>();
        MutableKey key = new MutableKey(1, "原始名字");
        map.put(key, "我是值");

        System.out.println("修改 key 前 get = " + map.get(key));
        key.setName("改了名字");                         // hashCode 变了 → 哈希到别的桶
        System.out.println("修改 key 后 get = " + map.get(key) + "   ← 找不到了！");
        System.out.println("但它还躺在 map 里: " + map.keySet() + "（keySet 遍历所有桶，所以还能看到）");
        System.out.println("结论：HashMap 的 key 一定要用不可变对象（String、Integer、enum 等）");
    }

    // ==================== 5. 遍历中删除 ====================

    private static void failFastDemo() {
        section("5. 遍历中删除元素（ConcurrentModificationException）");

        // 错误写法：增强 for 里直接 list.remove()
        List<String> wrong = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        try {
            for (String s : wrong) {
                if ("b".equals(s)) {
                    wrong.remove(s);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("增强 for 里 remove → " + e.getClass().getSimpleName()
                    + "（迭代器发现 modCount 变了）");
        }
        System.out.println("异常后 list = " + wrong + "  ← 元素已删但状态不可控，千万不要这么写");

        // 正确写法一：Iterator.remove()，它会同步 expectedModCount
        List<String> byIterator = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Iterator<String> it = byIterator.iterator();
        while (it.hasNext()) {
            if ("b".equals(it.next())) {
                it.remove();
            }
        }
        System.out.println("Iterator.remove 后 = " + byIterator);

        // 正确写法二：removeIf（JDK 8+，最简洁，推荐）
        List<String> byRemoveIf = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        byRemoveIf.removeIf(s -> "b".equals(s) || "c".equals(s));
        System.out.println("removeIf 后 = " + byRemoveIf);

        // 正确写法三：倒序遍历删除，避开「删除后下标前移」的问题
        List<String> byIndex = new ArrayList<>(Arrays.asList("a", "b", "c"));
        for (int i = byIndex.size() - 1; i >= 0; i--) {
            if ("b".equals(byIndex.get(i))) {
                byIndex.remove(i);
            }
        }
        System.out.println("倒序删除后 = " + byIndex);

        // Map 同理
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 0);
        map.put("c", 3);
        map.values().removeIf(value -> value == 0);
        System.out.println("Map 删掉值为 0 的条目后 = " + map);
    }

    // ==================== 6. 排序 ====================

    private static void sortingDemo() {
        section("6. 排序：Comparable 与 Comparator");

        // Comparable：类自己实现 compareTo，定义「自然顺序」，一个类只有一套
        List<Person> people = new ArrayList<>(Arrays.asList(
                new Person("小明", 18), new Person("小红", 20), new Person("小刚", 18)));
        Collections.sort(people);
        System.out.println("用 Person.compareTo（按年龄）: " + people);

        // Comparator：从外部传规则，一个类可以有很多套，不需要改类
        people.sort(Comparator.comparing(Person::getName));
        System.out.println("按姓名: " + people);

        people.sort(Comparator.comparingInt(Person::getAge).reversed().thenComparing(Person::getName));
        System.out.println("年龄降序 + 同龄按姓名: " + people);

        // TreeMap / TreeSet 也用比较器来维护顺序
        Map<String, Integer> tree = new TreeMap<>();
        tree.put("banana", 2);
        tree.put("apple", 1);
        tree.put("cherry", 3);
        System.out.println("TreeMap key 自然顺序: " + tree);

        Map<String, Integer> byLength = new TreeMap<>(Comparator.comparingInt(String::length));
        byLength.put("aaa", 1);
        byLength.put("b", 2);
        byLength.put("cc", 3);
        System.out.println("TreeMap 按 key 长度排序: " + byLength);

        // 自定义比较器：按字符串长度再按字典序
        List<String> words = new ArrayList<>(Arrays.asList("ddd", "a", "cc", "bb"));
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("先按长度再按字典序: " + words);
    }

    // ==================== 7. 泛型 ====================

    private static void genericsDemo() {
        section("7. 泛型");

        // 泛型类：类型在编译期确定，取出来不用强制转换，也不会放错类型
        Box<String> stringBox = new Box<>("内容");
        Box<Integer> intBox = new Box<>(123);
        System.out.println(stringBox + " / " + intBox.get());

        // 泛型方法
        System.out.println("first = " + first(Arrays.asList("x", "y")));
        System.out.println("max = " + max(Arrays.asList(3, 9, 2)));

        // 通配符 PECS：Producer Extends, Consumer Super
        // 只「读」的用 ? extends Number，可以接收 List<Integer>、List<Double>
        System.out.println("sumNumbers(List<Integer>) = " + sumNumbers(Arrays.asList(1, 2, 3)));
        System.out.println("sumNumbers(List<Double>)  = " + sumNumbers(Arrays.asList(1.5, 2.5)));

        // 只「写」的用 ? super Integer，可以接收 List<Integer>、List<Number>、List<Object>
        List<Number> sink = new ArrayList<>();
        addNumber(sink, 1);
        addNumber(sink, 2.5);
        System.out.println("addNumber 写入 List<Number> = " + sink);

        // 为什么 List<Integer> 不能赋给 List<Number>（泛型不变性）
        // List<Number> bad = new ArrayList<Integer>();   // 编译错误
        System.out.println("List<Integer> 不能赋值给 List<Number>，但可以用 List<? extends Number> 来读");

        // 类型擦除：编译后泛型信息就没了，所以运行期拿不到具体类型
        List<Integer> ints = Arrays.asList(1, 2, 3);
        System.out.println("运行期 ints.getClass() = " + ints.getClass().getName() + "  ← 看不到 Integer");
    }

    // ==================== 8. 练习 ====================

    private static void exercises() {
        section("8. 练习");

        String text = "java is great java is fun go is fast";
        System.out.println("① 词频统计（按次数降序）: " + wordCount(text));
        System.out.println("② 出现 >= 2 次的单词: " + frequentWords(text, 2));
        System.out.println("③ 按单词长度分组: " + groupByLength(Arrays.asList("a", "bb", "cc", "ddd")));
    }

    /** ① 词频统计：merge 一行搞定计数，再按次数降序。 */
    private static Map<String, Integer> wordCount(String text) {
        Map<String, Integer> count = new HashMap<>();
        for (String word : text.split(" ")) {
            count.merge(word, 1, Integer::sum);
        }
        Map<String, Integer> sorted = new LinkedHashMap<>();
        count.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    /** ② 只保留出现次数 >= min 的单词，TreeMap 保证输出有序，方便看结果。 */
    private static Map<String, Integer> frequentWords(String text, int min) {
        Map<String, Integer> result = new TreeMap<>();
        wordCount(text).forEach((word, times) -> {
            if (times >= min) {
                result.put(word, times);
            }
        });
        return result;
    }

    /** ③ 按长度分组：computeIfAbsent 是「没有就建一个 List，有就直接用」的惯用写法。 */
    private static Map<Integer, List<String>> groupByLength(List<String> words) {
        Map<Integer, List<String>> groups = new TreeMap<>();
        for (String word : words) {
            groups.computeIfAbsent(word.length(), key -> new ArrayList<>()).add(word);
        }
        return groups;
    }

    // ==================== 泛型方法 ====================

    /** 泛型方法：类型参数 T 写在返回值前面。 */
    private static <T> T first(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    /** 上界：T 必须可比较，否则不能调用 compareTo。 */
    private static <T extends Comparable<T>> T max(List<T> list) {
        T result = list.get(0);
        for (T item : list) {
            if (item.compareTo(result) > 0) {
                result = item;
            }
        }
        return result;
    }

    /** ? extends Number：生产者，只读。 */
    private static double sumNumbers(List<? extends Number> list) {
        double total = 0;
        for (Number n : list) {
            total += n.doubleValue();
        }
        return total;
    }

    /** ? super Integer：消费者，只写。 */
    private static void addNumber(List<? super Integer> list, Number value) {
        list.add(value.intValue());
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 配套类型
    // ==================================================================

    /** 泛型类。 */
    static class Box<T> {
        private final T value;

        Box(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }

        @Override
        public String toString() {
            return "Box(" + value + ")";
        }
    }

    /** 实现 Comparable，定义自然顺序（按年龄）。 */
    static class Person implements Comparable<Person> {
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }

        @Override
        public int compareTo(Person other) {
            return Integer.compare(age, other.age);
        }

        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }

    /** 可变对象：用来演示「key 被改了就找不回来」的坑。 */
    static class MutableKey {
        private final int id;
        private String name;

        MutableKey(int id, String name) {
            this.id = id;
            this.name = name;
        }

        void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MutableKey that = (MutableKey) o;
            return id == that.id && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        @Override
        public String toString() {
            return "Key(" + id + ",\"" + name + "\")";
        }
    }
}
