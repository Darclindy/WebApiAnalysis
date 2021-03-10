package edu.osu.sec.vsa.graph;

import java.util.HashSet;
import java.util.Hashtable;

import edu.osu.sec.vsa.utility.ListUtility;
import edu.osu.sec.vsa.utility.Logger;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;

//
public class CallGraph {

	static Hashtable<String, CallGraphNode> nodes = new Hashtable<String, CallGraphNode>();

	static Hashtable<String, HashSet<SootMethod>> fieldSetters = new Hashtable<String, HashSet<SootMethod>>();

	public static void init() {
		long st = System.currentTimeMillis();
		CallGraphNode tmp;
		Value tv;
		FieldRef fr;
		String str;


		//先将所有的方法存入GraghNode中
		for (SootClass sclas : Scene.v().getClasses()) {//遍历所有类
			for (SootMethod smthd : sclas.getMethods()) {//遍历类里的方法
				tmp = new CallGraphNode(smthd);
				nodes.put(smthd.toString(), tmp);	//<方法名,节点对象>
				if (smthd.isConcrete())
					smthd.retrieveActiveBody();
			}
		}

		for (SootClass sclas : Scene.v().getClasses()) {
			for (SootMethod smthd : ListUtility.clone(sclas.getMethods())) {//遍历method
				if (!smthd.isConcrete())
					continue;
				Body body = smthd.retrieveActiveBody();//返回active body
				if (body == null)
					continue;
				for (Unit unit : body.getUnits()) {//获取该body下的语句
					if (unit instanceof Stmt) {//unit是否为Stmt的一个实例；寻找调用、跳转语句
						if (((Stmt) unit).containsInvokeExpr()) {//调用语句InvokeExpr
							try {
								addCall(smthd, ((Stmt) unit).getInvokeExpr().getMethod());
							} catch (Exception e) {
								//Logger.printW(e.getMessage());
								//System.out.println(e.getMessage());
							}
						}
						for (ValueBox var : ((Stmt) unit).getDefBoxes()) {//返回在该Unit被定义的ValueBox(s)列表
							tv = var.getValue();
							if (tv instanceof FieldRef) {	//FieldRef soot class下的成员
								fr = (FieldRef) tv;
								//if (fr.getField().getDeclaringClass().isApplicationClass()) {//如果是ApplicationClass,就加进来
									str = fr.getField().toString();
									if (!fieldSetters.containsKey(str)) {	//fieldSetters不含有该method，就添加进来。
										fieldSetters.put(str, new HashSet<SootMethod>());
									}

									fieldSetters.get(str).add(smthd);
									//System.out.println("FS"+smthd+" "+str);//str:Filed
									//Logger.print("FS:" + smthd + " " + str);//FS<android.arch.core.internal.SafeIterableMap: void <init>()> <android.arch.core.internal.SafeIterableMap: java.util.WeakHashMap mIterators>
								//}
							}
						}
					}
				}
			}
		}

	}

	private static void addCall(SootMethod from, SootMethod to) {
		CallGraphNode fn, tn;
		fn = getNode(from);
		tn = getNode(to);
		if (fn == null || tn == null) {
			// Logger.printW("NULL: " + from + " " + to);
			//System.out.println("from===>"+from);
			//System.out.println("to  ===>"+to);
			return;
		}

		fn.addCallTo(tn);
		tn.addCallBy(fn);

	}

	public static CallGraphNode getNode(SootMethod from) {
		return getNode(from.toString());
	}

	public static CallGraphNode getNode(String from) {
		return nodes.get(from);
	}

	public static HashSet<SootMethod> getSetter(SootField sootField) {
		return fieldSetters.get(sootField.toString());
	}
}
