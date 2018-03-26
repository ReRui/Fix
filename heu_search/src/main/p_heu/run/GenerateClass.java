package p_heu.run;

import fix.entity.ImportPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateClass {
    public static void main(String[] args) {
        compileJava(ImportPath.verifyPath + "\\exportExamples\\" + ImportPath.projectName, ImportPath.verifyPath + "\\generateClass");
    }

    public static void compileJava(String dirPath, String desk) {
        //遍历该目录下的所有java文件
        File file = new File(dirPath);
        File[] fileArr = file.listFiles();
        List<String> fileList = new ArrayList<String>();

        for (File f : fileArr) {
            fileList.add(f.toString());
        }
        try {
            compile(fileList, desk);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void compile(List<String> files, String dest) throws IOException, InterruptedException {
        com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();

//        String[] cpargs = new String[] {"-d", dest, files};
        String[] cpargs = new String[files.size() + 2];
        cpargs[0] = "-d";
        cpargs[1] = dest;
        for (int i = 2; i < cpargs.length; ++i) {
            cpargs[i] = files.get(i - 2);
        }
        int status = javac.compile(cpargs);

        //输出
        /*if(status!=0){
            System.out.println("fail");
        }
        else {
            System.out.println("success");
        }*/
    }
}
