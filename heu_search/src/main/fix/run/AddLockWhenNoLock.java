package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.analyzefile.WhichVarToBeLocked;
import fix.io.AddLock;
import fix.io.InsertCode;

public class AddLockWhenNoLock {

    static String  filePath = "D:\\FixExamples\\exportExamples\\account\\Account.java";
    static String packageName = "account";
    static String className = "Account";
    static String vatName = "amount";
    static int varLine = 28;
    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //variableLoc   包名/java文件:行数    必须类似   account/Account.java:32
        String variableLoc = packageName + "/" + className + ".java:" + String.valueOf(varLine);

        boolean whetherLocked = checkWhetherLocked.check(variableLoc,vatName,variableLoc,vatName);
        //没加锁，则给它加上同步锁
        if(!whetherLocked){
            WhichVarToBeLocked whichVarToBeLocked = new WhichVarToBeLocked(vatName,varLine);
            String lockName = whichVarToBeLocked.searchWhich(filePath);
            System.out.println("====" + lockName);
            AddLock.addLock(28,29,lockName,filePath);
        }

    }
}
