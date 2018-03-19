package fix;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
       String s = "account/Account.java:11";
       System.out.println(s.split(":")[0].split("/")[1]);
    }

}
