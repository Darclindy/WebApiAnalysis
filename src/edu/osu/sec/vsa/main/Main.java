package edu.osu.sec.vsa.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipException;

import com.sun.xml.internal.ws.client.sei.ValueSetter;
import org.json.JSONArray;
import org.json.JSONObject;

import soot.Scene;
import soot.Value;
import soot.jimple.parser.node.TStrictfp;
import soot.options.Options;
import brut.androlib.AndrolibException;
import edu.osu.sec.vsa.base.GlobalStatistics;
import edu.osu.sec.vsa.graph.CallGraph;
import edu.osu.sec.vsa.graph.DGraph;
import edu.osu.sec.vsa.graph.IDGNode;
import edu.osu.sec.vsa.graph.ValuePoint;
import edu.osu.sec.vsa.utility.ErrorHandler;
import edu.osu.sec.vsa.utility.FileUtility;
import edu.osu.sec.vsa.utility.Logger;

public class Main {

	static JSONObject targetMethds;	//目标方法

	public static void startWatcher(int sec) {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(sec * 1000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				wf("TimeOut!!!");
				System.exit(0);
			}
		};
		t.setDaemon(true);
		t.start();
	}

	public static void main(String[] args) throws ZipException, IOException, AndrolibException {
		initDirs();		//新建output，log文件。

		Config.ANDROID_JAR_DIR = args[0];//arg0为android.jar包的路径
		
		targetMethds = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1]))));//arg1为json文件路径，读取信息,并转换为json格式
		String apk = targetMethds.getString("apk");//获取目标apk名称
		Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(args[0]));//设置异常处理
		ApkContext apkcontext = ApkContext.getInstance(apk);//创建apkContext对象，path成员初始化
		Logger.TAG = apkcontext.getPackageName();

		soot.G.reset();			//soot.G.reset()是一个标准的soot操作，用于清空soot之前所有操作遗留下的缓存值
		Options.v().set_src_prec(Options.src_prec_apk);			//是否记录代码所在行
		Options.v().set_process_dir(Collections.singletonList(apkcontext.getAbsolutePath()));//待反编译文件所在的文件夹，此处是apk文件地址
		//Options.v().set_android_jars(Config.ANDROID_JAR_DIR);//表示在该路径下寻找android.jar文件
		Options.v().set_force_android_jar(Config.ANDROID_JAR_DIR);//表示绝对路径在该路径下寻找android.jar文件
		Options.v().set_process_multiple_dex(true);
		Options.v().set_whole_program(true);		//以全局应用的模式运行
		Options.v().set_allow_phantom_refs(true);	//表示是否加载未被解析的类
		Options.v().set_output_format(Options.output_format_none);//设置输出的格式，此处设置为不输出，因此不会输出反编译后的jimple文件
		Options.v().ignore_resolution_errors();
		Scene.v().loadNecessaryClasses();        // //使用soot反编译dex文件，并将反编译后的文件加载到内存中

		startWatcher(Config.TIMEOUT);
		CallGraph.init();						//生成函数调用图
		//startWatcher(20);

		DGraph dg = new DGraph();	//生成数据依赖图

		List<ValuePoint> allvps = new ArrayList<ValuePoint>();
		List<ValuePoint> vps;
		String tsig;			//存储目标函数的三地址码
		List<Integer> regIndex;//存储目标函数的参数位置
		JSONObject tmp;

		for (Object jobj : targetMethds.getJSONArray("methods")) {
			
			tmp = (JSONObject) jobj;
			
			tsig = tmp.getString("method");	//遍历输入json文件的method的三字节码
			regIndex = new ArrayList<Integer>();

			for(Object tob:tmp.getJSONArray("parmIndexs")){	//获得参数索引
				regIndex.add((Integer) tob);
			}

			vps = ValuePoint.find(dg, tsig, regIndex); //目标方法作为第一个ValuePoint,进入DGragh,

			/*
			for (ValuePoint vp : vps) {
				System.out.println("sigatureInApp"+tsig);
				tmp = new JSONObject();
				//tmp.put("sigatureInApp", tsig);
				//tmp.put("sigatureIndex", targetMethds.getString(tsig));	//
				vp.setAppendix(tmp);
				vp.print();//打印该valuepoint
			}
			 */
			allvps.addAll(vps);
		}

		dg.solve(allvps);//		反向切片

		/*
		JSONObject result = new JSONObject();
			for (IDGNode tn : dg.getNodes()) {
			Logger.print(tn.toString());//打印Dgragh中各个节点的信息，包括类名，方法，语句，能否抵达，前驱，BackwardContexts
		}

		for (ValuePoint vp : allvps) {
			tmp = vp.toJson();

			if (tmp.has("ValueSet")){
				Logger.print(tmp.getJSONArray("ValueSet").toString());
			}

			result.append("ValuePoints", vp.toJson());
		}
		*/

		//wf(result.toString());		//将JSONObjeact输出到目标文件


		//输出http信息
		Vector<String> list = new Vector<String>();
		for(Object vp : dg.getNodes()){
			if(vp instanceof ValuePoint){
				for(HashMap<Integer, HashSet<String>> var : ((ValuePoint) vp).getResult()){
					for(int i : var.keySet()){
						for(String s : var.get(i)){
							if(s.contains("http") && !list.contains(s))
								list.add(s);
						}
					}
				}
			}
		}


		String OutPut = "";
		for(String temp : list){
			OutPut += temp;
			OutPut += "\n";
			System.out.println(temp);
		}
		wf(OutPut);

	}



	public static void wf(String content) {
		FileUtility.wf(Config.RESULTDIR + ApkContext.getInstance().getPackageName(), content, false);
	}

	public static void initDirs() {
		File tmp = new File(Config.RESULTDIR);//output file
		if (!tmp.exists())
			tmp.mkdir();
		tmp = new File(Config.LOGDIR);//log file
		if (!tmp.exists())
			tmp.mkdir();
	}
}

