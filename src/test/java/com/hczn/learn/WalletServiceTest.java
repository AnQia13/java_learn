package com.hczn.learn;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * 第 10 课：数据库逻辑的单元测试。
 *
 * <p>运行：{@code ./mvnw test -Dtest=WalletServiceTest}
 *
 * <p>用 H2 内存库的好处：不用装数据库、每次都是干净状态、跑得飞快。
 * 所以「DAO / Service 层必须被测到」这件事没有借口。
 *
 * <p>用到了第 7 课讲的 {@code @Before} / {@code @After}：
 * 每个用例开始前重建表并塞好测试数据，结束后关连接。
 */
public class WalletServiceTest {

    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        connection = WalletService.open();
        WalletService.createSchema(connection);
        WalletService.addAccount(connection, 1, "alice", "1000.00");
        WalletService.addAccount(connection, 2, "bob", "500.00");
    }

    @After
    public void tearDown() throws SQLException {
        connection.close();
    }

    // ==================== 正常路径 ====================

    @Test
    public void transferShouldMoveMoney() throws SQLException {
        WalletService.transfer(connection, 1, 2, new BigDecimal("200.00"));

        assertAmount("800.00", WalletService.balanceOf(connection, 1));
        assertAmount("700.00", WalletService.balanceOf(connection, 2));
    }

    @Test
    public void successfulTransferShouldBeVisibleToOtherConnections() throws SQLException {
        WalletService.transfer(connection, 1, 2, new BigDecimal("100.00"));

        // 换一个连接再查：能看到，说明 commit 真的生效了（而不是只改了本地内存里的对象）
        try (Connection other = WalletService.open()) {
            assertAmount("900.00", WalletService.balanceOf(other, 1));
            assertAmount("600.00", WalletService.balanceOf(other, 2));
        }
    }

    @Test
    public void transferShouldAllowDrainingTheAccountCompletely() throws SQLException {
        WalletService.transfer(connection, 2, 1, new BigDecimal("500.00"));   // 正好转空

        assertAmount("0.00", WalletService.balanceOf(connection, 2));
        assertAmount("1500.00", WalletService.balanceOf(connection, 1));
    }

    // ==================== 事务回滚 ====================

    @Test
    public void transferShouldRollbackWhenBalanceIsInsufficient() throws SQLException {
        assertThrows(WalletService.InsufficientBalanceException.class,
                () -> WalletService.transfer(connection, 2, 1, new BigDecimal("99999.00")));

        // 最关键的两条断言：失败之后，双方余额都必须和转账前一模一样
        assertAmount("500.00", WalletService.balanceOf(connection, 2));
        assertAmount("1000.00", WalletService.balanceOf(connection, 1));
    }

    @Test
    public void transferShouldRollbackWhenTargetAccountIsMissing() throws SQLException {
        // 扣款会先成功，然后才发现收款账户不存在 —— 必须把扣掉的钱还回去
        assertThrows(SQLException.class,
                () -> WalletService.transfer(connection, 1, 999, new BigDecimal("10.00")));

        assertAmount("1000.00", WalletService.balanceOf(connection, 1));
    }

    @Test
    public void failedTransferShouldNotLeakUncommittedState() throws SQLException {
        try {
            WalletService.transfer(connection, 2, 1, new BigDecimal("99999.00"));
        } catch (SQLException ignored) {
            // 预期内
        }

        // 换个连接看：数据库里也必须是原样（说明 rollback 生效，而不是留下一个挂着的事务）
        try (Connection other = WalletService.open()) {
            assertAmount("500.00", WalletService.balanceOf(other, 2));
        }
    }

    // ==================== 参数校验 ====================

    @Test
    public void transferShouldRejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> WalletService.transfer(connection, 1, 2, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> WalletService.transfer(connection, 1, 2, new BigDecimal("-1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> WalletService.transfer(connection, 1, 2, null));
    }

    @Test
    public void reusingTheConnectionAfterFailureShouldStillWork() throws SQLException {
        // 失败的事务不能把连接搞坏 —— 这也是「autoCommit 必须还原」的意义
        assertThrows(SQLException.class,
                () -> WalletService.transfer(connection, 2, 1, new BigDecimal("99999.00")));

        assertTrue("autoCommit 必须恢复成 true", connection.getAutoCommit());

        WalletService.transfer(connection, 1, 2, new BigDecimal("50.00"));   // 还能正常用
        assertAmount("550.00", WalletService.balanceOf(connection, 2));
        assertAmount("950.00", WalletService.balanceOf(connection, 1));
    }

    @Test
    public void balanceOfShouldFailForUnknownAccount() {
        assertThrows(SQLException.class, () -> WalletService.balanceOf(connection, 12345));
    }

    // ==================== PreparedStatement 的安全边界 ====================

    @Test
    public void preparedStatementShouldNeutraliseSqlInjection() throws SQLException {
        String malicious = "x' OR '1'='1";

        try (PreparedStatement prepared = connection.prepareStatement(
                "SELECT COUNT(*) FROM wallet WHERE owner_name = ?")) {
            prepared.setString(1, malicious);
            try (ResultSet rs = prepared.executeQuery()) {
                rs.next();
                // 用拼接的话这里会返回 2（全表），用占位符就是 0 —— 它只是个普通字符串
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    /** BigDecimal 不能用 assertEquals 直接比（scale 不同时 equals 为 false），要 compareTo。 */
    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals("金额不符，期望 " + expected + "，实际 " + actual,
                0, new BigDecimal(expected).compareTo(actual));
    }
}
