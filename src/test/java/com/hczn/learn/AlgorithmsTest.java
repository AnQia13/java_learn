package com.hczn.learn;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 第 8 课：算法测试。
 *
 * <p>运行：{@code ./mvnw test -Dtest=AlgorithmsTest}
 *
 * <p>算法题测试的重点是「边界」：空输入、单个元素、全相同、全负数、
 * 结果不存在、以及能不能骗过溢出。
 */
public class AlgorithmsTest {

    // ==================== 数组与双指针 ====================

    @Test
    public void twoSumSortedShouldFindPair() {
        int[] nums = {-4, -1, -1, 0, 1, 2, 5};
        assertArrayEquals(new int[]{0, 6}, Algorithms.twoSumSorted(nums, 1));   // -4 + 5
    }

    @Test
    public void twoSumSortedShouldReportNotFound() {
        assertArrayEquals(new int[]{-1, -1}, Algorithms.twoSumSorted(new int[]{1, 2, 3}, 100));
        assertArrayEquals(new int[]{-1, -1}, Algorithms.twoSumSorted(new int[]{}, 1));
        assertArrayEquals(new int[]{-1, -1}, Algorithms.twoSumSorted(new int[]{1}, 1));   // 只有一个元素
    }

    /** 用 int 相加会溢出成 Integer.MIN_VALUE，从而错误地「命中」，这里是防溢出的回归测试。 */
    @Test
    public void twoSumSortedShouldNotBeFooledByIntOverflow() {
        assertArrayEquals(new int[]{-1, -1},
                Algorithms.twoSumSorted(new int[]{1, Integer.MAX_VALUE}, Integer.MIN_VALUE));
    }

    @Test
    public void twoSumShouldFindPair() {
        assertArrayEquals(new int[]{0, 1}, Algorithms.twoSum(new int[]{2, 7, 11, 15}, 9));
        assertArrayEquals(new int[]{-1, -1}, Algorithms.twoSum(new int[]{1, 2}, 100));
    }

    /** 暴力解和优化解必须给出同样的答案，否则优化就是错的。 */
    @Test
    public void bruteForceAndHashShouldAgree() {
        int[] nums = {3, 3, 11, 15, 7, 2};
        assertArrayEquals(Algorithms.twoSumBruteForce(nums, 9), Algorithms.twoSum(nums, 9));
        assertArrayEquals(Algorithms.twoSumBruteForce(nums, 500), Algorithms.twoSum(nums, 500));
    }

    @Test
    public void removeElementShouldCompactInPlace() {
        int[] nums = {3, 2, 2, 3, 1};
        int length = Algorithms.removeElement(nums, 3);
        assertEquals(3, length);
        assertArrayEquals(new int[]{2, 2, 1}, Arrays.copyOf(nums, length));
    }

    @Test
    public void removeElementShouldHandleEdgeCases() {
        assertEquals(0, Algorithms.removeElement(new int[]{5, 5, 5}, 5));       // 全删光
        assertEquals(0, Algorithms.removeElement(new int[]{}, 5));               // 空数组
        assertEquals(3, Algorithms.removeElement(new int[]{1, 2, 3}, 99));       // 一个都不删
    }

