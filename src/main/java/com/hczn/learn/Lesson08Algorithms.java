package com.hczn.learn;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 第 8 课：算法与数据结构。
 *
 * <p>运行：
 * <pre>
 *   ./mvnw -q compile
 *   java -cp target/classes com.hczn.learn.Lesson08Algorithms
 * </pre>
 *
 * <p>本课覆盖：复杂度分析、数组与双指针、哈希表、链表、栈与队列、二叉树、
 * 排序与二分查找、动态规划。
 *
 * <p>所有算法的实现在 {@link Algorithms}，可以独立用 JUnit 测试。
 */
public class Lesson08Algorithms {

    private static final Random RANDOM = new Random(42);   // 固定种子，保证每次跑结果一样

    public static void main(String[] args) {
        complexityCheatSheet();
        arraysAndTwoPointers();
        hashTableSpeed();
        linkedList();
        stackAndQueue();
        binaryTree();
        sortingAndSearching();
        dynamicProgramming();
        summary();
    }

    // ==================== 0. 复杂度速查 ====================

    private static void complexityCheatSheet() {
        section("0. 复杂度速查：先想清楚，再动手写");

        System.out.println("大 O 只看「增长最快的那一项」，且忽略常数：");
        System.out.println("  O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2ⁿ) < O(n!)");
        System.out.println();
        System.out.println("n = 1000 时大致要跑多少次运算：");
        System.out.println("  O(1)        1          无所谓");
        System.out.println("  O(log n)    ≈10        二分查找");
        System.out.println("  O(n)        1,000      一次遍历、哈希表");
        System.out.println("  O(n log n)  ≈10,000    排序");
        System.out.println("  O(n²)       1,000,000  双重循环 → 已经要 10ms 级别了");
        System.out.println("  O(2ⁿ)       10^301     指数爆炸，n 只到 20 就基本跑不动");
        System.out.println();
        System.out.println("两个关键套路：");
        System.out.println("  ① 用「空间换时间」：暴力 O(n²) 的查找，用哈希表降到 O(n)");
        System.out.println("  ② 用「排序 + 双指针 / 二分」：把无序变有序，往往是 O(n log n) 的起点");
        System.out.println();
        System.out.println("面试时先说思路和复杂度，再写代码；写完再核对一下边界（空、单个、全相同、溢出）。");
    }

    // ==================== 1. 数组与双指针 ====================

