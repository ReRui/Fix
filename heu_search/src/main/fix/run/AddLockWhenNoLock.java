package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.analyzefile.WhichVarToBeLocked;
import fix.io.AddLock;
import fix.io.InsertCode;

import java.util.List;

public class AddLockWhenNoLock {

    static String filePath = "D:\\FixExamples\\exportExamples\\account\\Account.java";
    static String packageName = "account";
    static String className = "Account";
    static String vatName = "amount";
    static int varLine = 28;
    public static void main(String[] args){
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
            AddLock.addLock(varLine,endLine + 1,lockName,filePath);
        }

    }
}
