
package fix.run;

import fix.listener.LockListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Vector;

public class AcquireVariable {
    static String filePath = "";
    static String fieldName = "amount";//要寻找的参数名
    static {
        //读取桌面路径
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File com=fsv.getHomeDirectory();
        filePath = com.getPath() + "/lock.txt";
    }

    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=fix.search.SingleExecutionSearch",
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
