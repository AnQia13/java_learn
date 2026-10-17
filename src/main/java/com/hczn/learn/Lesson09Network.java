package com.hczn.learn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 第 9 课：网络编程。
 *
 * <p>运行：
 * <pre>
 *   ./scripts/run.sh com.hczn.learn.Lesson09Network
 *   # 等价于：./mvnw compile &amp;&amp; java -cp target/classes com.hczn.learn.Lesson09Network
 * </pre>
 *
 * <p>本课覆盖：网络基本概念、URI 解析、本机地址、TCP（Socket/ServerSocket）、
 * UDP（DatagramSocket）、HTTP 客户端（JDK 11+ 的 HttpClient）。
 *
 * <p>除了最后一节 HTTP 请求需要联网，其余全部在本机（127.0.0.1）跑，离线也能运行。
 */
public class Lesson09Network {

    public static void main(String[] args) throws Exception {
        concepts();
        uriParsing();
        localAddresses();
        tcpEcho();
        udpPing();
        httpClient();
        summary();
    }

    // ==================== 0. 概念 ====================

    private static void concepts() {
        section("0. 概念：一次请求到底经过了什么");

        System.out.println("IP + 端口 = 一台机器上的一个进程");
        System.out.println("  IP   负责「找到哪台机器」，端口负责「找到机器上的哪个程序」");
        System.out.println("  0~1023   系统保留（80 HTTP、443 HTTPS、22 SSH），普通程序不能监听");
        System.out.println("  1024~49151 注册端口，一般应用用这一段");
        System.out.println("  49152~65535 动态/临时端口，客户端发起连接时系统随机分配");
        System.out.println();
        System.out.println("TCP vs UDP：");
        System.out.println("  TCP  面向连接、可靠、有序、有流量控制；代价是握手和重传");
        System.out.println("  UDP  无连接、不保证送达、不保证顺序；快、开销小");
        System.out.println("        → 传文件、网页用 TCP；直播、游戏、DNS 常用 UDP");
        System.out.println();
        System.out.println("HTTP 就是「跑在 TCP 之上的文本协议」：");
        System.out.println("  请求行 GET /index.html HTTP/1.1");
        System.out.println("  请求头 Host: example.com / User-Agent: ...");
        System.out.println("  空行");
        System.out.println("  请求体（GET 一般没有，POST 才有）");
        System.out.println();
        System.out.println("Java 里的四层工具箱（从低到高）：");
        System.out.println("  Socket / ServerSocket / DatagramSocket  最底层，自己管字节流");
        System.out.println("  URL / URI / URLConnection               较早的抽象，现在少用了");
        System.out.println("  HttpClient（JDK 11+）                    推荐，支持 HTTP/2、异步、超时");
        System.out.println("  OkHttp / Apache HttpClient               第三方，功能更全（连接池、拦截器）");
    }

    // ==================== 1. URI 解析 ====================

    private static void uriParsing() throws Exception {
        section("1. URI / URL：先能拆开，才能拼对");

        URI uri = URI.create("https://search.example.com:8443/product/list?q=java&page=2#detail");
        System.out.println("完整    : " + uri);
        System.out.println("协议    : " + uri.getScheme());
        System.out.println("主机    : " + uri.getHost());
        System.out.println("端口    : " + uri.getPort() + "（-1 表示没写，用协议默认端口）");
        System.out.println("路径    : " + uri.getPath());
        System.out.println("查询串  : " + uri.getQuery());
        System.out.println("片段    : " + uri.getFragment() + "（# 后面的部分，不会发给服务器）");
        System.out.println();
        System.out.println("⚠ 拼 URL 时不要自己写字符串拼接：");
        System.out.println("   错：\"http://api.com/search?q=\" + \"java 教程\"   → 空格和中文没编码，接收方会解析错");
        System.out.println("   对：URLEncoder.encode(\"java 教程\", UTF_8) → " + java.net.URLEncoder.encode("java 教程", UTF_8));
        System.out.println();
        System.out.println("⚠ 用户可控的字符串不要直接拼进 URL/重定向地址（SSRF / 开放重定向漏洞的来源）");
    }

    // ==================== 2. 本机地址 ====================

