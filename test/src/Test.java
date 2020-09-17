package test;

import org.sweetchips.base.Hide;

public class Test {

    @Hide
    public Test() {
        this("default");
    }

    public Test(String msg) {
        System.out.println("init: " + msg);
    }

    @Hide
    public void test() {
        this.test("default");
    }

    public void test(String msg) {
        System.out.println("test: " + msg);
    }

}