    private static void arraysAndTwoPointers() {
        section("1. 数组与双指针");

        int[] sorted = {-4, -1, -1, 0, 1, 2, 5};
        System.out.println("有序数组 " + Arrays.toString(sorted) + "，找和为 0 的两个数");
        System.out.println("  双指针结果下标 " + Arrays.toString(Algorithms.twoSumSorted(sorted, 0))
                + " → " + sorted[Algorithms.twoSumSorted(sorted, 0)[0]]
                + " + " + sorted[Algorithms.twoSumSorted(sorted, 0)[1]]);

        int[] nums = {2, 7, 11, 15};
        int[] pair = Algorithms.twoSum(nums, 9);
        System.out.println("无序数组 " + Arrays.toString(nums) + " 找和为 9 → 下标 " + Arrays.toString(pair)
                + "（哈希表 O(n)，对比 TestDemo 里的暴力 O(n²)）");

        int[] withTarget = {3, 2, 2, 3, 1};
        int[] copy = withTarget.clone();
        int newLength = Algorithms.removeElement(copy, 3);
        System.out.println("原地移除 3 → 新长度 " + newLength + "，内容 "
                + Arrays.toString(Arrays.copyOf(copy, newLength)));

        int[] sub = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("最大子数组和 " + Arrays.toString(sub) + " → " + Algorithms.maxSubArray(sub)
                + "（Kadane，O(n)：要么接着前面累加，要么从自己重来）");
        System.out.println("全是负数时 → " + Algorithms.maxSubArray(new int[]{-3, -1, -7}) + "（取最大的那个，别返回 0）");

        System.out.println("三数之和为 0: " + Algorithms.threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
        System.out.println("  → 排序后固定第一个数，剩下两个用双指针夹逼，整体 O(n²)");
    }

    // ==================== 2. 哈希表：空间换时间 ====================

    private static void hashTableSpeed() {
        section("2. 哈希表：空间换时间的经典案例");

        int n = 20_000;
        int[] data = new int[n];
        for (int i = 0; i < n; i++) {
            data[i] = RANDOM.nextInt(1_000_000);        // 都是非负数
        }
        // 故意取一个不可能凑出来的目标值，这样暴力解法必须把所有组合都试一遍，
        // 才是真正的最坏情况。如果目标值能提前命中，两者的差距会小很多甚至看不出来。
        int target = -1;

        // 先各跑一次做 JIT 预热，否则第一次的时间不准
        Algorithms.twoSumBruteForce(new int[]{1, 2, 3}, 5);
        Algorithms.twoSum(new int[]{1, 2, 3}, 5);

        long start = System.nanoTime();
        Algorithms.twoSumBruteForce(data, target);
        long bruteForceMs = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        Algorithms.twoSum(data, target);
        long hashMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("n = " + n + "，同一组数据、同一个目标值：");
        System.out.printf("  暴力双重循环 O(n²)  耗时 %4d ms%n", bruteForceMs);
        System.out.printf("  哈希表一次遍历 O(n) 耗时 %4d ms%n", hashMs);
        System.out.println("  → 多花一个 Map 的内存，换掉一个数量级的耗时，这就是「空间换时间」");
        System.out.println("  ⚠ 这种计时受 JIT、GC 影响很大，只适合看数量级差距，不能当微基准测试");

        System.out.println();
        System.out.println("字母异位词分组: " + Algorithms.groupAnagrams(
                new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}));
        System.out.println("  → 关键技巧：把「排序后的串」当 key，O(n·k log k)");

        System.out.println("最长连续序列 " + Arrays.toString(new int[]{100, 4, 200, 1, 3, 2})
                + " → " + Algorithms.longestConsecutive(new int[]{100, 4, 200, 1, 3, 2})
                + "（看起来要排序，实际用 HashSet 只从起点数，O(n)）");
    }

    // ==================== 3. 链表 ====================

    private static void linkedList() {
        section("3. 链表：指针操作 + 哑节点 + 快慢指针");

        Algorithms.ListNode head = Algorithms.buildList(1, 2, 3, 4, 5);
        System.out.println("原始链表      " + Algorithms.listToString(head));
        System.out.println("反转后        " + Algorithms.listToString(Algorithms.reverse(head)));
        System.out.println("  → 迭代法三个指针 prev / current / next，空间 O(1)；关键是先存 next 再改指针");

        Algorithms.ListNode a = Algorithms.buildList(1, 3, 5, 7);
        Algorithms.ListNode b = Algorithms.buildList(2, 4, 6);
        System.out.println("合并有序链表  " + Algorithms.listToString(a) + " + " + Algorithms.listToString(b)
                + " → " + Algorithms.listToString(Algorithms.mergeTwoLists(a, b)));
        System.out.println("  → 用哑节点(dummy)把头节点特判省掉，最后 return dummy.next");

        Algorithms.ListNode odd = Algorithms.buildList(1, 2, 3, 4, 5);
        Algorithms.ListNode even = Algorithms.buildList(1, 2, 3, 4);
        System.out.println("中间节点  " + Algorithms.listToString(odd) + " → " + Algorithms.middleNode(odd).val
                + "，" + Algorithms.listToString(even) + " → " + Algorithms.middleNode(even).val
                + "（快慢指针，偶数长度时取靠后的那个）");

        Algorithms.ListNode noCycle = Algorithms.buildList(1, 2, 3);
        Algorithms.ListNode cycle = Algorithms.createCycle(Algorithms.buildList(1, 2, 3, 4), 1);
        System.out.println("是否有环  " + Algorithms.listToString(noCycle) + " → " + Algorithms.hasCycle(noCycle)
                + "，1→2→3→4 且尾部接回 2 → " + Algorithms.hasCycle(cycle)
                + "（Floyd 判圈，快指针每次走两步）");

        Algorithms.ListNode remove = Algorithms.buildList(1, 2, 3, 4, 5);
        System.out.println("删除倒数第 2 个  " + Algorithms.listToString(remove) + " → "
                + Algorithms.listToString(Algorithms.removeNthFromEnd(remove, 2))
                + "（快指针先走 n+1 步，再一次遍历搞定）");

        System.out.println();
        System.out.println("⚠ 顺带说个你原代码里的坑：TestDemo 里的 ListNode 是非静态内部类，");
        System.out.println("  必须 new TestDemo().new ListNode(1) 才能创建节点；这里写成静态嵌套类就正常了。");
    }

