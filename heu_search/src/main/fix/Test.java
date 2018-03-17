package fix;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {

    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args) throws IOException {
        List list = new ArrayList();
        list.add(1);
        System.out.println(list.contains(2));
    }

}
