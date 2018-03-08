package fix;

import fix.analyzefile.AcquireVariableInSameLock;
import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.ExamplesIO;
import fix.io.InsertCode;
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
        String src = "int a = 0;";
        StringBuffer stringBuffer = new StringBuffer(src);
        String result = stringBuffer.insert(0,"lhr").toString();
        System.out.println(result);
        System.out.println("-----------");
        System.out.println(stringBuffer);
    }
}
