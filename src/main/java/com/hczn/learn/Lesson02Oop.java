package com.hczn.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 第 2 课：面向对象。
 *
 * <p>运行：
 * <pre>
 *   javac -d target/classes $(find src/main/java -name '*.java')
 *   java -cp target/classes com.hczn.learn.Lesson02Oop
 * </pre>
 *
 * <p>本课覆盖：类与对象、封装、继承、多态、抽象类、接口、内部类、
 * equals/hashCode/toString、枚举。
 */
public class Lesson02Oop {

    public static void main(String[] args) {
        classesAndObjects();
        encapsulation();
        inheritanceAndPolymorphism();
        abstractAndInterface();
        innerClasses();
        equalsHashCode();
        enums();
    }

    // ==================== 1. 类与对象 ====================

    private static void classesAndObjects() {
        section("1. 类与对象");

        // 用构造器创建对象
        Student s1 = new Student("小明", 18);
        Student s2 = new Student("小红");          // 重载的构造器，默认年龄

        System.out.println(s1);
        System.out.println(s2);

        // 对象是引用类型，两个变量可以指向同一个对象
        Student s3 = s1;
        s3.setAge(19);
        System.out.println("改 s3 后 s1.getAge() = " + s1.getAge() + "（同一个对象）");
    }

    // ==================== 2. 封装 ====================

    private static void encapsulation() {
        section("2. 封装");

        Account account = new Account("张三", 100);
        account.deposit(50);
        account.withdraw(30);
        // account.balance = 9999;    // 编译错误：balance 是 private，只能通过方法改
        System.out.println("余额 = " + account.getBalance());

        try {
            account.withdraw(10000);   // 让类的内部逻辑守住规则
        } catch (IllegalStateException e) {
            System.out.println("取款失败: " + e.getMessage());
        }

        // static 属于类，不属于对象
        System.out.println("已创建账户数 = " + Account.getCount());
        System.out.println("常量 RATE = " + Account.RATE);
    }

    // ==================== 3. 继承与多态 ====================

    private static void inheritanceAndPolymorphism() {
        section("3. 继承与多态");

        // 父类引用指向子类对象 → 多态
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(2));
        shapes.add(new Rectangle(3, 4));
        shapes.add(new Circle(1));

        double total = 0;
        for (Shape shape : shapes) {
            // 运行时才决定调用哪个 area()，这叫动态绑定
            System.out.printf("%s 面积 = %.2f%n", shape.getName(), shape.area());
            total += shape.area();
        }
        System.out.printf("总面积 = %.2f%n", total);

        // instanceof 判断真实类型（Java 16+ 可写 instanceof Circle c 直接绑定变量）
        for (Shape shape : shapes) {
            if (shape instanceof Circle) {
                Circle c = (Circle) shape;      // 向下转型
                System.out.println("这是圆，半径 = " + c.getRadius());
            }
        }

