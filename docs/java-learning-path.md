# Java 学习路径（配合本仓库）

> 工程配置：Maven Wrapper（`./mvnw`，Maven 3.9.9）+ JDK 21（pom 里 `java.version=21`）。
> 源码 UTF-8，JUnit 4.13.2 scope=test，H2 2.2.224 scope=runtime（只有第 10 课用）。

## 怎么用这份路径

1. 每学一个主题，**先自己写一遍**，再对照 `src/main/java/com/hczn/learn/` 下的对应课。
2. 每课末尾都有「练习」，必须动手敲，不要只看。
3. 每课都是一个可以独立运行的 `main`，改一改、跑一跑，看输出变化。

> 自学推荐配合 [`学员手册.md`](./学员手册.md)：每课一页，只有目标 / 知识点 / 易错提醒 / 练习 / 自测答案。
> 想把每课当一堂课来上，看 [`教案.md`](./教案.md)（讲师版）。

```bash
cd /Users/anqia/Work/GitHub/java_learn

./mvnw compile                    # 编译
./mvnw test                       # 跑测试（当前 91 个用例，1 个 @Ignore 跳过）
./mvnw package                    # 打包成 target/testjava-1.0-SNAPSHOT.jar

# 运行任意一课（推荐用脚本，它会自动把第三方依赖带上 classpath）
./scripts/run.sh com.hczn.learn.Lesson01Basics
./scripts/run.sh com.hczn.learn.Lesson02Oop
./scripts/run.sh com.hczn.learn.Lesson03Collections
./scripts/run.sh com.hczn.learn.Lesson04ExceptionsIo
./scripts/run.sh com.hczn.learn.Lesson05Concurrency
./scripts/run.sh com.hczn.learn.Lesson06ModernJava
./scripts/run.sh com.hczn.learn.Lesson07Jvm
./scripts/run.sh com.hczn.learn.Lesson08Algorithms
./scripts/run.sh com.hczn.learn.Lesson09Network
./scripts/run.sh com.hczn.learn.Lesson10Jdbc
./scripts/run.sh com.hczn.learn.Lesson11Collections

# 不带第三方依赖的课也可以直接手写 classpath（少一层包装，更直观）
java -cp target/classes com.hczn.learn.Lesson08Algorithms
```

用 IDEA 的话，直接点对应 `main` 方法左边的绿色三角（依赖 IDEA 会自动加）。

---

## 课程清单

| 课 | 文件 | 主题 | 重点 |
| --- | --- | --- | --- |
| **01** | `Lesson01Basics.java` | 语言基础 | 8 种基本类型、`var`、整数除法截断、`++i`/`i++`、数组、方法重载与传参、`String` 不可变、`Integer` 缓存坑 |
| **02** | `Lesson02Oop.java` | 面向对象 | 构造器与 `this`、封装、`static`/`final`、继承与多态、抽象类 vs 接口、内部类、`equals`/`hashCode`、枚举 |
| **03** | `Lesson03Collections.java` | 集合与泛型 | `ArrayList`/`LinkedList`、`HashSet`/`TreeSet`、`HashMap` 内部原理、可变 key 的坑、遍历中删除、`Comparable`/`Comparator`、泛型与 PECS |
| **04** | `Lesson04ExceptionsIo.java` | 异常与 IO/NIO | 异常体系、受检 vs 非受检、`finally`、自定义异常与异常链、try-with-resources、`Files` 读写、缓冲流、`Files.walk` |
| **05** | `Lesson05Concurrency.java` | 多线程与并发 | `start()` vs `run()`、竞态条件、`synchronized`、`volatile`、CAS 与原子类、线程池、`ConcurrentHashMap`、死锁 |
| **06** | `Lesson06ModernJava.java` | Java 8+ 现代写法 | Lambda、函数式接口、方法引用、`Stream`、`Collectors`、`Optional`、`java.time`、record / 模式匹配 / switch 表达式 / 文本块 |
| **07** | `Lesson07Jvm.java` + `TextUtils.java` / `TextUtilsTest.java` | JVM 与工程化 | 堆 / 栈 / 方法区、可达性分析与分代回收、四种引用、字符串常量池、双亲委派与类初始化、反射、`javac`/`java`/`jar`、Maven 生命周期、JUnit |
| **08** | `Lesson08Algorithms.java` + `Algorithms.java` / `AlgorithmsTest.java` | 算法与数据结构 | 复杂度分析、双指针、哈希表（空间换时间）、链表（哑节点/快慢指针）、栈与单调栈、二叉树遍历、快排/归并/二分、动态规划 |
| **09** | `Lesson09Network.java` | 网络编程 | IP/端口/TCP vs UDP、URI 解析、`NetworkInterface`、`ServerSocket`/`Socket` 回显、`DatagramSocket`、JDK 11+ `HttpClient`（同步/异步） |
| **10** | `Lesson10Jdbc.java` + `WalletService.java` / `WalletServiceTest.java` | JDBC 与数据库 | JDBC 五步曲、`Statement` vs `PreparedStatement`、**SQL 注入实测**、`ResultSet`、影响行数、事务与回滚、`addBatch`、元数据、连接池 |
| **11** | `Lesson11Collections.java` + `MiniArrayList.java` / `MiniHashMap.java` / `MiniCollectionsTest.java` | 手写集合，看穿底层 | 数组扩容 1.5 倍、中间插入 O(n)、fail-fast 与 `modCount`（含「有时不抛异常」的坑）、哈希桶与冲突、扰动函数、2 的幂容量、扩容 rehash、可变 key 陷阱、JMH 微基准 |

