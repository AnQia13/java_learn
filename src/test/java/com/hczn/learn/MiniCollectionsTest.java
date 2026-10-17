package com.hczn.learn;

import org.junit.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * 第 11 课：手写集合的测试。
 *
 * <p>运行：{@code ./mvnw test -Dtest=MiniCollectionsTest}
 *
 * <p>手写容器的测试重点和算法题不太一样，主要盯三件事：
 * <ol>
 *   <li>边界：空集合、越界、null 元素 / null key</li>
 *   <li>结构不变式：扩容后元素不能丢、容量必须是 2 的幂、size 必须准</li>
 *   <li>和 JDK 行为一致：同样的操作，结果要和 ArrayList / HashMap 对得上</li>
 * </ol>
 */
public class MiniCollectionsTest {

    // ==================================================================
    // MiniArrayList
    // ==================================================================

    @Test
    public void addAndGetShouldKeepInsertionOrder() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("[a, b, c]", list.toString());
    }

    @Test
    public void newListShouldBeEmpty() {
        MiniArrayList<Integer> list = new MiniArrayList<>();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertEquals(10, list.capacity());     // 默认容量 10，和 JDK 一样
        assertFalse(list.iterator().hasNext());
    }

    @Test
    public void setShouldReturnOldValue() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("old");
        assertEquals("old", list.set(0, "new"));
        assertEquals("new", list.get(0));
        assertEquals(1, list.size());
    }

    @Test
    public void zeroCapacityShouldStillBeUsable() {
        // 传 0 是合法的（JDK 也允许），内部至少给 1 个槽位，add 的时候再扩容
        MiniArrayList<String> list = new MiniArrayList<>(0);
        list.add("a");
        list.add("b");
        assertEquals("[a, b]", list.toString());
    }

    @Test
    public void negativeCapacityShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MiniArrayList<>(-1));
    }

    @Test
    public void getShouldRejectOutOfBounds() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("only");

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0 + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    public void addAtIndexShouldShiftElementsRight() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("c");
        list.add(1, "b");                  // 插到中间

        assertEquals("[a, b, c]", list.toString());
        assertEquals(3, list.size());

        list.add(3, "d");                  // 追加等价于 add(E)
        assertEquals("[a, b, c, d]", list.toString());

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(99, "x"));
    }

    @Test
    public void removeByIndexShouldShiftElementsLeft() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals("b", list.remove(1));
        assertEquals("[a, c]", list.toString());
        assertEquals(2, list.size());

        assertEquals("c", list.remove(1));   // 删最后一个
        assertEquals("[a]", list.toString());
    }

    @Test
    public void removedSlotShouldBeClearedForGarbageCollection() {
        // 这是一个容易漏的细节：只把 size 减小、不把数组槽位置 null，
        // 对象就会被数组一直引用着，GC 回收不了 —— 也就是内存泄漏。
        MiniArrayList<Object> list = new MiniArrayList<>();
        Object big = new Object();
        list.add(big);
        list.remove(0);

        assertFalse(list.contains(big));
        assertEquals(0, list.size());
        assertEquals("[]", list.toString());
    }

    @Test
    public void removeByObjectShouldUseEquals() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");

        assertTrue(list.remove("a"));
        assertFalse(list.remove("a"));       // 已经没了
        assertEquals("[b]", list.toString());
    }

    @Test
    public void containsAndIndexOfShouldHandleNullElement() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add(null);

        assertEquals(1, list.indexOf(null));
        assertTrue(list.contains(null));
        assertTrue(list.remove(null));
        assertEquals(1, list.size());
    }

    @Test
    public void clearShouldResetSizeButNotCapacity() {
        MiniArrayList<Integer> list = new MiniArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(i);
        }
        int capacityBefore = list.capacity();

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertEquals(capacityBefore, list.capacity());   // clear 不缩容，这和 JDK 行为一致
    }

    @Test
    public void shouldSurviveManyResizesAndKeepEveryElement() {
        // 结构不变式：扩容了很多次之后，size 和每个元素都必须还在
        int total = 1_000;
        MiniArrayList<Integer> list = new MiniArrayList<>();   // 默认容量 10，会扩容很多次

        for (int i = 0; i < total; i++) {
            list.add(i);
        }

        assertEquals(total, list.size());
        assertTrue("容量不够说明扩容算错了", list.capacity() >= total);
        for (int i = 0; i < total; i++) {
            assertEquals(i, (int) list.get(i));
        }
    }

    @Test
    public void growthShouldMirrorJdkBehaviour() {
        // 和 JDK ArrayList 的容量增长对比：都是 1.5 倍那一套
        MiniArrayList<Integer> mine = new MiniArrayList<>();
        ArrayList<Integer> jdk = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            mine.add(i);
            jdk.add(i);
        }

        assertEquals(jdk.size(), mine.size());
        assertEquals(jdk.toString(), mine.toString());
    }

    @Test
    public void iteratorShouldVisitEveryElementInOrder() {
        MiniArrayList<Integer> list = new MiniArrayList<>();
        for (int i = 1; i <= 5; i++) {
            list.add(i * 10);
        }

        List<Integer> visited = new ArrayList<>();
        for (int value : list) {              // for-each 底层就是调 iterator()
            visited.add(value);
        }

        assertEquals(List.of(10, 20, 30, 40, 50), visited);
    }

    @Test
    public void iteratorRemoveShouldBeAllowed() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals("b")) {
                iterator.remove();            // 通过迭代器删是允许的
            }
        }

        assertEquals("[a, c]", list.toString());
    }

    @Test
    public void modifyingWhileIteratingShouldThrowConcurrentModification() {
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        // 删第一个：下一轮 next() 时检查到 modCount 变了，抛 CME
        assertThrows(ConcurrentModificationException.class, () -> {
            for (String value : list) {
                if (value.equals("a")) {
                    list.remove(0);
                }
            }
        });
    }

    @Test
    public void concurrentModificationIsNotAlwaysDetected() {
        // 反过来也要测：删「最后一个」时根本不会抛异常，循环会安静地提前结束。
        // 这说明 fail-fast 是「尽力检测」而不是「一定检测」，不能拿它当正确性保证。
        MiniArrayList<String> list = new MiniArrayList<>();
        list.add("a");
        list.add("b");

        for (String value : list) {
            if (value.equals("a")) {
                list.remove(0);
            }
        }

        assertEquals("[b]", list.toString());
    }

    // ==================================================================
    // MiniHashMap
    // ==================================================================

    @Test
    public void putAndGetShouldWork() {
        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        assertNull(map.put("alice", 90));
        assertNull(map.put("bob", 85));

        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(90), map.get("alice"));
        assertEquals(Integer.valueOf(85), map.get("bob"));
        assertNull(map.get("nobody"));
    }

    @Test
    public void putShouldReturnPreviousValueAndNotGrow() {
        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        map.put("alice", 90);

        assertEquals(Integer.valueOf(90), map.put("alice", 100));   // 返回旧值
        assertEquals(1, map.size());                                 // 没有变多
        assertEquals(Integer.valueOf(100), map.get("alice"));
    }

    @Test
    public void newMapShouldHavePowerOfTwoCapacity() {
        assertEquals(16, new MiniHashMap<String, Integer>().capacity());
        assertEquals(128, new MiniHashMap<String, Integer>(100).capacity());   // 100 → 向上取到 128
        assertEquals(1, new MiniHashMap<String, Integer>(1).capacity());
        assertThrows(IllegalArgumentException.class, () -> new MiniHashMap<String, Integer>(0));
    }

    @Test
    public void nullKeyAndNullValueShouldBeSupported() {
        MiniHashMap<String, String> map = new MiniHashMap<>();
        map.put(null, "null 的取值");

        assertEquals("null 的取值", map.get(null));
        assertTrue(map.containsKey(null));

        map.put("key", null);
        assertNull(map.get("key"));
        assertTrue(map.containsKey("key"));    // 注意：get 返回 null 不代表 key 不存在
    }

    @Test
    public void containsKeyShouldDistinguishNullValueFromMissingKey() {
        MiniHashMap<String, String> map = new MiniHashMap<>();
        map.put("present", null);

        assertTrue(map.containsKey("present"));
        assertFalse(map.containsKey("absent"));
        assertNull(map.get("present"));
        assertNull(map.get("absent"));
    }

    @Test
    public void removeShouldReturnValueAndUpdateSize() {
        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        assertEquals(Integer.valueOf(1), map.remove("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());
        assertNull(map.remove("a"));           // 删不存在的返回 null
        assertEquals(1, map.size());
    }

    @Test
    public void equalKeysMustHitTheSameEntry() {
        // equals 相等 + hashCode 相等 → 必须当成同一个 key
        MiniHashMap<Lesson11Collections.GoodKey, String> map = new MiniHashMap<>();
        map.put(new Lesson11Collections.GoodKey("alice"), "第一");
        map.put(new Lesson11Collections.GoodKey("alice"), "第二");

        assertEquals(1, map.size());
        assertEquals("第二", map.get(new Lesson11Collections.GoodKey("alice")));
    }

    @Test
    public void badHashCodeShouldStillBeCorrectJustSlow() {
        // 哈希冲突只影响性能，绝对不允许影响正确性
        MiniHashMap<Lesson11Collections.BadKey, Integer> map = new MiniHashMap<>();
        for (int i = 1; i <= 20; i++) {
            map.put(new Lesson11Collections.BadKey("k" + i), i);
        }

        assertEquals(20, map.size());
        for (int i = 1; i <= 20; i++) {
            assertEquals(Integer.valueOf(i), map.get(new Lesson11Collections.BadKey("k" + i)));
        }

        // 而且能证明它确实退化了：所有元素都挤在少数几个桶里
        assertTrue("坏 hashCode 应该产生很长的链，实际 " + map.longestChainLength(),
                map.longestChainLength() >= 10);
        assertTrue(map.collisionCount() >= 10);
    }

    @Test
    public void goodHashCodeShouldSpreadKeysOut() {
        MiniHashMap<Lesson11Collections.GoodKey, Integer> map = new MiniHashMap<>();
        for (int i = 1; i <= 20; i++) {
            map.put(new Lesson11Collections.GoodKey("k" + i), i);
        }

        assertEquals(20, map.size());
        assertTrue("正常 hashCode 不该出现长链，实际 " + map.longestChainLength(),
                map.longestChainLength() <= 3);
    }

    @Test
    public void shouldResizeAndKeepAllEntries() {
        int total = 500;
        MiniHashMap<Integer, Integer> map = new MiniHashMap<Integer, Integer>(1);   // 从 1 开始，逼它多次扩容

        for (int i = 0; i < total; i++) {
            map.put(i, i * 2);
        }

        assertEquals(total, map.size());
        assertTrue("容量必须是 2 的幂", Integer.bitCount(map.capacity()) == 1);
        assertTrue("元素数不该超过阈值", map.size() <= map.threshold());
        for (int i = 0; i < total; i++) {
            assertEquals(Integer.valueOf(i * 2), map.get(i));
        }
    }

    @Test
    public void behaviourShouldMatchJdkHashMap() {
        MiniHashMap<String, Integer> mine = new MiniHashMap<>();
        Map<String, Integer> jdk = new HashMap<>();
        List<String> keys = List.of("alice", "bob", "carol", "dave", "erin", "frank");

        for (String key : keys) {
            mine.put(key, key.length());
            jdk.put(key, key.length());
        }
        for (String key : keys) {
            assertEquals(jdk.get(key), mine.get(key));
        }

        mine.remove("alice");
        jdk.remove("alice");
        assertEquals(jdk.size(), mine.size());
        for (String key : keys) {
            assertEquals(jdk.get(key), mine.get(key));
        }
    }

    @Test
    public void clearShouldRemoveEverything() {
        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("a"));
        assertEquals(0, map.longestChainLength());
    }

    /** 自测一下「可变 key」这个坑确实存在，免得哪天真踩上去。 */
    @Test
    public void mutableKeyShouldBecomeUnreachableAfterMutation() {
        MiniHashMap<Lesson11Collections.MutableKey, String> map = new MiniHashMap<>();
        Lesson11Collections.MutableKey key = new Lesson11Collections.MutableKey("alice");
        map.put(key, "值");

        assertNotNull(map.get(key));

        key.setName("bob");                   // 改了参与 hashCode 的字段

        assertNull("key 改了之后应该找不到了", map.get(key));
        assertEquals("但元素还留在表里，成了取不出来的垃圾", 1, map.size());
    }

    /** 顺手比对一下：JDK 的 HashMap 也有同样的坑，不是我们手写实现的问题。 */
    @Test
    public void jdkHashMapHasTheSameMutableKeyTrap() {
        Map<Lesson11Collections.MutableKey, String> jdk = new HashMap<>();
        Lesson11Collections.MutableKey key = new Lesson11Collections.MutableKey("alice");
        jdk.put(key, "值");

        key.setName("bob");

        assertNull(jdk.get(key));
        assertEquals(1, jdk.size());
        assertSame(key, jdk.keySet().iterator().next());
    }

    /** 记录一下 MiniHashMap 的节点是不可变的 key：链表结构里 key 一旦放进桶就不应该再变。 */
    @Test
    public void mapToStringShouldContainAllEntries() {
        MiniHashMap<String, Integer> map = new MiniHashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        String text = map.toString();
        assertTrue(text, text.contains("a=1"));
        assertTrue(text, text.contains("b=2"));
        assertTrue(text.startsWith("{"));
        assertTrue(text.endsWith("}"));
    }
}
