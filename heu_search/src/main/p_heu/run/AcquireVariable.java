
package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.listener.LockListener;

import java.util.Vector;

public class AcquireVariable {
    private static String filePath = "C:\\Users\\lhr\\Desktop\\lock.txt";
    private static String fieldName = "amount";//要寻找的参数名
    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch",
                "account.Main"
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        LockListener lockListener = new LockListener(filePath,fieldName);
        jpf.addListener(lockListener);
        jpf.run();
        Vector<String> oneLockfieldVector = lockListener.getOneLockfieldVector();
        for(String s : oneLockfieldVector)
            System.out.println("变量:" + s);
    }

}