## 仓库里保留的练习文件

你原来写的练习文件基本都删掉了，现在只剩这一个：

| 文件 | 内容 |
| --- | --- |
| `src/test/java/com/hczn/java/TestDemo.java` | 两数之和、两数相加等算法题 |

> 注意：`TestDemo` 里的 `ListNode` 是非静态内部类，必须依附 `TestDemo` 实例才能 `new`
> （第 2 课的内部类知识点）。
> 第 8 课里的 `Algorithms.ListNode` 写成了「静态嵌套类」，可以直接 `new Algorithms.ListNode(1)`，
> 对比着看就能明白 `static` 的差别。

## 外部依赖与运行方式

| 依赖 | 版本 | scope | 用途 |
| --- | --- | --- | --- |
| `junit:junit` | 4.13.2 | test | 单元测试（4.13 才有 `assertThrows`） |
| `com.h2database:h2` | 2.2.224 | runtime | 第 10 课的内存数据库 |

`h2` 为什么要用 `runtime` 而不是 `compile`？因为第 10 课的代码只 `import java.sql.*`，
编译期根本不认识 H2 —— 驱动是靠 JDBC 4.0 的 SPI 在运行时自动发现的。
这正是「面向接口编程」的好处：换数据库只改 pom 和连接串，业务代码一行不动。

> 代价是：`java -cp target/classes` 少了 H2 的 jar。所以第 10 课要用
> `./scripts/run.sh`，它会先让 Maven 算出完整 classpath 再启动。

### 三个脚本

```bash
./scripts/run.sh com.hczn.learn.Lesson10Jdbc    # 编译 + 带上依赖 + 运行任意一课
./scripts/benchmark.sh                          # 跑全部 JMH 微基准
./scripts/benchmark.sh ".*TwoSumBenchmark.*"    # 只跑匹配的基准
```

## 第 7 课配套：命令行速查

### 不用 Maven，手工编译与运行

```bash
# 编译单个文件到指定目录（-d 指定输出目录，目录会自动创建）
javac -encoding UTF-8 -d out/classes src/main/java/com/hczn/learn/Lesson07Jvm.java

# 运行：-cp 是 classpath，必须指向「包的根目录」，而不是类文件所在目录
java -cp out/classes com.hczn.learn.Lesson07Jvm

# 编译整个 main 目录（find 把文件列表喂给 javac）
javac -encoding UTF-8 -d out/classes $(find src/main/java -name '*.java')
```

> 关键理解：`Lesson07Jvm.java` 里写的是 `package com.hczn.learn;`，
> 所以 javac 会生成 `out/classes/com/hczn/learn/Lesson07Jvm.class`，
> `-cp` 要指向 `out/classes`（包名的上一级），类名要写全限定名 `com.hczn.learn.Lesson07Jvm`。

### 打成可执行 jar

```bash
# --main-class 会写进 MANIFEST.MF 的 Main-Class，这样 java -jar 才能找到入口
jar --create --file demo.jar --main-class com.hczn.learn.Lesson07Jvm -C out/classes .

jar --list --file demo.jar        # 查看内容（缩写 jar tf）
java -jar demo.jar                # 直接运行

# 看清单文件
unzip -p demo.jar META-INF/MANIFEST.MF
```

