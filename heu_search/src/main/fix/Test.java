package fix;

import java.io.*;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
        String s = "abcc}";
        int index = s.indexOf('}');
        s = s.substring(0,index) + s.substring(index + 1);
        System.out.println(index);
        System.out.println(s);
    }
}
