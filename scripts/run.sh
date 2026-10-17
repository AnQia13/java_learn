#!/usr/bin/env bash
#
# 运行任意一课，自动把 Maven 依赖（比如第 10 课的 H2 数据库）带上 classpath。
#
# 用法：
#   ./scripts/run.sh com.hczn.learn.Lesson01Basics
#   ./scripts/run.sh com.hczn.learn.Lesson10Jdbc
#
# 为什么不直接 java -cp target/classes ... ？
#   因为那样只有自己的类，没有第三方 jar。Lesson10 需要 H2 驱动，
#   手工拼 classpath 又长又容易错，所以让 Maven 帮我们算出来、缓存起来。
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 1 ]; then
    echo "用法: $0 <全限定类名> [参数...]" >&2
    exit 1
fi

MAIN_CLASS="$1"
shift

./mvnw -q compile

CP_FILE="target/classpath.txt"
if [ ! -f "$CP_FILE" ] || [ pom.xml -nt "$CP_FILE" ]; then
    echo "解析依赖 classpath（首次或 pom 变更后才会重算）..." >&2
    ./mvnw -q dependency:build-classpath \
        -Dmdep.outputFile="$CP_FILE" \
        -Dmdep.includeScope=runtime
fi

java -cp "target/classes:$(cat "$CP_FILE")" "$MAIN_CLASS" "$@"
