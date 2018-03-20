package fix;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
       List l = new ArrayList();
       l.add(1);
       l.add(2);
       l.add(3);
       l.add(4);
       l.add(5);
       l.add(6);
       for(int  i = 0;i <l.size(); i++){
           l.remove(i);
           i--;
           System.out.println(l.size());
       }
    }

}
