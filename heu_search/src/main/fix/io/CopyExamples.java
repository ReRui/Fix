package fix.io;

import java.io.*;

public class CopyExamples {

    public static void createDirectory(String dirpath){
        File dir = new File(dirpath);
        dir.mkdirs();
    }

    public static void CopyFile(String filepath,String copyFilepath){
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath)),"UTF-8"));
            bw = new BufferedWriter(new FileWriter(new File(copyFilepath)));
            String read = "";
            while (((read = br.readLine()) != null)) {
                bw.write(read);
                bw.write('\n');
                bw.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