### JVM 参数

```bash
java -Xms64m -Xmx64m -cp target/classes com.hczn.learn.Lesson07Jvm   # 固定堆大小
java -Xss512k -cp target/classes com.hczn.learn.Lesson07Jvm          # 调小栈，更容易复现 StackOverflowError
java -XX:+PrintGCDetails -cp target/classes com.hczn.learn.Lesson07Jvm   # 打印 GC 细节（JDK 9+ 建议用 -Xlog:gc）
java -Xlog:gc -cp target/classes com.hczn.learn.Lesson07Jvm          # JDK 9+ 的 GC 日志
```

### Maven 生命周期（每一步都包含前面所有步骤）

| 阶段 | 作用 | 产物 |
| --- | --- | --- |
| `clean` | 删除 `target/` | — |
| `compile` | 编译 `src/main/java` | `target/classes/*.class` |
| `test-compile` | 再编译 `src/test/java` | `target/test-classes/*.class` |
| `test` | 跑测试（Surefire） | `target/surefire-reports/` |
| `package` | 打成 jar | `target/testjava-1.0-SNAPSHOT.jar` |
| `verify` / `install` / `deploy` | 校验 / 装进本地仓库 / 发布 | — |

```bash
./mvnw clean              # 只清理
./mvnw compile            # clean + compile 一起写也行
./mvnw test               # 编译 + 跑测试
./mvnw package            # 编译 + 测试 + 打包
./mvnw package -DskipTests    # 跳过测试打包

./mvnw test -Dtest=TextUtilsTest          # 只跑某个测试类
./mvnw test -Dtest='WalletServiceTest,MiniCollectionsTest'   # 跑多个
./mvnw test -Dtest='!TestDemo'            # 排除某个测试类
```

### 写 JUnit 测试的要点

- 类名以 `Test` 结尾（或 `Test` 开头），Surefire 才认得；方法必须 `public void`、无参数。
- 断言把「期望值」写在前面：`assertEquals(期望, 实际)`，失败信息才好读。
- 数组/字符串要逐个比内容，用 `assertArrayEquals`，别用 `assertEquals`（那比的是引用）。
- 断言异常用 `assertThrows`（4.13+），它能拿到异常对象，所以还能顺便断言消息：
  ```java
  IllegalArgumentException error =
          assertThrows(IllegalArgumentException.class, () -> TextUtils.max(new int[0]));
  assertTrue(error.getMessage().contains("空"));
  ```
  `@Test(expected = ...)` 是老写法，只能证明「抛了」，说不出抛得对不对。
  ⚠ JUnit 4 里**没有** `assertDoesNotThrow` / `assertTimeout`，那是 JUnit 5 的 API。
- `BigDecimal` 不要用 `assertEquals` 比 —— scale 不同时 `equals` 就为 `false`，
  要用 `compareTo(...) == 0`。
- 测试要测边界：空串、单元素、全负数、null 入参、越界下标。
- 纯函数最好测；依赖数据库/网络/当前时间的代码先重构再测
  （第 10 课把转账逻辑抽到 `WalletService` 就是为了这个）。

## 第 8 课配套：算法速查

第 8 课把算法都放在 `Algorithms.java` 里（全是 `static` 纯函数），方便单独测：

```bash
./mvnw test -Dtest=AlgorithmsTest         # 只跑算法测试（35 个用例）
./scripts/run.sh com.hczn.learn.Lesson08Algorithms   # 看演示 + 实测性能对比
```

| 主题 | 方法 |
| --- | --- |
| 数组/双指针 | `twoSumSorted` `removeElement` `maxSubArray` `threeSum` |
| 哈希表 | `twoSum` `groupAnagrams` `longestConsecutive` |
| 链表 | `reverse` `mergeTwoLists` `middleNode` `hasCycle` `removeNthFromEnd` |
| 栈/队列 | `isValidParentheses` `dailyTemperatures` `MyQueue` |
| 二叉树 | `buildBst` `inorder` `preorder` `levelOrder` `maxDepth` `isBalanced` `isValidBst` |
| 排序/查找 | `quickSort` `mergeSort` `binarySearch` |
| 动态规划 | `climbStairs` `lengthOfLIS` `coinChange` `longestCommonSubsequence` |

