package main;

import test.Test;

import org.sweetchips.base.Uncheckcast;

public class Main {

    @Uncheckcast
    public static void main(String[] args) {
        Test test = new Test().test();
    }
}