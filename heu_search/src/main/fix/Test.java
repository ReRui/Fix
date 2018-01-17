package fix;

import fix.io.CopyExamples;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class Test {
    public static void main(String[] args){

//        CopyExamples.createDirectory("C:\\Users\\lhr\\Desktop\\Fix\\heu_search\\src\\exportExamples\\test");
        CopyExamples.CopyFile("C:\\Users\\lhr\\Desktop\\Fix\\heu_search\\src\\examples\\account\\Account.java","C:\\Users\\lhr\\Desktop\\Fix\\heu_search\\src\\exportExamples\\account\\Account.java");
    }
}
