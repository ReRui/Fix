package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.analyzefile.WhichVarToBeLocked;
import fix.io.InsertCode;

public class AddLockWhenNoLock {

    static String  filePath = "D:\\FixExamples\\exportExamples\\account\\Account.java";
    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //variableLoc   包名/java文件:行数    必须类似   account/Account.java:32
        boolean whetherLocked = checkWhetherLocked.check("account/Account.java:28","amount","account/Account.java:28","amount");
        if(!whetherLocked){
            WhichVarToBeLocked whichVarToBeLocked = new WhichVarToBeLocked("amount",28);
            String lockName = whichVarToBeLocked.searchWhich(filePath);
            System.out.println("====" + lockName);
            InsertCode.insert(28, " synchronized (" + lockName +"){ ", filePath);
            InsertCode.insert(29, " }", filePath);
        }

    }
}
