package com.hczn.java;

import org.junit.Test;

import java.util.Arrays;
import java.util.Scanner;

public class TestDemo {
    public static void main(String[] args) {
        //键盘输入一个整数数组 nums
        Scanner scanner = new Scanner(System.in);
        int[] nums = new int[10];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = scanner.nextInt();
        }
      //键盘输入目标值 target
        int target = scanner.nextInt();
        //调用方法
        int[] result = twoSum(nums, target);
        //打印结果
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
        }

    }

    private static int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }
    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
  }
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode result = new ListNode();
        ListNode temp = result;
        int carry = 0;
        while (l1 != null || l2 != null) {
            int x = (l1 == null) ? 0 : l1.val;
            int y = (l2 == null) ? 0 : l2.val;
            int sum = x + y + carry;
            carry = sum / 10;
            temp.next = new ListNode(sum % 10);
            temp = temp.next;
            if (l1 != null) l1 = l1.next;
            if (l2 != null) l2 = l2.next;
        }
        if (carry > 0) {
            temp.next = new ListNode(carry);
        }
        return result.next;
    }
    public int lengthOfLongestSubstring(String s) {
        //字符串s不同的字符的个数
        int[] count = new int[256]; //字符串s中不同的字符的个数
        int max = 0;    //最长子串的长度
        int start = 0;  //子串的起始位置
        for (int i = 0; i < s.length(); i++) {
            //如果count[s.charAt(i)] > 0,说明s.charAt(i)在字符串s中出现过，
            // 并且在字符串s中出现的位置是start到i-1，
            // 因此，start = count[s.charAt(i)] + 1;
            if (count[s.charAt(i)] > 0) {
                start = Math.max(start, count[s.charAt(i)]);
            }
            //记录字符串s中s.charAt(i)出现的位置
            count[s.charAt(i)] = i + 1;
            //更新max
            max = Math.max(max, i - start + 1);
        }
        return max;


    }
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {

        int[] nums = new int[nums1.length + nums2.length];  //存放两个数组的合并结果
        int i = 0, j = 0, k = 0;    //i,j分别指向nums1,nums2的下标，k指向nums的下标
        while (i < nums1.length && j < nums2.length) {  //当nums1,nums2的下标都没有超过其长度时 循环

            if (nums1[i] < nums2[j]) {
                nums[k++] = nums1[i++];
            } else {
                nums[k++] = nums2[j++];
            }
        }

        //如果nums1的下标没有超过其长度，则将nums1中剩余的元素添加到nums中
        while (i < nums1.length) {
            nums[k++] = nums1[i++];
        }

        //如果nums2的下标没有超过其长度，则将nums2中剩余的元素添加到nums中
        while (j < nums2.length) {
            nums[k++] = nums2[j++];
        }

        if (nums.length % 2 == 0) {
            return (nums[nums.length / 2 - 1] + nums[nums.length / 2]) / 2.0;
        } else {
            return nums[nums.length / 2];
        }
    }

    public String longestPalindrome(String s) {
        if (s == null || s.length() < 1) {
            return "";
        }
        int start = 0, end = 0;
        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);
            int len2 = expandAroundCenter(s, i, i + 1);
            int len = Math.max(len1, len2);
            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }
        return s.substring(start, end + 1);

    }

    private int expandAroundCenter(String s, int i, int i1) {
        int l = i, r = i1;
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
            l--;
            r++;
        }
        return r - l - 1;
    }

    public int reverse(int x) {
        //数字反转
        int result = 0; //存放结果
        while (x != 0) {    //当x不为0时循环
            int pop = x % 10; //取出x的最后一位
            x = x / 10; //x除以10
            if (result > Integer.MAX_VALUE / 10 || (result == Integer.MAX_VALUE / 10 && pop > 7)) {
                return 0;
            }
            if (result < Integer.MIN_VALUE / 10 || (result == Integer.MIN_VALUE / 10 && pop < -8)) {
                return 0;
            }
            result = result * 10 + pop;
        }
        return result;
    }

    public int myAtoi(String s) {
        s = s.trim();   //去除字符串前后的空格
        if (s.length() == 0) {
            return 0;
        }
        int i = 0;
        if (s.charAt(0) == '+' || s.charAt(0) == '-') { //如果第一个字符是+或者-
            i++;
        }

        int result = 0; //存放结果
        while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {    //当i小于字符串长度并且字符串中的第i个字符是数字时循环
            result = result * 10 + (s.charAt(i) - '0'); //将字符串中的第i个字符转换为数字并且存放在result中
            i++;    //i加1
        }
        if (s.charAt(0) == '-') {
            result = -result;
        }
        //如果整数数超过 32 位有符号整数范围
        if (result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        //如果整数数超过 32 位有符号整数范围
        if (result < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return result;
    }


    public String[] findWords(String[] words) {
        //返回可以使用在 美式键盘 同一行的字母打印出来的单词
        String[] result = new String[words.length];
        int index = 0;
        for (String word : words) {
            if (isOneLine(word)) {
                result[index++] = word;
            }
        }
        return Arrays.copyOf(result, index);

    }

    private boolean isOneLine(String word) {
        //判断是否是美式键盘 同一行的字母打印出来的单词
        char[] chars = word.toCharArray();
        int line = getLine(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            if (getLine(chars[i]) != line) {
                return false;
            }

        }
        return true;
    }

    private int getLine(char aChar) {
        aChar = Character.toLowerCase(aChar);
        String line1 = "qwertyuiop";
        String line2 = "asdfghjkl";
        String line3 = "zxcvbnm";
        if (line1.indexOf(aChar) != -1) {
            return 1;
        } else if (line2.indexOf(aChar) != -1) {
            return 2;
        } else {
            return 3;
        }
    }

    //控制台显示进度条
    public void showProgress(int max, int current) {
        int percent = (int) (current * 100.0 / max);
        System.out.print("\r" + percent + "%");
    }

    @Test
    public void testShowProgress() {
        showProgress(100, 50);
    }


    @Test
            //数组拼接
    public void testArrayConcat() {
        int[] a = {1, 2, 3};
        int[] b = {4, 5, 6};
        int[] c = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        for (int i = 0; i < c.length; i++) {
            System.out.println(c[i]);
        }
    }


}
