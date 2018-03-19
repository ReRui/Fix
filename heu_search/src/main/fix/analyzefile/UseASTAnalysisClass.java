package fix.analyzefile;

import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;
import p_heu.run.Unicorn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UseASTAnalysisClass {

    static String className = "";//类的名字，以后用来比较用
    static boolean flagMember = false;
    static boolean flagConstruct = false;

    public static void main(String[] args) {
       System.out.println(isConstructOrIsMemberVariable(11,12, ImportPath.examplesRootPath + "\\exportExamples\\" + ImportPath.projectName + "\\Account.java"));
    }

    public static boolean isConstructOrIsMemberVariable(int firstLoc, int lastLoc, String filePath) {
        useAST(firstLoc, lastLoc, filePath);
        return flagConstruct || flagMember;
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

    public static void useAST(int firstLoc, int lastLoc, String filePath) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        cu.accept(new ASTVisitor() {

            Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰

            public boolean visit(TypeDeclaration node) {
                className = node.getName().toString();
                return true;
            }

            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
                //判断是不是成员变量
                if(cu.getLineNumber(node.getStartPosition()) >= firstLoc && cu.getLineNumber(node.getStartPosition()) <= lastLoc){
                    flagMember = isMemberVariable(node);
                }
                SimpleName name = node.getName();
//                System.out.println(isMemberVariable(node) + "test");
                this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
//                    System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                    //判断是不是构造函数
                    if(cu.getLineNumber(node.getStartPosition()) >= firstLoc && cu.getLineNumber(node.getStartPosition()) <= lastLoc){
                        flagConstruct =  isConstruct(node);
                    }
                }
                return true;
            }
        });
    }

    //判断是不是成员变量
    private static boolean isMemberVariable(ASTNode node) {
        if(node.getParent().getParent() instanceof TypeDeclaration) {
            return true;
        }else{
            return false;
        }
    }

    //判断是不是构造函数
    private static boolean isConstruct(ASTNode node) {
        /**
         * 检查结点的所有父节点，看看有没有一个是构造函数
         * 判断节点类型是函数，节点名字与类名相同，则是构造函数
         */
        while (!(node instanceof TypeDeclaration)) {
            if ((node instanceof MethodDeclaration) && (((MethodDeclaration) node).getName().toString().equals(className))) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }


}
