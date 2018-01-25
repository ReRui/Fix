package fix;

import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.InsertCode;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Test {

    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;
    static Set<String> variableVector = new HashSet<String>();
    //chanage file content to buffer array
    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while((line = br.readLine()) != null) {
                // append the content and the lost new line.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length()-1, contents, 0);

            assert(contents.length > 0);
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return contents;
    }

    public static void main(String[] args){
        lock("D:\\FixExamples\\examples\\account\\Account.java");
    }


    public static void lock(String filePath) {
        MatchVariable matchVariable = new MatchVariable();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        cu.accept(new ASTVisitor() {

            Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰

            public boolean visit(TypeDeclaration  node){
                System.out.println(node.getName());
                return true;
            }


            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
                    /*System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                    System.out.println("+=========================");
//                    System.out.println(node.isDeclaration());
                    System.out.println(node.getParent().getParent().getParent().getParent());
                    if(node.getParent().getParent().getParent().getParent() instanceof TypeDeclaration  )
                        System.out.println("yes");*/
                }
                return true;
            }


        });
    }

}