    // ==================== 4. 栈与队列 ====================

    private static void stackAndQueue() {
        section("4. 栈与队列");

        for (String s : new String[]{"()[]{}", "([{}])", "(]", "([)]", ""}) {
            System.out.println("括号匹配 \"" + s + "\" → " + Algorithms.isValidParentheses(s));
        }
        System.out.println("  → 栈的典型用途：最近的、需要配对的东西，就压栈");

        int[] temperatures = {73, 74, 75, 71, 69, 72, 76, 73};
        System.out.println("每日温度 " + Arrays.toString(temperatures) + " → "
                + Arrays.toString(Algorithms.dailyTemperatures(temperatures)));
        System.out.println("  → 单调栈：栈里只留还没找到答案的下标，每个元素进出各一次，O(n)");

        Algorithms.MyQueue queue = new Algorithms.MyQueue();
        queue.push(1);
        queue.push(2);
        System.out.println("两个栈实现队列: push(1) push(2) → peek=" + queue.peek() + " pop=" + queue.pop()
                + " empty=" + queue.empty() + " pop=" + queue.pop() + " empty=" + queue.empty());
        System.out.println("  → 进队压 in 栈，出队时把 in 全部倒进 out 栈，每个元素最多搬一次，均摊 O(1)");
    }

    // ==================== 5. 二叉树 ====================

    private static void binaryTree() {
        section("5. 二叉树：遍历是万题之母");

        Algorithms.TreeNode bst = Algorithms.buildBst(8, 3, 10, 1, 6, 14, 4, 7, 13);
        System.out.println("中序遍历 " + Algorithms.inorder(bst) + "  ← 二叉搜索树的中序一定是升序");
        System.out.println("前序遍历 " + Algorithms.preorder(bst));
        System.out.println("层序遍历 " + Algorithms.levelOrder(bst) + "  ← BFS，用队列一层层来");

        System.out.println("最大深度 = " + Algorithms.maxDepth(bst) + "（递归：1 + max(左, 右)）");

        // 按 1..7 顺序插入会退化成一条链
        Algorithms.TreeNode skewed = Algorithms.buildBst(1, 2, 3, 4, 5, 6, 7);
        Algorithms.TreeNode balanced = Algorithms.buildBalanced(new int[]{1, 2, 3, 4, 5, 6, 7});
        System.out.println("顺序插入 1..7 后，深度 = " + Algorithms.maxDepth(skewed) + "，平衡吗？ "
                + Algorithms.isBalanced(skewed) + "（退化成链表，查找从 O(log n) 变 O(n)）");
        System.out.println("取中点建树后，深度 = " + Algorithms.maxDepth(balanced) + "，平衡吗？ "
                + Algorithms.isBalanced(balanced));

        Algorithms.TreeNode valid = Algorithms.buildBst(5, 3, 8, 1, 4, 7, 9);
        Algorithms.TreeNode invalid = new Algorithms.TreeNode(10);
        invalid.left = new Algorithms.TreeNode(5);
        invalid.right = new Algorithms.TreeNode(15);
        invalid.right.left = new Algorithms.TreeNode(6);      // 6 < 10 却在右子树里
        System.out.println("合法二叉搜索树？ " + Algorithms.isValidBst(valid));
        System.out.println("非法二叉搜索树（15 的左孩子是 6）？ " + Algorithms.isValidBst(invalid));
        System.out.println("  → 不能只比较「父 vs 子」，要把上下界一路传下去，否则抓不到这种跨层违规");
    }

