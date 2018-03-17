package fix;

import fix.entity.ImportPath;

import java.io.*;

public class Test {

    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args) throws IOException {
//        System.out.println(acquireLockName("account/Account.java:28"));
        String s = "      ac";
        System.out.println(s);
        s =s.trim();
        System.out.println(s);
    }

    //读到那一行，然后对字符串处理
    private static String acquireLockName(String position) {
        BufferedReader br = null;
        String read = "";//用来读
        String result = "";//用来处理
        int line = 0;
        int poi = Integer.parseInt(position.split(":")[1]);
        System.out.println(poi + "poi");
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dirPath + "\\Account.java")),"UTF-8"));
            while (((read = br.readLine()) != null)) {
                line++;
                if(line == poi){//找到哪一行
                    String[] res = read.split("\\.");
                    System.out.println("分完情况");
                    for(String s : res)
                        System.out.println(s + "fenwan");
                    if(res.length > 1) {
                        result = res[0];
                    }
                    else{
                        result = "this";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


}
