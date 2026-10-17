package com.hczn.learn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 第 8 课配套：常见算法与数据结构的实现集合。
 *
 * <p>全部是 static 纯函数（输入确定 → 输出确定），所以可以直接用 JUnit 测。
 * 对应测试：{@code src/test/java/com/hczn/learn/AlgorithmsTest.java}
 *
 * <p>教学演示与复杂度分析见 {@link Lesson08Algorithms}。
 */
public final class Algorithms {

    private Algorithms() {
        throw new AssertionError("工具类不允许实例化");
    }

    // ==================================================================
    // 一、数组与双指针
    // ==================================================================

    /** 有序数组的两数之和：双指针，时间 O(n)，空间 O(1)。返回下标，找不到返回 {-1,-1}。 */
    public static int[] twoSumSorted(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        while (left < right) {
            // 用 long 相加，避免两个大 int 相加溢出
            long sum = (long) nums[left] + nums[right];
            if (sum == target) {
                return new int[]{left, right};
            }
            if (sum < target) {
                left++;          // 和太小 → 左指针右移让和变大
            } else {
                right--;         // 和太大 → 右指针左移让和变小
            }
        }
        return new int[]{-1, -1};
    }

    /** 无序数组的两数之和：哈希表，时间 O(n)，空间 O(n)。 */
    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();      // 值 → 下标
        for (int i = 0; i < nums.length; i++) {
            Integer match = seen.get(target - nums[i]);    // 我之前存过「还差多少」吗
            if (match != null) {
                return new int[]{match, i};
            }
            seen.put(nums[i], i);
        }
        return new int[]{-1, -1};
    }

    /** 暴力两数之和：时间 O(n²)。只用来和上面的 O(n) 解法做对比。 */
    public static int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if ((long) nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1};
    }

    /** 原地移除所有等于 val 的元素（快慢指针），返回新长度。前 length 个即结果。 */
    public static int removeElement(int[] nums, int val) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != val) {
                nums[slow++] = nums[fast];      // slow 指向下一个要写入的位置
            }
        }
        return slow;
    }

    /** 最大子数组和（Kadane 算法），时间 O(n)，空间 O(1)。 */
    public static int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("数组不能为 null 或空");
        }
        int best = nums[0];
        int current = nums[0];                  // 以 i 结尾的最大子数组和
        for (int i = 1; i < nums.length; i++) {
            // 要么接在前面继续累加，要么从自己重新开始
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    /** 三数之和为 0 的所有不重复组合：排序 + 双指针，时间 O(n²)。 */
    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums.length < 3) {
            return result;
        }
        int[] sorted = nums.clone();            // 不改调用方的数组
        Arrays.sort(sorted);

        for (int i = 0; i + 2 < sorted.length; i++) {
            if (sorted[i] > 0) {
                break;                          // 最小值都大于 0，后面不可能凑出 0
            }
            if (i > 0 && sorted[i] == sorted[i - 1]) {
                continue;                       // 跳过重复的固定值
            }
            int left = i + 1;
            int right = sorted.length - 1;
            while (left < right) {
                int sum = sorted[i] + sorted[left] + sorted[right];
                if (sum == 0) {
                    result.add(Arrays.asList(sorted[i], sorted[left], sorted[right]));
                    while (left < right && sorted[left] == sorted[left + 1]) {
                        left++;                 // 跳过重复的第二个数
                    }
                    while (left < right && sorted[right] == sorted[right - 1]) {
                        right--;                // 跳过重复的第三个数
                    }
                    left++;
                    right--;
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }

    // ==================================================================
    // 二、链表
    // ==================================================================

    /**
     * 链表节点：注意这里是 <b>静态嵌套类</b>（有 static）。
     *
     * <p>对比 {@code com.hczn.java.TestDemo} 里的 {@code ListNode} —— 那里没有 static，
     * 属于「成员内部类」，必须先 new 出 TestDemo 实例才能创建节点，写算法题时很别扭。
     */
    public static class ListNode {
        public final int val;
        public ListNode next;

        public ListNode(int val) {
            this.val = val;
        }

        public ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    /** 用一串值构建链表，例如 buildList(1,2,3) → 1→2→3。 */
    public static ListNode buildList(int... values) {
        ListNode dummy = new ListNode(0);       // 哑节点：省掉「第一个节点要特殊处理」的分支
        ListNode tail = dummy;
        for (int value : values) {
            tail.next = new ListNode(value);
            tail = tail.next;
        }
        return dummy.next;
    }

    public static String listToString(ListNode head) {
        StringBuilder builder = new StringBuilder();
        for (ListNode current = head; current != null; current = current.next) {
            builder.append(current.val);
            if (current.next != null) {
                builder.append("→");
            }
        }
        return builder.toString();
    }

    /** 反转链表（迭代版），时间 O(n)，空间 O(1)。 */
    public static ListNode reverse(ListNode head) {
        ListNode prev = null;
        while (head != null) {
            ListNode next = head.next;          // 先存下后继，否则改完指针就找不到了
            head.next = prev;
            prev = head;
            head = next;
        }
        return prev;
    }

    /** 合并两个升序链表。 */
    public static ListNode mergeTwoLists(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) {
                tail.next = a;
                a = a.next;
            } else {
                tail.next = b;
                b = b.next;
            }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;        // 剩下那串直接接上
        return dummy.next;
    }

    /** 链表中间节点：快慢指针，快指针走两步、慢指针走一步。 */
    public static ListNode middleNode(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    /** 判断链表有没有环（Floyd 判圈算法）。 */
    public static boolean hasCycle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                return true;                    // 有环的话快指针早晚会追上慢指针
            }
        }
        return false;
    }

    /** 把链表尾部接到下标 pos 的节点上，用来构造带环的链表测试数据。pos < 0 表示不造环。 */
    public static ListNode createCycle(ListNode head, int pos) {
        List<ListNode> nodes = new ArrayList<>();
        for (ListNode current = head; current != null; current = current.next) {
            nodes.add(current);
        }
        if (pos >= 0 && pos < nodes.size()) {
            nodes.get(nodes.size() - 1).next = nodes.get(pos);
        }
        return head;
    }

    /** 删除倒数第 n 个节点：一次遍历，快指针先走 n+1 步。 */
    public static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0, head);   // 加了哑节点，删头节点就不用特判
        ListNode fast = dummy;
        ListNode slow = dummy;
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }
        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        slow.next = slow.next.next;
        return dummy.next;
    }

    // ==================================================================
    // 三、栈与队列
    // ==================================================================

    /** 括号匹配：遇到左括号压入对应的右括号，遇到右括号就弹栈比对。 */
    public static boolean isValidParentheses(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(':
                    stack.push(')');
                    break;
                case '[':
                    stack.push(']');
                    break;
                case '{':
                    stack.push('}');
                    break;
                default:
                    if (stack.isEmpty() || stack.pop() != c) {
                        return false;
                    }
            }
        }
        return stack.isEmpty();                 // 还有没配对的左括号 → 非法
    }

    /** 每日温度：单调递减栈，时间 O(n)。返回「还要等几天才会更暖」。 */
    public static int[] dailyTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length];
        Deque<Integer> stack = new ArrayDeque<>();     // 存下标，栈内温度从栈底到栈顶递减
        for (int i = 0; i < temperatures.length; i++) {
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int previous = stack.pop();
                result[previous] = i - previous;
            }
            stack.push(i);
        }
        return result;
    }

    /** 用两个栈实现队列：进队压 in，出队时把 in 全部倒进 out。 */
    public static class MyQueue {
        private final Deque<Integer> in = new ArrayDeque<>();
        private final Deque<Integer> out = new ArrayDeque<>();

        public void push(int value) {
            in.push(value);
        }

        public int pop() {
            moveIfNeeded();
            return out.pop();
        }

        public int peek() {
            moveIfNeeded();
            return out.peek();
        }

        public boolean empty() {
            return in.isEmpty() && out.isEmpty();
        }

        /** 只有 out 空了才需要倒，每个元素最多被搬一次，所以均摊仍是 O(1)。 */
        private void moveIfNeeded() {
            if (out.isEmpty()) {
                while (!in.isEmpty()) {
                    out.push(in.pop());
                }
            }
        }
    }

    // ==================================================================
    // 四、哈希表技巧
    // ==================================================================

    /** 字母异位词分组：把「排序后的字符串」当作分组的 key。 */
    public static List<List<String>> groupAnagrams(String[] words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();   // LinkedHashMap 保证输出顺序稳定
        for (String word : words) {
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            groups.computeIfAbsent(new String(chars), key -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }

    /** 最长连续序列：只从「一段数字的起点」开始数，所以整体是 O(n)。 */
    public static int longestConsecutive(int[] nums) {
        Set<Integer> values = new HashSet<>();
        for (int n : nums) {
            values.add(n);
        }
        int best = 0;
        for (int n : values) {
            if (values.contains(n - 1)) {
                continue;                       // 不是起点，跳过，避免重复扫描
            }
            int length = 1;
            while (values.contains(n + length)) {
                length++;
            }
            best = Math.max(best, length);
        }
        return best;
    }

    // ==================================================================
    // 五、二叉树
    // ==================================================================

    public static class TreeNode {
        public final int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    /** 按顺序插入构建二叉搜索树；如果输入已排序，会退化成一条链。 */
    public static TreeNode buildBst(int... values) {
        TreeNode root = null;
        for (int value : values) {
            root = insertBst(root, value);
        }
        return root;
    }

    public static TreeNode insertBst(TreeNode root, int value) {
        if (root == null) {
            return new TreeNode(value);
        }
        if (value < root.val) {
            root.left = insertBst(root.left, value);
        } else if (value > root.val) {
            root.right = insertBst(root.right, value);
        }
        return root;                            // 相等就忽略，不允许重复
    }

    /** 用有序数组构建平衡二叉搜索树（每次取中点当根）。 */
    public static TreeNode buildBalanced(int[] sortedValues) {
        return buildBalanced(sortedValues, 0, sortedValues.length - 1);
    }

    private static TreeNode buildBalanced(int[] values, int low, int high) {
        if (low > high) {
            return null;
        }
        int mid = (low + high) / 2;
        TreeNode node = new TreeNode(values[mid]);
        node.left = buildBalanced(values, low, mid - 1);
        node.right = buildBalanced(values, mid + 1, high);
        return node;
    }

    /** 中序遍历：二叉搜索树的中序结果一定是升序的。 */
    public static List<Integer> inorder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private static void inorder(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }
        inorder(node.left, result);
        result.add(node.val);
        inorder(node.right, result);
    }

    /** 前序遍历：根 → 左 → 右。 */
    public static List<Integer> preorder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        preorder(root, result);
        return result;
    }

    private static void preorder(TreeNode node, List<Integer> result) {
        if (node == null) {
            return;
        }
        result.add(node.val);
        preorder(node.left, result);
        preorder(node.right, result);
    }

    /** 层序遍历（BFS）：借队列一层一层来。 */
    public static List<Integer> levelOrder(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            result.add(node.val);
            if (node.left != null) {
                queue.add(node.left);
            }
            if (node.right != null) {
                queue.add(node.right);
            }
        }
        return result;
    }

    public static int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }

    /** 是否平衡二叉树：自底向上算高度，一旦发现不平衡立刻用 -1 短路，避免重复计算。 */
    public static boolean isBalanced(TreeNode root) {
        return heightIfBalanced(root) != -1;
    }

    private static int heightIfBalanced(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int left = heightIfBalanced(node.left);
        if (left == -1) {
            return -1;
        }
        int right = heightIfBalanced(node.right);
        if (right == -1) {
            return -1;
        }
        if (Math.abs(left - right) > 1) {
            return -1;
        }
        return 1 + Math.max(left, right);
    }

    /** 是否合法二叉搜索树：用上下界传递，不能只比较父子节点。 */
    public static boolean isValidBst(TreeNode root) {
        return validate(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private static boolean validate(TreeNode node, long min, long max) {
        if (node == null) {
            return true;
        }
        if (node.val <= min || node.val >= max) {
            return false;
        }
        return validate(node.left, min, node.val) && validate(node.right, node.val, max);
    }

    // ==================================================================
    // 六、排序与查找
    // ==================================================================

    /** 快速排序：平均 O(n log n)，最坏 O(n²)（本例取最右为基准，遇到已排序数据就会退化）。 */
    public static void quickSort(int[] array) {
        quickSort(array, 0, array.length - 1);
    }

    private static void quickSort(int[] array, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(array, low, high);
        quickSort(array, low, pivotIndex - 1);
        quickSort(array, pivotIndex + 1, high);
    }

    private static int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int store = low;
        for (int i = low; i < high; i++) {
            if (array[i] < pivot) {
                swap(array, store++, i);
            }
        }
        swap(array, store, high);
        return store;
    }

    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /** 归并排序：稳定，任何情况都是 O(n log n)，代价是需要额外空间。 */
    public static void mergeSort(int[] array) {
        if (array.length < 2) {
            return;
        }
        int mid = array.length / 2;
        int[] left = Arrays.copyOfRange(array, 0, mid);
        int[] right = Arrays.copyOfRange(array, mid, array.length);
        mergeSort(left);
        mergeSort(right);
        merge(array, left, right);
    }

    private static void merge(int[] array, int[] left, int[] right) {
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.length && j < right.length) {
            array[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];   // <= 保证稳定性
        }
        while (i < left.length) {
            array[k++] = left[i++];
        }
        while (j < right.length) {
            array[k++] = right[j++];
        }
    }

    /** 二分查找：要求数组已升序，返回下标，找不到返回 -1。 */
    public static int binarySearch(int[] sorted, int target) {
        int low = 0;
        int high = sorted.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;   // 写成 (low+high)/2 在极端大数组下会整型溢出
            if (sorted[mid] == target) {
                return mid;
            }
            if (sorted[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    // ==================================================================
    // 七、动态规划
    // ==================================================================

    /** 爬楼梯：dp[i] = dp[i-1] + dp[i-2]，其实就是一个斐波那契，可以滚动变量省空间。 */
    public static int climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        int twoStepsBefore = 1;
        int oneStepBefore = 2;
        for (int i = 3; i <= n; i++) {
            int current = twoStepsBefore + oneStepBefore;
            twoStepsBefore = oneStepBefore;
            oneStepBefore = current;
        }
        return oneStepBefore;
    }

    /** 最长递增子序列长度：O(n²) 版的 dp，dp[i] 表示「以 i 结尾的最长递增子序列长度」。 */
    public static int lengthOfLIS(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);                     // 每个元素自己至少算长度 1
        int best = 1;
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    /** 零钱兑换：dp[i] 表示凑出金额 i 需要的最少硬币数，凑不出返回 -1。 */
    public static int coinChange(int[] coins, int amount) {
        int impossible = amount + 1;             // 比任何合法答案都大，当「无穷」用
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, impossible);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    /** 最长公共子序列长度：经典的二维 dp。 */
    public static int longestCommonSubsequence(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
