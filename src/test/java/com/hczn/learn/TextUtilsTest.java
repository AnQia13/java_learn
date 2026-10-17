package com.hczn.learn;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 第 7 课：JUnit 单元测试演示。
 *
 * <p>运行：{@code ./mvnw test}，或者 {@code ./mvnw test -Dtest=TextUtilsTest}
 *
 * <p>类名必须以 Test 结尾（或 Test 开头），Surefire 才会自动找到它。
 * JUnit 4 的测试方法要求：public void、无参数、用 @Test 标注。
 */
public class TextUtilsTest {

    // ==================== 生命周期 ====================

    @BeforeClass
    public static void beforeClass() {
        System.out.println("  [@BeforeClass] 整个测试类开始前跑一次，用来准备昂贵资源（必须是 static）");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("  [@AfterClass] 整个测试类结束后跑一次，用来释放资源");
    }

    @Before
    public void setUp() {
        System.out.println("  [@Before] 每个测试方法前都跑，用来准备测试数据");
    }

    @After
    public void tearDown() {
        System.out.println("  [@After] 每个测试方法后都跑，用来清理");
    }

    // ==================== 正常路径 ====================

    @Test
    public void reverseShouldReverseString() {
        assertEquals("edcba", TextUtils.reverse("abcde"));
        assertEquals("", TextUtils.reverse(""));                 // 边界：空串
        assertEquals("a", TextUtils.reverse("a"));               // 边界：单字符
    }

    @Test
    public void isPalindromeShouldHandleChineseAndEdgeCases() {
        assertTrue(TextUtils.isPalindrome("上海自来水来自海上"));
        assertTrue(TextUtils.isPalindrome("abcba"));
        assertTrue(TextUtils.isPalindrome(""));                  // 边界：空串算回文
        assertFalse(TextUtils.isPalindrome("hello"));
    }

    @Test
    public void wordCountShouldCountRepeatedWords() {
        Map<String, Integer> count = TextUtils.wordCount("java is great java is fun");

        // assertEquals(期望值, 实际值)，习惯上把期望放前面，失败信息才好看
        assertEquals(2, count.get("java").intValue());            // 出现了 2 次
        assertEquals(2, count.get("is").intValue());
        assertEquals(1, count.get("great").intValue());
        assertEquals(4, count.size());                            // 不同的词只有 java/is/great/fun 四个
    }

    @Test
    public void wordCountShouldIgnoreExtraSpaces() {
        assertEquals(2, TextUtils.wordCount("  a   b  a  ").size());
        assertTrue(TextUtils.wordCount("   ").isEmpty());          // 全是空白 → 空 Map
    }

    @Test
    public void maxShouldReturnLargestValue() {
        assertEquals(9, TextUtils.max(new int[]{3, 9, 2}));
        assertEquals(-1, TextUtils.max(new int[]{-5, -1, -3}));    // 全是负数也要对
        assertEquals(7, TextUtils.max(new int[]{7}));              // 边界：只有一个元素
    }

    // ==================== 异常路径 ====================

    /**
     * JUnit 4.13 的新写法：assertThrows 能拿到异常对象，
     * 所以除了「抛没抛」还能断言消息内容，比 expected= 信息量大得多。
     */
    @Test
    public void maxShouldRejectEmptyArray() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> TextUtils.max(new int[0]));
        assertTrue(error.getMessage().contains("空"));
    }

    @Test
    public void maxShouldRejectNull() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> TextUtils.max(null));
        assertTrue(error.getMessage().contains("null"));
    }

    @Test
    public void reverseShouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> TextUtils.reverse(null));
    }

    /** assertThrows 的进阶用法：用 try/catch 之外的第二种姿势断言「没有抛异常」。 */
    @Test
    public void assertDoesNotThrowShouldPassForValidInput() {
        assertEquals(5, assertDoesNotThrow(() -> TextUtils.max(new int[]{5})));
    }

    // ==================== 其他常用注解 ====================

    /** timeout：超过 100 毫秒就判定失败，用来兜住可能死循环的实现。 */
    @Test(timeout = 100)
    public void maxShouldBeFast() {
        TextUtils.max(new int[]{1, 2, 3});
    }

    /** @Ignore：暂时跳过的测试，会显示为 skipped 而不是失败。 */
    @Ignore("演示用：这个用例还没写完，先跳过")
    @Test
    public void notReadyYet() {
        assertEquals(1, 2);                                      // 故意写错，证明它确实没被执行
    }
}
