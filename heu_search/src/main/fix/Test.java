package fix;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
        String s = "asdfasdf ac.amount+=mn";
        String result = "";
        String[] res = s.split("\\.");
        if (res.length > 1) {
            String temp = res[0];
            int index = 0;
            for (int i = temp.length() - 1; i >= 0; i--) {
                if (!((temp.charAt(i) >= 'a' && temp.charAt(i) <= 'z') || (temp.charAt(i) >= 'A' && temp.charAt(i) <= 'Z'))) {
                    index = i;
                }
            }
            result = res[0].substring(index);
        } else {
            result = "this";
        }
        System.out.println(result);
    }

}
