#!/usr/bin/env bash
#
# 运行 JMH 微基准测试（第 11 课配套）。
#
# 用法：
#   ./scripts/benchmark.sh                    # 跑全部基准
#   ./scripts/benchmark.sh ".*TwoSum.*"       # 只跑名字匹配的基准
#
# JMH 会先预热再测量、自动 fork 独立 JVM、防止死代码消除，
# 所以它测出来的数字才可信 —— 手写 System.nanoTime 的计时只能看个大概。
set -euo pipefail

cd "$(dirname "$0")/.."

FILTER="${1:-.*}"

./mvnw -q -Pbenchmarks test-compile

CP_FILE="target/benchmark-classpath.txt"
if [ ! -f "$CP_FILE" ] || [ pom.xml -nt "$CP_FILE" ]; then
    echo "解析依赖 classpath..." >&2
    ./mvnw -q -Pbenchmarks dependency:build-classpath \
        -Dmdep.outputFile="$CP_FILE" \
        -Dmdep.includeScope=test
fi

java -cp "target/test-classes:target/classes:$(cat "$CP_FILE")" \
    org.openjdk.jmh.Main "$FILTER"