    private static void localAddresses() throws Exception {
        section("2. 本机地址：为什么建议不写死 IP");

        InetAddress loopback = InetAddress.getLoopbackAddress();
        System.out.println("回环地址    : " + loopback.getHostAddress()
                + "（固定指本机，永远可用，写测试就用它）");
        System.out.println("主机名解析  : " + InetAddress.getLocalHost()
                + "（依赖 hosts/DNS，不一定成功，所以别在关键路径上依赖它）");

        System.out.println();
        System.out.println("网卡列表：");
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface nic = interfaces.nextElement();
            if (!nic.isUp() || nic.isLoopback()) {
                continue;
            }
            StringBuilder addresses = new StringBuilder();
            nic.getInetAddresses().asIterator().forEachRemaining(address ->
                    addresses.append(address.getHostAddress()).append(' '));
            System.out.printf("  %-12s %s%n", nic.getName(), addresses.toString().trim());
        }
    }

    // ==================== 3. TCP ====================

    private static void tcpEcho() throws Exception {
        section("3. TCP：ServerSocket（服务端） + Socket（客户端）");
        System.out.println("服务端流程：bind → accept（阻塞等待）→ 拿到 Socket → 读写字节流 → close");
        System.out.println("客户端流程：connect → 拿到 Socket → 读写字节流 → close");
        System.out.println();

        // 端口写 0 = 让操作系统分配一个空闲端口。
        // 写死 8080 在别人机器上可能被占用，写 0 则任何环境都能跑 —— 测试里很常用。
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        System.out.println("服务端已启动，监听 " + InetAddress.getLoopbackAddress().getHostAddress() + ":" + port);

        Thread serverThread = new Thread(() -> {
            // 每个连接单独开线程处理：这就是最朴素的「一连接一线程」模型
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = server.accept();
                    pool.submit(() -> handleEcho(socket));
                }
            } catch (IOException e) {
                // 关闭 server 时 accept 会抛异常，属于正常退出路径
                System.out.println("  [服务端] 已停止接收连接");
            }
        }, "echo-server");
        serverThread.start();

        try {
            // 客户端：连上以后按行发，按行收
            try (Socket socket = new Socket("127.0.0.1", port);
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);   // autoflush
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), UTF_8))) {

                socket.setSoTimeout(3_000);     // 读超时：防止对端不回应时永远卡住
                for (String message : List.of("hello", "java 网络编程", "bye")) {
                    out.println(message);
                    System.out.println("  客户端发送: " + message + "  →  客户端收到: " + in.readLine());
                }
            }
        } finally {
            server.close();
            serverThread.join(2_000);
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }

        System.out.println();
        System.out.println("⚠ 几个必须知道的坑：");
        System.out.println("  1) accept/read 都是阻塞的，一定要在「单独的线程」或「NIO/异步」里做");
        System.out.println("  2) 一定要设超时（setSoTimeout / connect timeout），否则网络抖动会挂死线程");
        System.out.println("  3) 每个 Socket 都要 close，用 try-with-resources；靠 GC 回收会耗尽句柄");
        System.out.println("  4) 文本写 PrintWriter 时注意编码，不写就用平台默认编码 → 中文变乱码");
        System.out.println("  5) 「一连接一线程」扛不住高并发，生产上用线程池或 NIO（Netty）");
    }

    /** 服务端处理单个连接：回显，并在前面加上收到的字符数。 */
    private static void handleEcho(Socket socket) {
        try (socket;                                  // Java 9+ 允许直接写已声明的变量
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {   // 返回 null 表示对端关闭了连接
                System.out.println("  [" + Thread.currentThread().getName() + "] 服务端收到: " + line);
                out.println("ECHO(" + line.length() + "): " + line);
            }
        } catch (IOException e) {
            System.out.println("  [服务端] 连接异常: " + e.getMessage());
        }
    }

    // ==================== 4. UDP ====================

    private static void udpPing() throws Exception {
        section("4. UDP：DatagramSocket");
        System.out.println("UDP 没有连接的概念，发出去就是一个「数据包」：");
        System.out.println("  send(DatagramPacket)  /  receive(DatagramPacket)（阻塞等待）");
        System.out.println();

        try (DatagramSocket server = new DatagramSocket(0);
             DatagramSocket client = new DatagramSocket()) {

            int port = server.getLocalPort();

            byte[] ping = "PING".getBytes(UTF_8);
            client.send(new DatagramPacket(ping, ping.length, InetAddress.getLoopbackAddress(), port));
            System.out.println("  客户端已发出 PING");

            byte[] buffer = new byte[64];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            server.receive(request);
            System.out.println("  服务端收到: " + new String(request.getData(), 0, request.getLength(), UTF_8)
                    + "  来自 " + request.getSocketAddress() + "（顺便就知道了回信地址）");

            byte[] pong = "PONG".getBytes(UTF_8);
            server.send(new DatagramPacket(pong, pong.length, request.getSocketAddress()));

            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            client.receive(response);
            System.out.println("  客户端收到: " + new String(response.getData(), 0, response.getLength(), UTF_8));
        }

        System.out.println();
        System.out.println("⚠ UDP 不保证送达、不保证顺序、不保证只到一次，还可能收到重复包。");
        System.out.println("  要可靠就在应用层自己做「确认 + 重传 + 序号去重」。");
    }

    // ==================== 5. HTTP 客户端 ====================

    private static void httpClient() {
        section("5. HTTP：java.net.http.HttpClient（JDK 11+，推荐）");
        System.out.println("不需要任何第三方依赖，JDK 自带，支持 HTTP/2、连接复用、异步。");
        System.out.println();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.com"))
                .timeout(Duration.ofSeconds(5))                 // 注意：连接超时和读超时是两个东西
                .header("User-Agent", "java-learn/1.0")
                .GET()
                .build();

        System.out.println("  GET https://example.com ...");
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("  状态码    : " + response.statusCode());
            System.out.println("  响应头    : content-type = "
                    + response.headers().firstValue("content-type").orElse("(无)"));
            System.out.println("  HTTP 版本 : " + response.version());

            String body = response.body().replaceAll("\\s+", " ").trim();
            System.out.println("  正文预览  : " + body.substring(0, Math.min(110, body.length())) + " ...");
        } catch (Exception e) {
            System.out.println("  请求失败：" + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.out.println("  （多半是没联网或被代理拦了；这段代码本身是对的，只是演示 API 用法）");
        }

        System.out.println();
        System.out.println("  同一个请求，换成异步只要把 send 改成 sendAsync：");
        System.out.println("    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())");
        System.out.println("        .thenApply(HttpResponse::statusCode)   // CompletableFuture 链式组合");
        System.out.println("        .thenAccept(System.out::println);");
        System.out.println();
        System.out.println("  POST 请求体用 BodyPublishers：");
        System.out.println("    .header(\"Content-Type\", \"application/json\")");
        System.out.println("    .POST(HttpRequest.BodyPublishers.ofString(\"{\\\"name\\\":\\\"Java\\\"}\"))");
        System.out.println();
        System.out.println("  ⚠ 生产代码三件套：超时、重试、错误处理。HttpClient 不会自动帮你重试。");
    }

    // ==================== 6. 小结 ====================

    private static void summary() {
        section("6. 小结");
        System.out.println("1. 网络编程 = 字节流的读写 + 超时 + 关闭，三件事没做好就会「偶发卡死」。");
        System.out.println("2. 先想清楚协议：谁先发、发什么格式、怎么表示结束（换行？长度前缀？EOF？）。");
        System.out.println("3. 端口写 0 让系统分配，测试才不会被「端口被占用」坑。");
        System.out.println("4. 只要涉及 IO，就必须能超时、能被中断、能关掉。");
        System.out.println("5. Java 的 HTTP/2 异步客户端已经很强，小项目不用急着上第三方库。");
        System.out.println();
        System.out.println("练习：");
        System.out.println("  ① 把 echo 服务端改成「一问一答」协议：客户端发 N，服务端返回第 N 个斐波那契数。");
        System.out.println("  ② 用 ExecutorService 起 20 个客户端并发打服务端，观察线程池只有 3 个线程时会发生什么。");
        System.out.println("  ③ 给 HTTP 那段加上「失败重试 3 次，每次间隔翻倍（指数退避）」。");
        System.out.println("  ④ 用 HttpClient 请求一个返回 JSON 的公开接口，先用 split/正则把某个字段抠出来（后面第 12 课再上 JSON 库）。");
        System.out.println();
        System.out.println("下一课（第 10 课）：用 JDBC + H2 内存数据库做增删改查、事务和 SQL 注入演示。");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }
}
