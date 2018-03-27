package fix.analyzefile;

import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.ReadWriteNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UseASTAnalysisClass {

    static String className = "";//类的名字，以后用来比较用
    static boolean flagMember = false;//是不是成员变量
    static boolean flagConstruct = false;//是不是构造函数

    static boolean rw1Match = false;//第一个读写点有没有匹配
    static boolean rw2Match = false;//第二个读写点有没有匹配

    static ASTNode rw1Node = null;//匹配第一个读写点
    static ASTNode rw2Node = null;//匹配第二个读写点

    static boolean flagSameFunction = false;//是不是在一个函数中

    static LockLine lockLine = new LockLine();//用来记录加锁的起始和终止行数

    public static void main(String[] args) {
//        System.out.println(isConstructOrIsMemberVariableOrReturn(11, 12, ImportPath.examplesRootPath + "\\exportExamples\\" + ImportPath.projectName + "\\Account.java"));
        /*List<ReadWriteNode> nodesList = new ArrayList<ReadWriteNode>();
        nodesList.add(new ReadWriteNode(1, "linkedlist.MyListNode@18d", "_next", "WRITE", "Thread-4", "linkedlist/MyLinkedList.java:52"));
        nodesList.add(new ReadWriteNode(2, "linkedlist.MyListNode@18d", "_next", "WRITE", "Thread-4", "linkedlist/MyLinkedList.java:53"));
        System.out.println(assertSameFunction(nodesList, ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\MyLinkedList.java"));*/
        useASTCFindLockLine(ImportPath.examplesRootPath + "\\exportExamples\\" + ImportPath.projectName + "\\Account.java");
    }

    //判断变量是不是在if(),while(),for()的判断中
    //注意是判断中，就是圆括号中
    //如果是的话，要稍微修改一下加锁的函数
    public static LockLine changeLockLine(int firstLoc, int lastLoc, String filePath) {
        lockLine.setFirstLoc(firstLoc);
        lockLine.setLastLoc(lastLoc);
        useASTChangeLine(firstLoc, lastLoc, filePath);
        return lockLine;
    }

    //判断是不是成员变量或者构造函数
    public static boolean isConstructOrIsMemberVariableOrReturn(int firstLoc, int lastLoc, String filePath) {
        useASTAnalysisConAndMem(firstLoc, lastLoc, filePath);
        return flagConstruct || flagMember;
    }

    //利用AST来寻找加锁的行数
    public static void useASTCFindLockLine(String filePath) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SynchronizedStatement node) {
                System.out.println(node);
                System.out.println(cu.getLineNumber(node.getStartPosition()));
                System.out.println(cu.getLineNumber(node.getStartPosition() + node.getLength()));
                return super.visit(node);
            }
        });
    }

    //利用AST来改变加锁位置
    public static void useASTChangeLine(int firstLoc, int lastLoc, String filePath) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(InfixExpression node) {
                int start = cu.getLineNumber(node.getStartPosition());
                int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
                if (firstLoc >= start && lastLoc <= end) {//加锁区域在圆括号的里面
                    ASTNode parent = node.getParent();
                    lockLine.setFirstLoc(cu.getLineNumber(parent.getStartPosition()));
                    lockLine.setLastLoc(cu.getLineNumber(parent.getStartPosition() + parent.getLength()));//此处lastloc不要加1，因为加锁的时候已经是+1了
                }
                return super.visit(node);
            }
        });
    }


    //是不是在一个函数中
    public static boolean assertSameFunction(List<ReadWriteNode> nodesList, String filePath) {
        ReadWriteNode rwn1 = nodesList.get(0);
        ReadWriteNode rwn2 = nodesList.get(1);

        //判断是不是在一个函数中
        useASTAssertSameFun(rwn1, rwn2, filePath);
        return flagSameFunction;
    }

    //用AST来判别是不是在一个函数中
    private static void useASTAssertSameFun(ReadWriteNode rwn1, ReadWriteNode rwn2, String filePath) {
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
                SimpleName name = node.getName();
               // this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
               // if (this.names.contains(node.getIdentifier())) {
//                    System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                   /* System.out.println(rwn1.getField() + "," + Integer.parseInt(rwn1.getPosition().split(":")[1]));
                    System.out.println(rwn2.getField() + "," + Integer.parseInt(rwn2.getPosition().split(":")[1]));
                    System.out.println("==============");*/
                    if (node.toString().equals(rwn1.getField()) && cu.getLineNumber(node.getStartPosition()) == Integer.parseInt(rwn1.getPosition().split(":")[1])) {
                        rw1Match = true;
                        rw1Node = node;
                    }
                    if (node.toString().equals(rwn2.getField()) && cu.getLineNumber(node.getStartPosition()) == Integer.parseInt(rwn2.getPosition().split(":")[1])) {
                        rw2Match = true;
                        rw2Node = node;
                    }

                    if (rw1Match && rw2Match) {//两个读写点都找到的时候
                        flagSameFunction = isSameFunction(rw1Node, rw2Node);
                    }
              //  }
                return true;
            }
        });
    }

    //判断两个结点是不是在一个函数中
    private static boolean isSameFunction(ASTNode rw1Node, ASTNode rw2Node) {
        //找到第一个结点在哪个函数中
        ASTNode iNode = rw1Node.getParent();
        while (!(iNode instanceof MethodDeclaration)) {
            iNode = iNode.getParent();
        }
        //找到第二个结点在哪个函数中
        ASTNode jNode = rw2Node.getParent();
        while (!(jNode instanceof MethodDeclaration)) {
            jNode = jNode.getParent();
        }
        if (iNode.equals(jNode)) {
            return true;
        } else {
            return false;
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

    public static void useASTAnalysisConAndMem(int firstLoc, int lastLoc, String filePath) {

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
                if (cu.getLineNumber(node.getStartPosition()) >= firstLoc && cu.getLineNumber(node.getStartPosition()) <= lastLoc) {
                    flagMember = isMemberVariable(node);
                }
                SimpleName name = node.getName();
//                System.out.println(isMemberVariable(node) + "test");
//                this.names.add(name.getIdentifier());

                return true; // do not continue to avoid usage info
            }

            //变量
            public boolean visit(SimpleName node) {
//                if (this.names.contains(node.getIdentifier())) {
//                    System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
                    //判断是不是构造函数
                    if (cu.getLineNumber(node.getStartPosition()) >= firstLoc && cu.getLineNumber(node.getStartPosition()) <= lastLoc) {
                        flagConstruct = isConstruct(node);
                    }
//                }
                return true;
            }
        });
    }

    //判断是不是成员变量
    private static boolean isMemberVariable(ASTNode node) {
        if (node.getParent().getParent() instanceof TypeDeclaration) {
            return true;
        } else {
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

    //表示加锁行数
    public static class LockLine {
        int firstLoc;
        int lastLoc;

        public int getFirstLoc() {
            return firstLoc;
        }

        public void setFirstLoc(int firstLoc) {
            this.firstLoc = firstLoc;
        }

        public int getLastLoc() {
            return lastLoc;
        }

        public void setLastLoc(int lastLoc) {
            this.lastLoc = lastLoc;
        }
    }
}
