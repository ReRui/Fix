package fix;

import fix.io.CopyExamples;
import fix.run.CheckWhetherLocked;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        checkWhetherLocked.check("1");
        /*Vector<String> v = new Vector<String>();
        v.add("a");
        v.remove("a");
        for (Object o : v)
            System.out.println(o);*/
    }
}
