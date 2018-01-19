package fix;

import fix.io.CopyExamples;
import fix.run.CheckWhetherLocked;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class Test {



    public static void main(String[] args){
        CheckWhetherLocked checkWhetherLocked = new CheckWhetherLocked();
        checkWhetherLocked.check("1");
    }
}
