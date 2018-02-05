package fix;

import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.InsertCode;
import fix.listener.LockListener;
import fix.listener.TestListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Test {



    public static void main(String[] args){

        String[] str = new String[]{
                "+classpath=" + ImportPath.examplesRootPath + "\\out\\production\\FixExamples",
                "+search.class=fix.search.SingleExecutionSearch",
                ImportPath.projectName + "." + ImportPath.mainClassName
        };
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        TestListener testListener = new TestListener();
        jpf.addListener(testListener);
        jpf.run();
    }


}
