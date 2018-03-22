package fix;

import java.io.*;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
        String s = "abcc";
        StringBuilder sb= new StringBuilder(s);
        sb.insert(0,"syaskjdaslkjd(){}");
        s = sb.toString();
        System.out.println(s);
        System.out.println(sb);
    }
}
