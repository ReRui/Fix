package fix.io;

import fix.analyzefile.AcquireVariableInSameLock;

import java.io.*;
import java.util.Vector;

public class ExamplesIO {

    //单例模式
    private static ExamplesIO examplesIO = new ExamplesIO();
    private ExamplesIO(){};
    public static ExamplesIO getInstance(){
        return examplesIO;
    }

    public void addLockToOneVar(int startLine, int endLine, String lockName,String filePath){
        InsertCode.insert(startLine, "synchronized (" + lockName +"){ ", filePath);
        InsertCode.insert(endLine, "}", filePath);
    }

    public String copyFromOneDirToAnotherAndChangeFilePath(String dir, String targetDir, String filePath){
        String changeFilePath = "";
        File file = new File(filePath);
        File[] fileArr = file.listFiles();
        //先创建一个copy目录
        changeFilePath = filePath.replaceAll(dir,targetDir);
        createDirectory(changeFilePath);


        for(File f : fileArr){
            //每个文件，拷贝到另一个目录下。
            String copyFile = f.getPath().replaceAll(dir,targetDir);
            CopyFile(f.getPath(),copyFile);
        }

        return changeFilePath;
    }

    private void createDirectory(String dirpath){
        File dir = new File(dirpath);
        dir.mkdirs();
    }

    private void CopyFile(String filepath,String copyFilepath){
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
