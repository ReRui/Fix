package fix;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
        List list = new ArrayList();
        list.add(1);
        list.add(2);
        System.out.println(list.indexOf(2));
        System.out.println(list.size());
    }

}
