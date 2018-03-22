package fix;

import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.ReadWriteNode;

import java.io.*;

public class Test {

    static String className = "";//类的名字，以后用来比较用

    public static void main(String[] args) {
        useASTAssertSameFun(ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\SetCheck.java");
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

    private static void useASTAssertSameFun(String filePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            // Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰

            public boolean visit(TypeDeclaration node) {
                className = node.getName().toString();
                return true;
            }

            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
                //判断是不是成员变量

                return true; // do not continue to avoid usage info
            }

            @Override
            public boolean visit(ReturnStatement node) {
                System.out.println(node);
                return super.visit(node);
            }

            @Override
            public boolean visit(InfixExpression node) {

                return super.visit(node);
            }

            //变量
            public boolean visit(SimpleName node) {
                System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                return true;
            }
        });
    }
}
