package test;

import main.Main;

import org.sweetchips.annotation.Hide;

import org.sweetchips.annotation.Uncheckcast;

public class Test extends Main {

    @Hide
    public Test() {
        this("default");
    }

    public Test(String msg) {
        System.out.println("init: " + msg);
    }

    @Hide
    @Uncheckcast({Test.class, Main.class})
    public Test test() {
        Test test = new Test();
        Object object = test;
        Main main = (Main) object;
        ((Test) main).test("default");
        return (Test) main;
    }

    public void test(String msg) {
        System.out.println("test: " + msg);
    }
}