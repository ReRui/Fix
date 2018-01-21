package fix.run;

import fix.entity.ImportPath;
import fix.listener.CheckWhetherLockedListener;
import fix.listener.LockListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class CheckWhetherLocked {

    //要寻找的变量的位置,形式必须是   包名/java文件：行数
    //"account/Account.java:32"

    public void check(String variableLoc){
        variableLoc = "test/Test.java:12";
        String[] str = new String[]{
                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                "+search.class=fix.search.SingleExecutionSearch",
//                ImportPath.projectName + "." + ImportPath.mainClassName
                "test.Test"
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        CheckWhetherLockedListener checkWhetherLockedListener = new CheckWhetherLockedListener(ImportPath.examplesRootPath + "\\examples\\lock.txt","x",variableLoc);
        jpf.addListener(checkWhetherLockedListener);
//        LockListener lockListener = new LockListener(ImportPath.examplesRootPath + "\\examples\\lock.txt","test");
//        jpf.addListener(lockListener);
        jpf.run();
        System.out.print(checkWhetherLockedListener.isCheckFlag());
    }
}
