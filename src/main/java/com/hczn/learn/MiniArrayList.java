package com.hczn.learn;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 第 11 课配套：手写一个 ArrayList，把 JDK 里看不见的东西摊开。
 *
 * <p>JDK 的 {@code java.util.ArrayList} 就是这个结构，差别只在工程细节
 * （序列化、subList、批量操作、更激进的扩容策略等）。
 *
 * <p>重点看三件事：
 * <ol>
 *   <li>底层就是一个 {@code Object[]}，容量和元素个数是两个不同的概念</li>
 *   <li>扩容是「新建数组 + 复制」，代价 O(n)，所以要尽量避免频繁扩容</li>
 *   <li>{@code modCount} + 迭代器 = fail-fast：遍历时修改集合会立刻抛异常</li>
 * </ol>
 *
 * @param <E> 元素类型
 */
public class MiniArrayList<E> implements Iterable<E> {

    /** 默认容量。JDK 里也是 10，而且是「第一次 add 时才分配」，这里从简，构造时就分配。 */
    private static final int DEFAULT_CAPACITY = 10;

    /** 真正存数据的数组。泛型擦除后运行时就是 Object[]。 */
    private Object[] elements;

    /** 元素个数。注意它和 elements.length（容量）不是一回事。 */
    private int size;

    /** 结构性修改次数：add / remove 才加 1，set 不算。迭代器靠它发现「遍历中被人改了」。 */
    private transient int modCount;

    public MiniArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public MiniArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("初始容量不能为负数: " + initialCapacity);
        }
        this.elements = new Object[Math.max(initialCapacity, 1)];
    }

    // ==================== 查询 ====================

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** JDK 没有暴露容量，这里专门暴露出来，方便观察扩容过程。 */
    public int capacity() {
        return elements.length;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index);
        return (E) elements[index];
    }

    /** 随机访问 O(1)：直接算下标，不需要从头找。这是数组最大的优势。 */
    public int indexOf(Object target) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(target, elements[i])) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Object target) {
        return indexOf(target) >= 0;
    }

    // ==================== 修改 ====================

    public void add(E element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
        modCount++;
    }

    /** 在中间插入：必须把后面所有元素整体右移，O(n)。 LinkedList 这里的优势就体现出来了。 */
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
        modCount++;
    }

    @SuppressWarnings("unchecked")
    public E set(int index, E element) {
        checkIndex(index);
        E old = (E) elements[index];
        elements[index] = element;
        return old;
    }

    @SuppressWarnings("unchecked")
    public E remove(int index) {
        checkIndex(index);
        E old = (E) elements[index];

        int moved = size - index - 1;
        if (moved > 0) {
            System.arraycopy(elements, index + 1, elements, index, moved);
        }
        elements[--size] = null;      // 必须置 null：否则这个槽位还引用着对象，GC 回收不了（内存泄漏）
        modCount++;
        return old;
    }

    public boolean remove(Object target) {
        int index = indexOf(target);
        if (index < 0) {
            return false;
        }
        remove(index);
        return true;
    }

    public void clear() {
        Arrays.fill(elements, 0, size, null);
        size = 0;
        modCount++;
    }

    // ==================== 扩容 ====================

    /**
     * 保证容量够用。JDK 的扩容是 {@code oldCapacity + (oldCapacity >> 1)}，也就是 1.5 倍。
     *
     * <p>为什么不是 2 倍？1.5 倍在「空间浪费」和「扩容次数」之间折中得更好；
     * 2 倍会浪费更多内存，1.1 倍又会频繁复制。
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= elements.length) {
            return;
        }
        int newCapacity = elements.length + (elements.length >> 1);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;    // 数组还很小时（比如容量 1），1.5 倍不够，得直接顶到需求值
        }
        elements = Arrays.copyOf(elements, newCapacity);
    }

    // ==================== fail-fast 迭代器 ====================

    /**
     * 迭代器会记住创建时的 {@code modCount}，每次 next 前对比一下。
     *
     * <p>为什么要有这个？因为「边遍历边删除」是最常见的一类 bug，
     * 与其让你拿到莫名其妙的结果，不如立刻抛 {@link ConcurrentModificationException} 让你知道。
     */
    @SuppressWarnings("unchecked")   // 泛型擦除后数组里是 Object，取出时必须强转，加注解消掉编译提示
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int cursor;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public E next() {
                checkForComodification();
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                return (E) elements[cursor++];
            }

            @Override
            public void remove() {
                checkForComodification();
                MiniArrayList.this.remove(--cursor);   // 迭代器自己的删除是允许的，删完同步 modCount
                expectedModCount = modCount;
            }

            private void checkForComodification() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException(
                            "遍历过程中集合被修改了（modCount " + expectedModCount + " → " + modCount + "）");
                }
            }
        };
    }

    // ==================== 杂项 ====================

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(elements[i]);
        }
        return builder.append(']').toString();
    }
}
