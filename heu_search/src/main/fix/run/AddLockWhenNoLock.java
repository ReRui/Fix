package fix.run;

import fix.analyzefile.CheckWhetherLocked;
import fix.io.InsertCode;

public class AddLockWhenNoLock {

    static String  filePath = "D:\\FixExamples\\examples\\test\\Test.java";
    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        //variableLoc   包名/java文件:行数    必须类似   account/Account.java:32
        boolean whetherLocked = checkWhetherLocked.check("test/Test.java:12","x","test/Test.java:13","s");
        /*if(!whetherLocked){
            InsertCode.insert(22, "ReentrantLock lock" + 11 +" = new ReentrantLock(true);lock" + 11 + ".lock();", filePath);
            InsertCode.insert(23, "lock" + 11 + ".unlock();", filePath);
        }*/
    }
}
