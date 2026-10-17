# Java 学习路径（配合本仓库）

> 工程配置：Maven Wrapper（`./mvnw`，Maven 3.9.9）+ JDK 21（pom 里 `java.version=21`）。
> 源码 UTF-8，JUnit 4 scope=test。

## 怎么用这份路径

1. 每学一个主题，**先自己写一遍**，再对照 `src/main/java/com/hczn/learn/` 下的对应课。
2. 每课末尾都有「练习」，必须动手敲，不要只看。
3. 每课都是一个可以独立运行的 `main`，改一改、跑一跑，看输出变化。

```bash
cd /Users/anqia/Work/GitHub/java_learn

./mvnw compile                    # 编译
./mvnw test                       # 跑测试（当前 49 个用例，1 个 @Ignore 跳过）
./mvnw package                    # 打包成 target/testjava-1.0-SNAPSHOT.jar

# 运行任意一课
java -cp target/classes com.hczn.learn.Lesson01Basics
java -cp target/classes com.hczn.learn.Lesson02Oop
java -cp target/classes com.hczn.learn.Lesson03Collections
java -cp target/classes com.hczn.learn.Lesson04ExceptionsIo
java -cp target/classes com.hczn.learn.Lesson05Concurrency
java -cp target/classes com.hczn.learn.Lesson06ModernJava
java -cp target/classes com.hczn.learn.Lesson07Jvm
java -cp target/classes com.hczn.learn.Lesson08Algorithms
```

用 IDEA 的话，直接点对应 `main` 方法左边的绿色三角。

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

## 仓库里保留的练习文件

你原来写的那些练习文件已经删掉了，现在保留这两个作为对照练习：

| 文件 | 内容 |
| --- | --- |
| `src/test/java/com/hczn/java/NIOTest.java` | `Buffer` / `Channel`（第 4 课相关） |
| `src/test/java/com/hczn/java/TestDemo.java` | 两数之和、两数相加等算法题 |

> 注意：`TestDemo` 里的 `ListNode` 是非静态内部类，必须依附 `TestDemo` 实例才能 `new`
> （第 2 课的内部类知识点）。
> 第 8 课里的 `Algorithms.ListNode` 写成了「静态嵌套类」，可以直接 `new Algorithms.ListNode(1)`，
> 对比着看就能明白 `static` 的差别。

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
./mvnw test -Dtest='!NIOTest'             # 排除某个测试类
```

### 写 JUnit 测试的要点

- 类名以 `Test` 结尾（或 `Test` 开头），Surefire 才认得；方法必须 `public void`、无参数。
- 断言把「期望值」写在前面：`assertEquals(期望, 实际)`，失败信息才好读。
- 数组/字符串要逐个比内容，用 `assertArrayEquals`，别用 `assertEquals`（那比的是引用）。
- JUnit 4.12 用 `@Test(expected = XxxException.class)` 断言异常；`assertThrows` 要 4.13+。
- 测试要测边界：空串、单元素、全负数、null 入参。
- 纯函数最好测；依赖数据库/网络/当前时间的代码先重构再测。

## 第 8 课配套：算法速查

第 8 课把算法都放在 `Algorithms.java` 里（全是 `static` 纯函数），方便单独测：

```bash
./mvnw test -Dtest=AlgorithmsTest         # 只跑算法测试（35 个用例）
java -cp target/classes com.hczn.learn.Lesson08Algorithms   # 看演示 + 实测性能对比
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
> 真正的微基准测试要用 JMH，课里已经用「跑多次取最快」的方式尽量降低误差。

## 推荐节奏

- **每天 1 小时**：30 分钟看 + 写代码，20 分钟刷 1 道算法题，10 分钟复盘笔记。
- **不要只看不写**。看懂和写出来是两件事。
- 遇到报错先自己读堆栈（从最下面的 `Caused by` 往上找根因），再用搜索引擎。
