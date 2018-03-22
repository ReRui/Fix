package fix.analyzefile;

import java.io.*;

//在添加两个同步的时候，万一两个同步交叉，要合并
public class LockAdjust {
    String oneLockName = "";//第一次加的锁
    int oneFirstLoc = 0;//第一次加锁位置
    int oneLastLoc = 0;//第一次加锁位置

    String twoLockName = "";//第二次加的锁
    int twoFirstLoc = 0;//第二次加锁位置
    int twoLastLoc = 0;//第二次加锁位置

    boolean oneLockFinish = false;//第一次加锁是否完成

    int finaFirstLoc = 0;//合并后的位置
    int finaLastLoc = 0;//合并后的位置

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

    public int getFinaFirstLoc() {
        return finaFirstLoc;
    }

    public void setFinaFirstLoc(int finaFirstLoc) {
        this.finaFirstLoc = finaFirstLoc;
    }

    public int getFinaLastLoc() {
        return finaLastLoc;
    }

    public void setFinaLastLoc(int finaLastLoc) {
        this.finaLastLoc = finaLastLoc;
    }

    public static void main(String[] args) {
        LockAdjust la = new LockAdjust();
        la.setOneFirstLoc(356);
        la.setOneLastLoc(358);
        la.setTwoFirstLoc(357);
        la.setTwoLastLoc(358);
        la.adjust("C:\\Users\\lhr\\Desktop\\a.java");
    }
    public  void adjust(String filePath) {
        if (oneLockName.equals(twoLockName)) { //两次加锁相同
            if (cross()) {
                finaFirstLoc = Math.min(oneFirstLoc, twoFirstLoc);
                finaLastLoc = Math.max(oneLastLoc, twoLastLoc);
                deleteOldSync(oneFirstLoc, oneLastLoc, twoFirstLoc, twoLastLoc, filePath);
            }
        }
    }

    private void deleteOldSync(int oneFirstLoc, int oneLastLoc, int twoFirstLoc, int twoLastLoc, String filePath) {
        String tempFile = "C:\\Users\\lhr\\Desktop\\i.java";
        FileToTempFile(oneFirstLoc, oneLastLoc, twoFirstLoc, twoLastLoc, filePath, tempFile);//将源文件修改后写入临时文件
        TempFileToFile(filePath, tempFile);//从临时文件写入
    }

    private void FileToTempFile(int oneFirstLoc, int oneLastLoc, int twoFirstLoc, int twoLastLoc, String filePath, String tempFile) {
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