    @Test
    public void maxSubArrayShouldFindBestSum() {
        assertEquals(6, Algorithms.maxSubArray(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));
        assertEquals(5, Algorithms.maxSubArray(new int[]{5}));
        // 全是负数要返回最大的那个负数，不能返回 0
        assertEquals(-1, Algorithms.maxSubArray(new int[]{-3, -1, -7}));
        assertEquals(-2, Algorithms.maxSubArray(new int[]{-2, -5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxSubArrayShouldRejectEmpty() {
        Algorithms.maxSubArray(new int[0]);
    }

    @Test
    public void threeSumShouldFindUniqueTriples() {
        List<List<Integer>> result = Algorithms.threeSum(new int[]{-1, 0, 1, 2, -1, -4});
        assertEquals(2, result.size());
        assertTrue(result.contains(Arrays.asList(-1, -1, 2)));
        assertTrue(result.contains(Arrays.asList(-1, 0, 1)));
    }

    @Test
    public void threeSumShouldDeduplicateAndHandleEdgeCases() {
        assertEquals(1, Algorithms.threeSum(new int[]{0, 0, 0, 0}).size());      // 重复的 0 只算一组
        assertTrue(Algorithms.threeSum(new int[]{1, 2, 3}).isEmpty());           // 无解
        assertTrue(Algorithms.threeSum(new int[]{1, 2}).isEmpty());              // 不足三个
        assertTrue(Algorithms.threeSum(new int[]{}).isEmpty());
    }

    // ==================== 链表 ====================

    @Test
    public void reverseShouldReverseList() {
        assertEquals("5→4→3→2→1",
                Algorithms.listToString(Algorithms.reverse(Algorithms.buildList(1, 2, 3, 4, 5))));
        assertEquals("1", Algorithms.listToString(Algorithms.reverse(Algorithms.buildList(1))));
        assertEquals("", Algorithms.listToString(Algorithms.reverse(Algorithms.buildList())));
        assertEquals("", Algorithms.listToString(Algorithms.reverse(null)));
    }

    @Test
    public void mergeTwoListsShouldMergeInOrder() {
        assertEquals("1→2→3→4→5→6", Algorithms.listToString(Algorithms.mergeTwoLists(
                Algorithms.buildList(1, 3, 5), Algorithms.buildList(2, 4, 6))));
        // 一条为空、一条为空的边界
        assertEquals("7→8", Algorithms.listToString(Algorithms.mergeTwoLists(
                Algorithms.buildList(7, 8), Algorithms.buildList())));
        assertEquals("7→8", Algorithms.listToString(Algorithms.mergeTwoLists(
                Algorithms.buildList(), Algorithms.buildList(7, 8))));
    }

    @Test
    public void middleNodeShouldHandleOddAndEven() {
        assertEquals(3, Algorithms.middleNode(Algorithms.buildList(1, 2, 3, 4, 5)).val);
        assertEquals(3, Algorithms.middleNode(Algorithms.buildList(1, 2, 3, 4)).val);   // 偶数取靠后的
    }

    @Test
    public void hasCycleShouldDetectCycles() {
        assertFalse(Algorithms.hasCycle(Algorithms.buildList(1, 2, 3)));

        // 尾部接回下标 1 的节点：1→2→3→4→2→...
        assertTrue(Algorithms.hasCycle(Algorithms.createCycle(Algorithms.buildList(1, 2, 3, 4), 1)));
        // 只有自己指向自己
        assertTrue(Algorithms.hasCycle(Algorithms.createCycle(Algorithms.buildList(1), 0)));
        assertFalse(Algorithms.hasCycle(null));
    }

    @Test
    public void removeNthFromEndShouldRemoveRightNode() {
        assertEquals("1→2→3→5", Algorithms.listToString(
                Algorithms.removeNthFromEnd(Algorithms.buildList(1, 2, 3, 4, 5), 2)));
        // 删头节点
        assertEquals("2→3", Algorithms.listToString(
                Algorithms.removeNthFromEnd(Algorithms.buildList(1, 2, 3), 3)));
        // 删尾节点
        assertEquals("1", Algorithms.listToString(
                Algorithms.removeNthFromEnd(Algorithms.buildList(1, 2), 1)));
    }

    // ==================== 栈与队列 ====================

    @Test
    public void isValidParenthesesShouldValidate() {
        assertTrue(Algorithms.isValidParentheses("()[]{}"));
        assertTrue(Algorithms.isValidParentheses("([{}])"));
        assertTrue(Algorithms.isValidParentheses(""));
        assertFalse(Algorithms.isValidParentheses("(]"));
        assertFalse(Algorithms.isValidParentheses("([)]"));      // 顺序错了，栈能抓住
        assertFalse(Algorithms.isValidParentheses("("));         // 有没配对的左括号
        assertFalse(Algorithms.isValidParentheses(")"));         // 右括号比左括号多
    }

    @Test
    public void dailyTemperaturesShouldComputeWaitDays() {
        assertArrayEquals(new int[]{1, 1, 4, 2, 1, 1, 0, 0},
                Algorithms.dailyTemperatures(new int[]{73, 74, 75, 71, 69, 72, 76, 73}));
        // 温度一直不涨 → 全是 0
        assertArrayEquals(new int[]{0, 0, 0}, Algorithms.dailyTemperatures(new int[]{5, 4, 3}));
        assertArrayEquals(new int[]{}, Algorithms.dailyTemperatures(new int[]{}));
    }

    @Test
    public void myQueueShouldBeFifo() {
        Algorithms.MyQueue queue = new Algorithms.MyQueue();
        assertTrue(queue.empty());

        queue.push(1);
        queue.push(2);
        queue.push(3);
        assertEquals(1, queue.peek());
        assertEquals(1, queue.pop());
        assertEquals(2, queue.pop());

        assertFalse(queue.empty());
        queue.push(4);                       // 中途再进队，验证两个栈的搬运逻辑
        assertEquals(3, queue.pop());
        assertEquals(4, queue.pop());
        assertTrue(queue.empty());
    }

    // ==================== 哈希表 ====================

    @Test
    public void groupAnagramsShouldGroupWords() {
        List<List<String>> groups = Algorithms.groupAnagrams(
                new String[]{"eat", "tea", "tan", "ate", "nat", "bat"});
        assertEquals(3, groups.size());
        assertEquals(Arrays.asList("eat", "tea", "ate"), groups.get(0));
        assertEquals(Arrays.asList("tan", "nat"), groups.get(1));
        assertEquals(Arrays.asList("bat"), groups.get(2));
    }

    @Test
    public void groupAnagramsShouldHandleEmptyInput() {
        assertTrue(Algorithms.groupAnagrams(new String[]{}).isEmpty());
    }

    @Test
    public void longestConsecutiveShouldFindLongestRun() {
        assertEquals(4, Algorithms.longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));
        assertEquals(5, Algorithms.longestConsecutive(new int[]{-1, 0, 1, 2, 3}));
        assertEquals(1, Algorithms.longestConsecutive(new int[]{7, 7, 7}));    // 重复数字不算更长
        assertEquals(0, Algorithms.longestConsecutive(new int[]{}));
    }

    // ==================== 二叉树 ====================

    @Test
    public void traversalsShouldBeCorrect() {
        Algorithms.TreeNode root = Algorithms.buildBst(8, 3, 10, 1, 6, 14, 4, 7, 13);

        // 二叉搜索树的中序一定是升序
        assertEquals(Arrays.asList(1, 3, 4, 6, 7, 8, 10, 13, 14), Algorithms.inorder(root));
        assertEquals(Arrays.asList(8, 3, 1, 6, 4, 7, 10, 14, 13), Algorithms.preorder(root));
        assertEquals(Arrays.asList(8, 3, 10, 1, 6, 14, 4, 7, 13), Algorithms.levelOrder(root));
    }

    @Test
    public void traversalsShouldHandleEmptyAndSingleNode() {
        assertTrue(Algorithms.inorder(null).isEmpty());
        assertTrue(Algorithms.levelOrder(null).isEmpty());
        assertEquals(Arrays.asList(42), Algorithms.inorder(new Algorithms.TreeNode(42)));
    }

    @Test
    public void buildBstShouldIgnoreDuplicates() {
        assertEquals(Arrays.asList(1, 2, 3), Algorithms.inorder(Algorithms.buildBst(2, 1, 2, 3, 3)));
    }

    @Test
    public void maxDepthAndBalanceShouldWork() {
        assertEquals(4, Algorithms.maxDepth(Algorithms.buildBst(8, 3, 10, 1, 6, 14, 4, 7, 13)));
        assertEquals(1, Algorithms.maxDepth(new Algorithms.TreeNode(1)));
        assertEquals(0, Algorithms.maxDepth(null));

        assertTrue(Algorithms.isBalanced(Algorithms.buildBalanced(new int[]{1, 2, 3, 4, 5, 6, 7})));
        // 顺序插入 1..5 退化成链，1 和 5 的高度差是 4
        assertFalse(Algorithms.isBalanced(Algorithms.buildBst(1, 2, 3, 4, 5)));
        assertTrue(Algorithms.isBalanced(null));
    }

    @Test
    public void isValidBstShouldRejectCrossLevelViolation() {
        assertTrue(Algorithms.isValidBst(Algorithms.buildBst(5, 3, 8, 1, 4, 7, 9)));
        assertTrue(Algorithms.isValidBst(null));

        Algorithms.TreeNode invalid = new Algorithms.TreeNode(10);
        invalid.left = new Algorithms.TreeNode(5);
        invalid.right = new Algorithms.TreeNode(15);
        invalid.right.left = new Algorithms.TreeNode(6);    // 6 < 10 却在右子树里
        assertFalse(Algorithms.isValidBst(invalid));

        Algorithms.TreeNode duplicate = new Algorithms.TreeNode(2);
        duplicate.left = new Algorithms.TreeNode(2);        // 二叉搜索树不允许重复
        assertFalse(Algorithms.isValidBst(duplicate));
    }

    // ==================== 排序与查找 ====================

    @Test
    public void sortsShouldSortCorrectly() {
        int[] data = {5, 2, 9, 1, 5, 6, -3};
        int[] expected = data.clone();
        Arrays.sort(expected);

        int[] quick = data.clone();
        Algorithms.quickSort(quick);
        assertArrayEquals(expected, quick);

        int[] merge = data.clone();
        Algorithms.mergeSort(merge);
        assertArrayEquals(expected, merge);
    }

    @Test
    public void sortsShouldHandleEmptyAndSingleElement() {
        int[] empty = {};
        Algorithms.quickSort(empty);
        Algorithms.mergeSort(empty);
        assertArrayEquals(new int[]{}, empty);

        int[] single = {7};
        Algorithms.quickSort(single);
        assertArrayEquals(new int[]{7}, single);
    }

    @Test
    public void sortsShouldHandleAlreadySortedInput() {
        // 快排取最右为基准时，已排序数据是最坏情况，但结果必须依然正确
        int[] sorted = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] quick = sorted.clone();
        Algorithms.quickSort(quick);
        assertArrayEquals(sorted, quick);
    }

    @Test
    public void binarySearchShouldFindOrReportMissing() {
        int[] sorted = {1, 3, 5, 7, 9, 11};
        assertEquals(3, Algorithms.binarySearch(sorted, 7));
        assertEquals(0, Algorithms.binarySearch(sorted, 1));
        assertEquals(5, Algorithms.binarySearch(sorted, 11));
        assertEquals(-1, Algorithms.binarySearch(sorted, 4));
        assertEquals(-1, Algorithms.binarySearch(new int[]{}, 1));
    }

    // ==================== 动态规划 ====================

    @Test
    public void climbStairsShouldMatchFibonacci() {
        assertEquals(1, Algorithms.climbStairs(1));
        assertEquals(2, Algorithms.climbStairs(2));
        assertEquals(3, Algorithms.climbStairs(3));
        assertEquals(8, Algorithms.climbStairs(5));
        assertEquals(1836311903, Algorithms.climbStairs(45));
    }

    @Test
    public void lengthOfLISShouldFindLongestIncreasing() {
        assertEquals(4, Algorithms.lengthOfLIS(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
        assertEquals(6, Algorithms.lengthOfLIS(new int[]{1, 2, 3, 4, 5, 6}));
        assertEquals(1, Algorithms.lengthOfLIS(new int[]{5}));
        assertEquals(1, Algorithms.lengthOfLIS(new int[]{3, 3, 3}));    // 严格递增，相等不算
        assertEquals(0, Algorithms.lengthOfLIS(new int[]{}));
    }

    @Test
    public void coinChangeShouldFindMinimumCoins() {
        assertEquals(3, Algorithms.coinChange(new int[]{1, 2, 5}, 11));
        assertEquals(2, Algorithms.coinChange(new int[]{1, 2, 5}, 7));
        assertEquals(1, Algorithms.coinChange(new int[]{5}, 5));
        assertEquals(0, Algorithms.coinChange(new int[]{1}, 0));
        assertEquals(-1, Algorithms.coinChange(new int[]{2}, 3));       // 凑不出要返回 -1
    }

    @Test
    public void longestCommonSubsequenceShouldWork() {
        assertEquals(3, Algorithms.longestCommonSubsequence("abcde", "ace"));
        assertEquals(3, Algorithms.longestCommonSubsequence("abc", "abc"));
        assertEquals(0, Algorithms.longestCommonSubsequence("abc", "def"));
        assertEquals(0, Algorithms.longestCommonSubsequence("", "abc"));
    }
}