    // ==================== 6. 排序与查找 ====================

    private static void sortingAndSearching() {
        section("6. 排序与二分查找");

        int size = 200_000;
        int[] source = new int[size];
        for (int i = 0; i < size; i++) {
            source[i] = RANDOM.nextInt(1_000_000);
        }

        // 计时方式：同一份随机数据排序两次，第一次当预热，第二次才取时间。
        // 不这么做的话，谁先跑谁吃亏（要先把字节码编译成本地机器码），比出来的结果没有意义。
        long builtInMs = timeSort(source, array -> Arrays.sort(array));
        long quickMs = timeSort(source, Algorithms::quickSort);
        long mergeMs = timeSort(source, Algorithms::mergeSort);

        System.out.println("随机数组 " + size + " 个元素排序耗时（取 4 次里最快的一次）：");
        System.out.printf("  Arrays.sort（官方）      %4d ms%n", builtInMs);
        System.out.printf("  手写快速排序             %4d ms%n", quickMs);
        System.out.printf("  手写归并排序             %4d ms%n", mergeMs);
        System.out.println("  → 三者都是同一个数量级，差别只是常数因子。");
        System.out.println("    官方实现的真正优势是「最坏情况有保障」（看下面的退化对比），而不是平均更快。");

        int[] expected = source.clone();
        Arrays.sort(expected);
        int[] check = source.clone();
        Algorithms.quickSort(check);
        System.out.println("  手写排序结果正确吗 = " + Arrays.equals(expected, check));

        // 快排最坏情况：已排序数据 + 取最右为基准 → 退化成 O(n²)，同时递归深度也会到 n
        // （这里刻意只用 1 万，递归太深会撞上 StackOverflowError，这也是快排的另一个隐患）
        int sortedSize = 10_000;
        int[] alreadySorted = new int[sortedSize];
        for (int i = 0; i < sortedSize; i++) {
            alreadySorted[i] = i;
        }
        int[] randomSameSize = new int[sortedSize];
        for (int i = 0; i < sortedSize; i++) {
            randomSameSize[i] = RANDOM.nextInt(1_000_000);
        }

        long randomQuickMs = timeSort(randomSameSize, Algorithms::quickSort);
        long worstQuickMs = timeSort(alreadySorted, Algorithms::quickSort);

        System.out.println();
        System.out.println("同样 " + sortedSize + " 个元素，同一份手写快排（取最右为基准）：");
        System.out.printf("  随机数据    %4d ms  ← 平均 O(n log n)%n", randomQuickMs);
        System.out.printf("  已排好序的  %4d ms  ← 最坏 O(n²)，慢了一个数量级%n", worstQuickMs);
        System.out.println("  → 官方 Arrays.sort 用双轴快排 + 三数取中，就是为了避免这种退化。生产代码直接用 Arrays.sort");

        int[] searchable = {1, 3, 5, 7, 9, 11};
        System.out.println();
        System.out.println("二分查找 " + Arrays.toString(searchable) + "：");
        for (int target : new int[]{7, 1, 11, 4}) {
            System.out.println("  找 " + target + " → 下标 " + Algorithms.binarySearch(searchable, target));
        }
        System.out.println("  → 每次砍掉一半，O(log n)。前提是「有序」；mid 写成 low+(high-low)/2 防溢出");
    }

    // ==================== 7. 动态规划 ====================

