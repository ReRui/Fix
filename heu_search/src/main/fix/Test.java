package fix;

import fix.entity.ImportPath;
import fix.entity.MatchVariable;
import fix.io.CopyExamples;
import fix.io.InsertCode;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Test {
//    static String filePath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;
    public static String filePath = "";
    static Set<String> variableVector = new HashSet<String>();

    public static String var = "";
    public static String lockName = "";
    public static int location = 0;

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

        Test t = new Test();
        t.filePath = "D:\\FixExamples\\exportExamples\\account\\Account.java";
        t.var = "amount";
        t.location = 28;
        t.lock("D:\\FixExamples\\exportExamples\\account\\Account.java");
        System.out.println(t.lockName);
        String lockNameNow = "this";
        if(t.lockName.equals("amount")){
            lockNameNow = "";
        }else{
            lockNameNow = t.lockName;
        }
        InsertCode.insert(28, " synchronized (" + lockNameNow +"){ ", filePath);
        InsertCode.insert(29, " }", filePath);
    }

    public static void lock(String filePath) {
        MatchVariable matchVariable = new MatchVariable();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        cu.accept(new ASTVisitor() {

            Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰

            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
//                System.out.println(node.getName() + "," +cu.getLineNumber(node.getStartPosition()));
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());
                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
//                System.out.println("SimpleName:" + node.getIdentifier() + "," + cu.getLineNumber(node.getStartPosition()));
                if (this.names.contains(node.getIdentifier())) {
                    if(node.toString().equals(var) && cu.getLineNumber(node.getStartPosition()) == location){
//                        System.out.println("SimpleName:" + node.getIdentifier() + "," + cu.getLineNumber(node.getStartPosition()));
                        lockName = node.getIdentifier();
                    }
                    /*System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                   //判断是此变量的类型，形式是 a.b  还是 b
                    String content = String.valueOf(node.getParent());
                    System.out.println(content);
                    String pattern = "^.+\\.amount$";
                    boolean isMatch = Pattern.matches(pattern, content);
                    System.out.println(isMatch);*/
                }

                return true;
            }

            //QualifiedName由两部分构成:name.SimpleName
            //QualifiedName先于SimpleName执行
            public boolean visit(QualifiedName  node){
                if(node.getName().toString().equals(var) && cu.getLineNumber(node.getStartPosition()) == location){
//                    System.out.println("QualifiedName:" + node.getQualifier() + "," + cu.getLineNumber(node.getStartPosition()));
                    lockName = node.getQualifier().toString();
                    return false;
                }
//                System.out.println("QualifiedName:" + node.getQualifier() + "," + cu.getLineNumber(node.getStartPosition()));
                return true;
            }

            public boolean visit(Name  node){
                System.out.println("Name:" + String.valueOf(node) + "," + cu.getLineNumber(node.getStartPosition()));
                return true;
            }

        });
    }
}
