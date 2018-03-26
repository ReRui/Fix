package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.analyzefile.WhichVarToBeLocked;
import fix.entity.ImportPath;
import fix.io.ExamplesIO;

import java.util.List;

public class AddLockWhenNoLock {

    static String packageName = "account";
    static String className = "Account";
    static String filePath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\" + className + ".java";
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;
    static String vatName = "amount";
    static int varLine = 28;
    public static void main(String[] args){/*
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //variableLoc   包名/java文件:行数    必须类似   account/Account.java:32
        String variableLoc = packageName + "/" + className + ".java:" + String.valueOf(varLine);

        boolean whetherLocked = checkWhetherLocked.check(variableLoc,vatName);
        //没加锁，则给它加上同步锁
        if(!whetherLocked){
            WhichVarToBeLocked whichVarToBeLocked = new WhichVarToBeLocked(vatName,varLine);
            //获取要加锁的变量的信息
            List result = whichVarToBeLocked.searchWhich(filePath);
            String lockName = (String) result.get(0);
            int endLine = (int) result.get(1);
            //加锁
            ExamplesIO examplesIO = ExamplesIO.getInstance();
            dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples","exportExamples",dirPath);
            examplesIO.addLockToOneVar(varLine,endLine + 1,lockName,dirPath + "\\" + className + ".java");
        }
*/
    }
}
