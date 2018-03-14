package fix;

import fix.analyzefile.AcquireVariableInSameLock;
import fix.analyzefile.CheckWhetherLocked;
import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.ExamplesIO;
import fix.io.InsertCode;
import fix.listener.CheckWhetherLockedListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Test {
    public static void main(String[] args){
        String[] str = new String[]{
                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                "+search.class=fix.search.SingleExecutionSearch",
                ImportPath.projectName + "." + ImportPath.mainClassName
//                "test.Test"
        };
        String variableName = "amount";
        String variableLoc = "account/Account.java:32";

        Config config = new Config(str);
        JPF jpf = new JPF(config);
        CheckWhetherLockedListener checkWhetherLockedListener = new CheckWhetherLockedListener(ImportPath.examplesRootPath + "\\examples\\lock.txt",variableName,variableLoc);
        jpf.addListener(checkWhetherLockedListener);
//        LockListener lockListener = new LockListener(ImportPath.examplesRootPath + "\\examples\\lock.txt","test");
//        jpf.addListener(lockListener);
        jpf.run();
        System.out.println(checkWhetherLockedListener.isCheckFlag());
        System.out.println(checkWhetherLockedListener.getProtectLockName());


//        System.out.println("true?" + CheckWhetherLocked.check("amount","account/Account.java:32"));
    }
}
