package fix;

import java.io.*;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
        BufferedReader br = null;
        String read = "";//用来读
        int line = 0;//记录当前读到那一行

        String filePath = "C:\\Users\\lhr\\Desktop\\a.java";
        String other = "C:\\Users\\lhr\\Desktop\\i.java";
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
            bw = new BufferedWriter(new FileWriter(new File(other)));

            while (((read = br.readLine()) != null)) {
                line++;

                if (line == 356) {
                    int index = read.indexOf('{');
                    index++;
                    read = read.substring(index);
                }
                if(line == 358) {
                    int index = read.indexOf('}');
                    index++;
                    read = read.substring(index);
                }

                bw.write(read);
                bw.write('\n');
                bw.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