    private static void dynamicProgramming() {
        section("7. 动态规划：想清楚「状态」「转移」「边界」");

        System.out.println("① 爬楼梯（一次 1 或 2 阶）：");
        System.out.println("   n=1→" + Algorithms.climbStairs(1) + "  n=2→" + Algorithms.climbStairs(2)
                + "  n=5→" + Algorithms.climbStairs(5) + "  n=45→" + Algorithms.climbStairs(45));
        System.out.println("   dp[i] = dp[i-1] + dp[i-2]；和斐波那契一样，只要存前两个数，空间 O(1)");

        int[] lis = {10, 9, 2, 5, 3, 7, 101, 18};
        System.out.println("② 最长递增子序列 " + Arrays.toString(lis) + " → 长度 " + Algorithms.lengthOfLIS(lis)
                + "（dp[i] = 以 i 结尾的最长长度，O(n²)；进阶可以用二分做到 O(n log n)）");

        System.out.println("③ 零钱兑换 coins=[1,2,5]：");
        for (int amount : new int[]{11, 3, 0, 7}) {
            System.out.println("   凑出 " + amount + " 最少需要 " + Algorithms.coinChange(new int[]{1, 2, 5}, amount) + " 枚");
        }
        System.out.println("   凑出 3 用硬币 [2] → " + Algorithms.coinChange(new int[]{2}, 3) + "（凑不出返回 -1，别返回 int 最大值）");
        System.out.println("   dp[i] = min(dp[i - 硬币] + 1)，从金额小往大推");

        System.out.println("④ 最长公共子序列");
        System.out.println("   \"abcde\" 和 \"ace\" → " + Algorithms.longestCommonSubsequence("abcde", "ace")
                + "；\"abc\" 和 \"abc\" → " + Algorithms.longestCommonSubsequence("abc", "abc")
                + "；\"abc\" 和 \"def\" → " + Algorithms.longestCommonSubsequence("abc", "def"));
        System.out.println("   二维 dp：字符相等就 dp[i-1][j-1]+1，否则取「不要 a 的字符」和「不要 b 的字符」的较大者");

        System.out.println();
        System.out.println("动态规划三步走：");
        System.out.println("  1. 定义状态：dp[i] 到底是什么？（「以 i 结尾」还是「前 i 个」差别很大）");
        System.out.println("  2. 写转移方程：dp[i] 能从哪些更小的状态推出来");
        System.out.println("  3. 定边界与遍历顺序：dp[0] 是什么？该正着推还是倒着推？");
    }

    // ==================== 8. 小结 ====================

    private static void summary() {
        section("8. 刷题方法小结");

        System.out.println("1. 先写暴力解，确认思路对了，再做优化。别一上来就追求最优解。");
        System.out.println("2. 优化方向按这个顺序想：");
        System.out.println("   重复计算 → 哈希表 / 记忆化 / 动态规划");
        System.out.println("   有序或可排序 → 双指针 / 二分查找");
        System.out.println("   需要「最近的」 → 栈；需要「一层层」 → 队列 / BFS");
        System.out.println("3. 写完必须过一遍边界：空输入、单个元素、全相同、全负数、结果不存在、数值溢出。");
        System.out.println("4. 主动说出时间/空间复杂度，这比写对代码更能体现实力。");
        System.out.println("5. 每道题写完后把思路写成注释留在代码里，一周后回看比重新做一遍有效。");
        System.out.println();
        System.out.println("本课所有算法都在 com.hczn.learn.Algorithms，跑测试验证：");
        System.out.println("  ./mvnw test -Dtest=AlgorithmsTest");
    }

    /**
     * 计时排序：跑 4 次取最快的一次，第一次只当预热。
     *
     * <p>为什么要这样：JVM 需要执行几次才会把热点代码从解释执行 → C1 → C2 逐级编译优化，
     * 只测一次测到的是「还没优化好」的速度，而且谁先跑谁吃亏。
     *
     * <p>这也说明了「微基准测试很坑」，生产级别的对比要用 JMH 这类专业工具。
     */
    private static long timeSort(int[] original, Consumer<int[]> sorter) {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int[] data = original.clone();
            long start = System.nanoTime();
            sorter.accept(data);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            if (i > 0) {
                best = Math.min(best, elapsed);
            }
        }
        return best;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }
}
