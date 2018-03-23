package fix;

import fix.analyzefile.LockPolicyPopularize;
import fix.entity.ImportPath;
import fix.run.Fix;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.ReadWriteNode;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class Test {

    static String className = "";//类的名字，以后用来比较用
    static String dirPath = ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName;

    public static void main(String[] args) {

//        useASTAssertSameFun(ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\WrongLock.java");
//        t();
        String s = "itr = itr._next;";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^.*\\s+(\\w+)\\." + "_next" + ".*$");
        Matcher m = p.matcher(s);
        if (m.matches()) {
            System.out.println(m.group(1));
        } else {
            System.out.println("wu");
        }
       /* ReadWriteNode readWriteNode = new ReadWriteNode(1, "wrongLock.Data@15f", "value", "WRITE", "Thread-2", "wrongLock/WrongLock.java:28");
        System.out.println(acquireLockName(readWriteNode));*/

    }
    public static String acquireLockName(ReadWriteNode node) {
        BufferedReader br = null;
        String read = "";//用来读
        String result = "";//用来处理
        int line = 0;
        int poi = Integer.parseInt(node.getPosition().split(":")[1]);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dirPath + "\\WrongLock.java")), "UTF-8"));
            while (((read = br.readLine()) != null)) {
                line++;
                if (line == poi) {//找到哪一行
                    String field = node.getField();//得的变量
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\s+(\\w+)\\." + field + ".*$");
                    Matcher m = p.matcher(read);
                    System.out.println(read);
                    if (m.matches()) {
                        result = m.group(1);
                    } else {
                        result = "this";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("锁的名字" + result.trim());
        return result.trim();
    }

    private static int t() {
        int a = 1;
        Object o = new Object();
        synchronized (o) {
            return a;
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

    private static void useASTAssertSameFun(String filePath) {
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
                this.names.add(node.getName().getIdentifier());

                return true; // do not continue to avoid usage info
            }


            //变量
            public boolean visit(SimpleName node) {
//                if (this.names.contains(node.getIdentifier())) {
//                    System.out.println("Usage of '" + node + "' at line " + cu.getLineNumber(node.getStartPosition()));
//                    System.out.println(node.getParent());
//                }
                return true;
            }
        });
    }
}
