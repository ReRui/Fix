package fix.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import fix.entity.MatchVariable;
import fix.io.InsertCode;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import javax.swing.filechooser.FileSystemView;

public class AddLock {
    static String filePath = "";
    static {
        //定位到项目目录下
        filePath = System.getProperty("user.dir") + "\\heu_search\\src\\examples" + "\\account";
    }

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
        File file = new File(filePath);
        File[] fileArr = file.listFiles();
        for(File f : fileArr){
            lock(f.getPath());
            System.out.println(f.getPath());
        }
    }

    public static void lock(String filePath) {
        MatchVariable matchVariable = new MatchVariable();
        Set<String> variableVector = new HashSet<String>();
        variableVector.add("amount");
        variableVector.add("name");
        InsertCode.insert(3, "import java.util.concurrent.locks.ReentrantLock;" + '\n', filePath);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        cu.accept(new ASTVisitor() {

            Set<String> names = new HashSet<String>();//存放实际使用的变量，不这样做会有System等变量干扰


            //定义变量
            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
                    System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));

                    boolean flag = false;
                    for(String s : variableVector){
                        if(s.equals(node.getIdentifier()))
                            flag = true;
                    }
                    if(flag){

                        if(matchVariable.matchVectorIsEmpty()){
                            matchVariable.addMatchVector(node.getIdentifier());
                            matchVariable.setNode(node.getParent());
                        }
                        else{
                            //如果有数据则需要往里面添加数据，对于相同的变量，后面的一个变量应该覆盖前面的，在次使用改变父节点的方法
                            if(matchVariable.getMatchVector().contains(node.getIdentifier())){
                                matchVariable.setNode(node.getParent());
                            }else{
                                matchVariable.addMatchVector(node.getIdentifier());
                                System.out.println("test" + node.getParent());
                                matchVariable.searchSame(node.getParent());
                            }
                        }
                        if(matchVariable.equalTarget(variableVector)){
                            System.out.println("匹配成功");
                            System.out.println("开始" + cu.getLineNumber(matchVariable.getStartLine()));//下一行
                            System.out.println("结束" + cu.getLineNumber(matchVariable.getEndLine() + 1));

                            //加锁
                            InsertCode.insert(cu.getLineNumber(matchVariable.getStartLine()), "ReentrantLock lock" + matchVariable.getLockNum() +" = new ReentrantLock(true);lock" + matchVariable.getLockNum() + ".lock();", filePath);
                            InsertCode.insert(cu.getLineNumber(matchVariable.getEndLine() + 1), "lock" + matchVariable.getLockNum() + ".unlock();", filePath);
                            matchVariable.update();

                            matchVariable.clear();
                        }
                    }
                    flag= false;
                }

                return true;
            }


        });
    }
}
