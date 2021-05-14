package top.clindy.main;

import brut.androlib.AndrolibException;
import edu.osu.sec.vsa.base.StmtPoint;
import edu.osu.sec.vsa.graph.CallGraph;
import edu.osu.sec.vsa.graph.CallGraphNode;
import edu.osu.sec.vsa.graph.DGraph;
import edu.osu.sec.vsa.graph.ValuePoint;
import edu.osu.sec.vsa.main.Config;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.internal.InvokeExprBox;
import soot.options.Options;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;

public class Main {
    static String ProcessDir = "./example/app/eye.apk";
    static String AndroidJar = "./libs/android.jar";
    static String TargetMethod = "<java.net.URL: void <init>(java.lang.String)>";
    static List<Integer> RegIndex;

    public static void FindURL(){
        List<ValuePoint> vps = new ArrayList<ValuePoint>();
        List<StmtPoint> sps = StmtPoint.findCaller(TargetMethod);//找到调用改语句的位置
        ValuePoint tmp;

        String pattern = TargetMethod+"(\"http" ;

        for(StmtPoint sp : sps){
            Body body = sp.getMethod_location().getActiveBody();

            String lct = sp.getBlock_location().toString();
            if(lct.contains(pattern)){
                System.out.println(lct.toString());
            }
        }

    }

    public static void PrintBlock(){
        List<StmtPoint> sps = StmtPoint.findCaller(TargetMethod);//找到调用改语句的位置

        for(StmtPoint sp : sps){
            String lct = sp.getBlock_location().toString();
            System.out.println(lct);
        }
    }

    public static void main(String[] args) throws ZipException, IOException, AndrolibException {
        soot.G.reset();
        Options.v().set_src_prec(Options.src_prec_apk);			//是否记录代码所在行
        Options.v().set_process_dir(Collections.singletonList(ProcessDir));
        Options.v().set_force_android_jar(AndroidJar);
        Options.v().set_whole_program(true);		//以全局应用的模式运行
        Options.v().set_allow_phantom_refs(true);	//表示是否加载未被解析的类`
        Options.v().set_process_multiple_dex(true);
        Options.v().ignore_resolution_errors();

        Scene.v().loadNecessaryClasses();		//使用soot反编译dex文件，并将反编译后的文件加载到内存中
        CallGraph.init();       //生成函数调用图


        FindURL();
        //PrintBlock();
        /*
        for (SootClass sclas : Scene.v().getClasses()) {
            if(sclas.toString().contains("java.net") ){
                for (SootMethod smthd : sclas.getMethods() ) {
                    //if((StmtPoint.findCaller(smthd.toString()).isEmpty()) && smthd.toString().contains("java.lang.String")){
                    if(smthd.toString().contains("java.lang.String")){
                        System.out.println(smthd.toString());
                    }
                }
            }
        }

         */

    }
}
