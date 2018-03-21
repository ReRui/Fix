package fix;

import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.ReadWriteNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
//        test(ImportPath.examplesRootPath + "\\exportExamples\\" + ImportPath.projectName + "\\IntRange.java");

        String s = "amount";
        Pattern p = Pattern.compile("(\\w+)\\.amount");
        Matcher m=p.matcher(s);
        while(m.find()) {
            System.out.println(m.group(1));
        }

    }

    //chanage file content to buffer array
    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                // append the content and the lost new line.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length() - 1, contents, 0);

            assert (contents.length > 0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return contents;
    }

    //用AST来判别是不是在一个函数中
    private static void test(String filePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(InfixExpression node) {
                System.out.println(node.toString());
                System.out.println(cu.getLineNumber(node.getStartPosition()));
                System.out.println("============");
                System.out.println(node.getParent());
                System.out.println("++++++++++++");
                return super.visit(node);
            }

        });
    }
}
