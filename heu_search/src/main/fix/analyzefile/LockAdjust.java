package fix.analyzefile;

import fix.entity.ImportPath;

import java.io.*;

//在添加两个同步的时候，万一两个同步交叉，要合并
public class LockAdjust {
    //单例
    private static LockAdjust instance = new LockAdjust();
    private LockAdjust() {

    }
    public static LockAdjust getInstance() {
        return instance;
    }

    String oneLockName = "";//第一次加的锁
    int oneFirstLoc = 0;//第一次加锁位置
    int oneLastLoc = 0;//第一次加锁位置

    String twoLockName = "";//第二次加的锁
    int twoFirstLoc = 0;//第二次加锁位置
    int twoLastLoc = 0;//第二次加锁位置

    boolean oneLockFinish = false;//第一次加锁是否完成

    int finalFirstLoc = 0;//合并后的位置
    int finalLastLoc = 0;//合并后的位置

    public String getOneLockName() {
        return oneLockName;
    }

    public void setOneLockName(String oneLockName) {
        this.oneLockName = oneLockName;
    }

    public int getOneFirstLoc() {
        return oneFirstLoc;
    }

    public void setOneFirstLoc(int oneFirstLoc) {
        this.oneFirstLoc = oneFirstLoc;
    }

    public int getOneLastLoc() {
        return oneLastLoc;
    }

    public void setOneLastLoc(int oneLastLoc) {
        this.oneLastLoc = oneLastLoc;
    }

    public String getTwoLockName() {
        return twoLockName;
    }

    public void setTwoLockName(String twoLockName) {
        this.twoLockName = twoLockName;
    }

    public int getTwoFirstLoc() {
        return twoFirstLoc;
    }

    public void setTwoFirstLoc(int twoFirstLoc) {
        this.twoFirstLoc = twoFirstLoc;
    }

    public int getTwoLastLoc() {
        return twoLastLoc;
    }

    public void setTwoLastLoc(int twoLastLoc) {
        this.twoLastLoc = twoLastLoc;
    }

    public boolean isOneLockFinish() {
        return oneLockFinish;
    }

    public void setOneLockFinish(boolean oneLockFinish) {
        this.oneLockFinish = oneLockFinish;
    }

    public int getFinalFirstLoc() {
        return finalFirstLoc;
    }

    public void setFinalFirstLoc(int finalFirstLoc) {
        this.finalFirstLoc = finalFirstLoc;
    }

    public int getFinalLastLoc() {
        return finalLastLoc;
    }

    public void setFinalLastLoc(int finalLastLoc) {
        this.finalLastLoc = finalLastLoc;
    }

    public static void main(String[] args) {
        LockAdjust la = new LockAdjust();
        la.setOneFirstLoc(356);
        la.setOneLastLoc(358);
        la.setTwoFirstLoc(357);
        la.setTwoLastLoc(358);
        la.setOneLockName("this");
        la.setTwoLockName("this");
        la.adjust("C:\\Users\\lhr\\Desktop\\a.java");
    }

    public void adjust(String filePath) {
        if (oneLockName.equals(twoLockName)) { //两次加锁相同
            if (cross()) {
                finalFirstLoc = Math.min(oneFirstLoc, twoFirstLoc);
                finalLastLoc = Math.max(oneLastLoc, twoLastLoc);
                deleteOldSync(filePath);//删除原有锁
//                addNewSync();
            }
        }
    }

    //删除原有锁
    private void deleteOldSync(String filePath) {
        String tempFile = ImportPath.tempFile;//临时文件的目录，不用太在意，反正用完就删
        FileToTempFile(filePath, tempFile);//将源文件修改后写入临时文件
        TempFileToFile(filePath, tempFile);//从临时文件写入
        deleteTempFile(tempFile);//删除临时文件
    }

    //原文件修改后写入临时文件
    private void FileToTempFile(String filePath, String tempFile) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        int line = 0;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
            bw = new BufferedWriter(new FileWriter(new File(tempFile)));
            String read = "";
            while (((read = br.readLine()) != null)) {
                line++;
                //删除第一个锁
                if (line == oneFirstLoc) {
                    int index = read.indexOf('{');
                    index++;
                    read = read.substring(index);
                }
                if (line == oneLastLoc) {
                    int index = read.indexOf('}');
                    index++;
                    read = read.substring(index);
                }

                //删除第二个锁
                if (line == twoFirstLoc) {
                    int index = read.indexOf('{');
                    index++;
                    read = read.substring(index);
                }
                if (line == twoLastLoc) {
                    int index = read.indexOf('}');
                    index++;
                    read = read.substring(index);
                }

                //添加合并后的锁
                //位置一定要在删除锁后面
                if (line == finalFirstLoc) {
                    StringBuilder sb = new StringBuilder(read);
                    sb.insert(0,"synchronized (" + oneLockName + "){ ");
                    read = sb.toString();
                }
                if(line == finalLastLoc) {
                    StringBuilder sb = new StringBuilder(read);
                    sb.insert(0,"}");
                    read = sb.toString();
                }
                bw.write(read);
                bw.write('\n');
                bw.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //从临时文件将修改后的内容再写入原文件
    private void TempFileToFile(String filePath, String tempFile) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(tempFile)), "UTF-8"));
            bw = new BufferedWriter(new FileWriter(new File(filePath)));
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
        } finally {
            try {
                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //删除临时文件
    private void deleteTempFile(String tempFile) {
        File file = new File(tempFile);
        file.delete();
    }

    //判断是不是交叉
    private boolean cross() {
        //先找不交叉的情况
        if (oneFirstLoc > twoLastLoc) {
            return false;
        }
        if (oneLastLoc < twoFirstLoc) {
            return false;
        }
        return true;
    }
}
