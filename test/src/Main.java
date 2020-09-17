package main;

import test.Test;

import org.sweetchips.base.Uncheckcast;

public class Main {

    @Uncheckcast
    public static void main(String[] args) {
        new Test().test();
        ((Test) new Object()).test();
    }

}