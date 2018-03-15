package fix;

import fix.entity.ImportPath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Test {
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args) throws IOException {
        long timeStart = System.currentTimeMillis();
        File file = new File(dirPath + "//account.java");//文件路径
        FileReader fileReader = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(fileReader);
        int number = 24;//设置指定行数
        String txt = "";
        int lines = 0;

        //锁
        int syncLine = 0;
        String syncName = "";
        while (txt != null) {
            lines++;
            txt = reader.readLine();
            String s = txt;
            if(txt.contains("synchronized") && lines > syncLine){
                syncLine = lines;
                syncName = txt;
            }
            if (lines == number) {
                System.out.println("第" + reader.getLineNumber() + "的内容是：" + txt + "\n");
                System.out.println("锁行数" + syncLine + ",锁信息" + syncName + "\n");
                acquireName(syncName);
                long timeEnd = System.currentTimeMillis();
                System.out.println("总共花费：" + (timeEnd - timeStart) + "ms");
                System.exit(0);
            }
        }
        reader.close();
        fileReader.close();
    }

    private static void acquireName(String syncName) {
        String name = "";
        int index = syncName.indexOf("synchronized") + 12;

        if((syncName.charAt(index) != '(') && (syncName.charAt(index + 1) != '(')){
            name = "this";
        }else{
            if(syncName.charAt(index) == '(')
                syncName = syncName.substring(index);
            else
                syncName = syncName.substring(index + 1);

            int ss = syncName.indexOf(')');
            name = syncName.substring(1,ss);
        }
        System.out.println(name);

    }
}
