package fix.analyzefile;

import fix.entity.ImportPath;
import fix.listener.CheckWhetherLockedListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class CheckWhetherLocked {


    //要寻找的变量的位置,形式必须是   包名/java文件：行数
    //"account/Account.java:32"
    public static void main(String[] args) {
        System.out.println(check("account/Account.java:24","amount", ImportPath.verifyPath + "\\generateClass"));
    }
    public static boolean check(String variableLoc, String variableName, String classpath) {
        String[] str = new String[]{
//                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\Patch",
                "+classpath=" + classpath,
                "+search.class=fix.search.SingleExecutionSearch",
                ImportPath.projectName + "." + ImportPath.mainClassName
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        CheckWhetherLockedListener checkWhetherLockedListener = new CheckWhetherLockedListener(ImportPath.examplesRootPath + "\\examples\\lock.txt", variableName, variableLoc);
        jpf.addListener(checkWhetherLockedListener);
//        LockListener lockListener = new LockListener(ImportPath.examplesRootPath + "\\examples\\lock.txt","test");
//        jpf.addListener(lockListener);
        jpf.run();
//        System.out.println(checkWhetherLockedListener.isCheckFlag());
        return checkWhetherLockedListener.isCheckFlag();
    }

}