> 第 8 课里有两段「实测」输出（O(n²) vs O(n) 的哈希表、快排遇到已排序数据退化）。
> 这些数字受 JIT 和 GC 影响很大，**只看数量级，别当精确基准**。
> 课里用「跑 4 次取最快」的方式尽量降低误差，但真正靠谱的做法是下面第 11 课的 JMH。

## 第 9 课配套：网络编程要点

```bash
./scripts/run.sh com.hczn.learn.Lesson09Network
```

- 只有最后一节（`HttpClient` 请求 `example.com`）需要联网，失败会优雅跳过；Socket / UDP 全在本机跑。
- 服务端用 `new ServerSocket(0)` 让系统分配空闲端口 —— 写死 8080 在别人机器上可能被占用。
- `accept()` / `read()` 都是阻塞的，必须放单独线程并设超时（`setSoTimeout`），否则会挂死。
- 文本流一定要显式指定编码（`UTF_8`），否则中文会变成平台默认编码。

## 第 10 课配套：JDBC 要点

```bash
./scripts/run.sh com.hczn.learn.Lesson10Jdbc
./mvnw test -Dtest=WalletServiceTest
```

- 数据库是 H2 内存库，进程退出数据就没了，不用装任何服务。
- 想看图形界面：`java -cp <h2的jar> org.h2.tools.Console`（连接串填 `jdbc:h2:mem:learn`）。
- 演示里最重要的一段是 **SQL 注入对比**：同一个恶意输入，拼接 SQL 会返回全表，占位符则查不到。
- 想换成文件数据库持久化，把 URL 改成 `jdbc:h2:./data/learn` 即可。

## 第 11 课配套：手写集合与 JMH

| 手写实现 | 对应 JDK 类 | 关键结论 |
| --- | --- | --- |
| `MiniArrayList` | `java.util.ArrayList` | 随机访问 O(1)、中间增删 O(n)、扩容 1.5 倍、`modCount` 驱动 fail-fast |
| `MiniHashMap` | `java.util.HashMap` | 数组+链表、容量 2 的幂、扰动函数、负载因子 0.75、冲突退化成 O(n) |

```bash
./scripts/run.sh com.hczn.learn.Lesson11Collections
./mvnw test -Dtest=MiniCollectionsTest        # 33 个用例
```

JMH 微基准放在 `src/test/java/com/hczn/learn/benchmark/`，**默认构建不编译它们**
（JMH 依赖只在 `benchmarks` profile 里），要用就带上 profile：

```bash
./scripts/benchmark.sh                                   # 全部（约 1~2 分钟）
./scripts/benchmark.sh ".*TwoSumBenchmark.*"             # 只跑匹配的
./scripts/benchmark.sh ".*MapBenchmark.*"                # 手写 HashMap vs JDK HashMap
```

| 基准类 | 对比内容 |
| --- | --- |
| `TwoSumBenchmark` | 暴力 O(n²) vs 哈希表 O(n)，n = 500 / 2000 |
| `SortBenchmark` | `Arrays.sort` vs 手写快排 vs 归并；含「已排序数据」下快排退化 |
| `MapBenchmark` | `MiniHashMap` vs `HashMap` 的 get，n = 1000 / 100000 |

实测参考（本机 JDK 21，仅供数量级参考）：

```
TwoSumBenchmark.bruteForce     500  avgt   34.8 us/op
TwoSumBenchmark.bruteForce    2000  avgt  544.5 us/op     ← n 涨 4 倍，耗时涨约 16 倍 = O(n²)
TwoSumBenchmark.hashTable      500  avgt    6.4 us/op
TwoSumBenchmark.hashTable     2000  avgt   34.0 us/op     ← 接近线性
```

> 想让 MiniHashMap 也体验一把 O(n)：把 `spread()` 直接改成 `return key.hashCode();`
> 再用一个 `hashCode()` 恒定的 key 跑 `MapBenchmark`，数字会非常难看。
> 这就是「一个好的 hashCode 值多少钱」。

## 推荐节奏

- **每天 1 小时**：30 分钟看 + 写代码，20 分钟刷 1 道算法题，10 分钟复盘笔记。
- **不要只看不写**。看懂和写出来是两件事。
- 遇到报错先自己读堆栈（从最下面的 `Caused by` 往上找根因），再用搜索引擎。
