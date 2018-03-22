package fix;

import p_heu.entity.ReadWriteNode;

import java.io.*;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
        tt();
        if(tt()){
            System.out.println(123);
        }
    }

    private static boolean tt() {
        return true;
    }
}
