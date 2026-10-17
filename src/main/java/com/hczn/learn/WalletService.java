package com.hczn.learn;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 第 10 课配套：把「转账」这段有事务语义的业务逻辑从演示课里抽出来。
 *
 * <p>为什么要抽出来？因为演示类里的 {@code private static} 方法没法写单元测试。
 * 真实项目里也是这个套路：<b>可测试的逻辑放进独立的类，测试类只负责调它</b>。
 *
 * <p>对应测试：{@code src/test/java/com/hczn/learn/WalletServiceTest.java}
 */
public final class WalletService {

    /** H2 内存库。DB_CLOSE_DELAY=-1：最后一个连接关掉后也别销毁，方便多个连接看到同一份数据。 */
    public static final String URL = "jdbc:h2:mem:learn;DB_CLOSE_DELAY=-1";
    public static final String USER = "sa";
    public static final String PASSWORD = "";

    private WalletService() {
        throw new AssertionError("工具类不允许实例化");
    }

    /** 余额不足（或账户不存在）时抛出。继承 SQLException，方便和真正的数据库错误区分开。 */
    public static class InsufficientBalanceException extends SQLException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 建表。测试里每个用例开头都调一次，保证从干净的状态开始。 */
    public static void createSchema(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS wallet");
            statement.execute("""
                    CREATE TABLE wallet (
                        id         INT PRIMARY KEY,
                        owner_name VARCHAR(50)      NOT NULL,
                        balance    DECIMAL(10, 2)   NOT NULL
                    )
                    """);
        }
    }

    public static void addAccount(Connection conn, int id, String owner, String balance) throws SQLException {
        try (PreparedStatement prepared = conn.prepareStatement("INSERT INTO wallet VALUES (?, ?, ?)")) {
            prepared.setInt(1, id);
            prepared.setString(2, owner);
            prepared.setBigDecimal(3, new BigDecimal(balance));
            prepared.executeUpdate();
        }
    }

    public static BigDecimal balanceOf(Connection conn, int id) throws SQLException {
        try (PreparedStatement prepared = conn.prepareStatement("SELECT balance FROM wallet WHERE id = ?")) {
            prepared.setInt(1, id);
            try (ResultSet rs = prepared.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("账户不存在: " + id);
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    public static List<String> balances(Connection conn) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT owner_name, balance FROM wallet ORDER BY id")) {
            while (rs.next()) {
                result.add(rs.getString(1) + "=" + rs.getBigDecimal(2).toPlainString());
            }
        }
        return result;
    }

    /**
     * 转账：两条 UPDATE 必须在同一个事务里。
     *
     * <p>扣款用的是「带条件的 UPDATE」：{@code WHERE id = ? AND balance >= ?}，
     * 靠「影响行数是不是 1」判断成功，而不是先 SELECT 查余额再决定 ——
     * 后者在并发下是典型的 check-then-act 竞态。
     *
     * <p>任何一步失败都 rollback，并把原来的异常继续抛出去，调用方才能知道为什么失败。
     */
    public static void transfer(Connection conn, int fromId, int toId, BigDecimal amount) throws SQLException {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("转账金额必须为正数: " + amount);
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (PreparedStatement deduct = conn.prepareStatement(
                     "UPDATE wallet SET balance = balance - ? WHERE id = ? AND balance >= ?");
             PreparedStatement add = conn.prepareStatement(
                     "UPDATE wallet SET balance = balance + ? WHERE id = ?")) {

            deduct.setBigDecimal(1, amount);
            deduct.setInt(2, fromId);
            deduct.setBigDecimal(3, amount);
            if (deduct.executeUpdate() != 1) {
                throw new InsufficientBalanceException(
                        "账户 " + fromId + " 余额不足，无法转出 " + amount);
            }

            add.setBigDecimal(1, amount);
            add.setInt(2, toId);
            if (add.executeUpdate() != 1) {
                throw new SQLException("收款账户不存在: " + toId);
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();         // 已经扣掉的钱要还回去
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);   // 必须还原，否则这个连接交回池子后会「改动不生效」
        }
    }
}