        // 父类的构造器会被 super(...) 调用
        Dog dog = new Dog("旺财");
        dog.eat();       // 继承自 Animal
        dog.speak();     // 子类重写
    }

    // ==================== 4. 抽象类与接口 ====================

    private static void abstractAndInterface() {
        section("4. 抽象类与接口");

        // 抽象类不能 new，但可以有字段和已实现的方法
        Shape shape = new Circle(5);
        System.out.println("抽象类引用: " + shape.describe());

        // 接口定义契约，一个类可以实现多个接口
        Flyable bird = new Bird();
        Swimmable fish = new Fish();
        bird.fly();
        fish.swim();
        // 接口的 default 方法
        System.out.println("default 方法: " + bird.status());
    }

    // ==================== 5. 内部类 ====================

    private static void innerClasses() {
        section("5. 内部类");

        // 成员内部类：需要先有外部类对象
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        inner.show();

        // 静态内部类：不依赖外部类对象
        Outer.StaticInner staticInner = new Outer.StaticInner();
        staticInner.show();

        // 匿名内部类：只用一个方法的接口/抽象类，适合一次性使用
        Greeting greeting = new Greeting() {
            @Override
            public String hello(String name) {
                return "匿名类说：你好 " + name;
            }
        };
        System.out.println(greeting.hello("Java"));

        // 匿名类其实就是 Lambda 的前身（见第 13 课）
        Greeting lambda = name -> "Lambda 说：你好 " + name;
        System.out.println(lambda.hello("Java"));
    }

    // ==================== 6. equals / hashCode ====================

    private static void equalsHashCode() {
        section("6. equals / hashCode");

        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);

        System.out.println("p1 == p2 : " + (p1 == p2));            // false，不同对象
        System.out.println("p1.equals(p2) : " + p1.equals(p2));    // true，内容相同
        System.out.println("hashCode 相同: " + (p1.hashCode() == p2.hashCode()));

        // 极端反例：如果只重写 equals 不重写 hashCode，HashSet 会认为它们是两个不同元素
        System.out.println("contains(p2) = " + new java.util.HashSet<>(List.of(p1)).contains(p2));
    }

    // ==================== 7. 枚举 ====================

    private static void enums() {
        section("7. 枚举");

        // 枚举是类型安全的常量，比 "int 常量 + 注释" 好得多
        for (Weekday day : Weekday.values()) {
            System.out.println(day + " → 序号 " + day.ordinal() + "，中文 " + day.getLabel()
                    + "，是否周末 " + day.isWeekend());
        }

        Weekday day = Weekday.valueOf("SATURDAY");
        System.out.println("valueOf 得到: " + day.getLabel());
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }

    // ==================================================================
    // 下面都是配套的示例类型。放在同一个文件里是为了方便对照学习，
    // 实际项目中「一个公开类一个文件」。
    // ==================================================================

    /** 学生：演示构造器、封装、toString。 */
    static class Student {
        private final String name;   // final 字段只能在构造器里赋值一次
        private int age;

        Student(String name, int age) {
            this.name = name;        // this 用来区分字段和参数
            this.age = age;
        }

        Student(String name) {
            this(name, 6);           // 构造器链：调用另一个构造器
        }

        int getAge() {
            return age;
        }

        void setAge(int age) {
            if (age < 0) {
                throw new IllegalArgumentException("年龄不能为负: " + age);
            }
            this.age = age;
        }

        @Override
        public String toString() {
            return "Student{name='" + name + "', age=" + age + "}";
        }
    }

    /** 账户：演示封装与 static。 */
    static class Account {
        static final double RATE = 0.03;      // 常量：static + final
        private static int count = 0;         // 类变量：所有对象共享

        private final String owner;
        private double balance;

        Account(String owner, double balance) {
            this.owner = owner;
            this.balance = balance;
            count++;
        }

        static int getCount() {
            return count;
        }

        void deposit(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("存款必须为正数");
            }
            balance += amount;
        }

        void withdraw(double amount) {
            if (amount > balance) {
                throw new IllegalStateException("余额不足，当前 " + balance);
            }
            balance -= amount;
        }

        double getBalance() {
            return balance;
        }
    }

    /** 形状：抽象类，有字段和自己实现的方法。 */
    abstract static class Shape {
        private final String name;

        Shape(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        /** 子类必须实现 */
        abstract double area();

        /** 子类可以直接用，也可以重写 */
        String describe() {
            return name + " 的周长/面积由子类决定，面积=" + area();
        }
    }

    static class Circle extends Shape {
        private final double radius;

        Circle(double radius) {
            super("圆形");               // 调用父类构造器，必须放在第一行
            this.radius = radius;
        }

        double getRadius() {
            return radius;
        }

        @Override
        double area() {
            return Math.PI * radius * radius;
        }
    }

    static class Rectangle extends Shape {
        private final double width;
        private final double height;

        Rectangle(double width, double height) {
            super("矩形");
            this.width = width;
            this.height = height;
        }

        @Override
        double area() {
            return width * height;
        }
    }

    static class Animal {
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        void eat() {
            System.out.println(getName() + " 在吃东西");
        }

        void speak() {
            System.out.println(getName() + " 发出声音");
        }
    }

    static class Dog extends Animal {
        Dog(String name) {
            super(name);
        }

        @Override
        void speak() {
            System.out.println(getName() + " 汪汪叫");
        }
    }

    interface Flyable {
        void fly();

        /** default 方法：接口也可以有实现（Java 8+） */
        default String status() {
            return "可以飞";
        }
    }

    interface Swimmable {
        void swim();
    }

    static class Bird implements Flyable {
        @Override
        public void fly() {
            System.out.println("小鸟在飞");
        }
    }

    static class Fish implements Swimmable {
        @Override
        public void swim() {
            System.out.println("小鱼在游");
        }
    }

    /** 外部类：演示成员内部类与静态内部类。 */
    static class Outer {
        private final String outerField = "外部字段";

        class Inner {
            void show() {
                // 成员内部类可以直接访问外部类实例的字段
                System.out.println("成员内部类读到: " + outerField);
            }
        }

        static class StaticInner {
            void show() {
                // 静态内部类不能直接访问外部类的实例字段
                System.out.println("静态内部类：不依赖外部类对象");
            }
        }
    }

    /** 只有一个抽象方法的接口 → 函数式接口，可以写成 Lambda。 */
    interface Greeting {
        String hello(String name);
    }

    /** 坐标点：演示 equals / hashCode。 */
    static class Point {
        private final int x;
        private final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;                       // 同一引用直接相等
            }
            if (o == null || getClass() != o.getClass()) {
                return false;                      // 类型不同
            }
            Point point = (Point) o;
            return x == point.x && y == point.y;   // 比较内容
        }

        @Override
        public int hashCode() {
            // 用参与 equals 的字段一起计算，保证「equals 相等 → hashCode 相等」
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }

    /** 带字段和方法构造器的枚举。 */
    enum Weekday {
        MONDAY("周一"), TUESDAY("周二"), WEDNESDAY("周三"),
        THURSDAY("周四"), FRIDAY("周五"), SATURDAY("周六"), SUNDAY("周日");

        private final String label;

        Weekday(String label) {      // 枚举构造器只能是 private
            this.label = label;
        }

        String getLabel() {
            return label;
        }

        boolean isWeekend() {
            return this == SATURDAY || this == SUNDAY;
        }
    }
}
