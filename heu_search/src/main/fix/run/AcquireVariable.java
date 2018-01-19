
package fix.run;

import fix.entity.ImportPath;
import fix.listener.LockListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.util.Vector;

public class AcquireVariable {

    private static Vector<String> oneLockfieldVector = new Vector<String>();
    public static void main(String[] args) {
        String[] str = new String[]{
//                "+classpath=D:\\FixExamples\\out\\production\\FixExamples",
                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                "+search.class=fix.search.SingleExecutionSearch",
                ImportPath.projectName + "." + ImportPath.mainClassName
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        LockListener lockListener = new LockListener(ImportPath.examplesRootPath + "\\examples\\lock.txt",ImportPath.parametersName);
        jpf.addListener(lockListener);
        jpf.run();
        oneLockfieldVector = lockListener.getOneLockfieldVector();
        for(String s : oneLockfieldVector)
            System.out.println("变量:" + s);
    }

    public Vector<String> getOneLockfieldVector() {
        String[] str = new String[]{
                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                "+search.class=fix.search.SingleExecutionSearch",
                ImportPath.projectName + "." + ImportPath.mainClassName
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        LockListener lockListener = new LockListener(ImportPath.examplesRootPath + "\\examples\\lock.txt",ImportPath.parametersName);
        jpf.addListener(lockListener);
        jpf.run();
        oneLockfieldVector = lockListener.getOneLockfieldVector();
        for(String s : oneLockfieldVector)
            System.out.println("变量:" + s);
        return oneLockfieldVector;
    }
}
