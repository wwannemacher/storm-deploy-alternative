package dk.kaspergsm.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.scriptbuilder.domain.Statement;
import dk.kaspergsm.stormdeploy.Tools;

/**
 * Contains all methods to configure ZeroMQ on nodes
 * 
 * @author Kasper Grud Skat Madsen
 */
public class ZeroMQ {
	private static String _cond = "$(find /usr/* -name 'libzmq.a' | wc -l) -eq 0";
	
	public static List<Statement> download() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec(Tools.conditionalExec(_cond, "cd ~")));
		st.add(exec(Tools.conditionalExec(_cond, "wget http://download.zeromq.org/zeromq-2.1.7.tar.gz")));
		st.add(exec(Tools.conditionalExec(_cond, "tar -zxf zeromq-2.1.7.tar.gz")));
		st.add(exec(Tools.conditionalExec(_cond, "rm zeromq-2.1.7.tar.gz")));
		return st;
	}
	
	public static ArrayList<Statement> compile() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec(Tools.conditionalExec(_cond, "cd zeromq-2.1.7")));
		st.add(exec(Tools.conditionalExec(_cond, "./configure")));
		st.add(exec(Tools.conditionalExec(_cond, "make")));
		st.add(exec(Tools.conditionalExec(_cond, "make install")));
		st.add(exec(Tools.conditionalExec(_cond, "ldconfig")));
		return st;
	}
	
	public static ArrayList<Statement> installJavaBinding() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec(Tools.conditionalExec(_cond, "git clone https://github.com/nathanmarz/jzmq.git")));
		st.add(exec(Tools.conditionalExec(_cond, "cd jzmq")));
		st.add(exec(Tools.conditionalExec(_cond, "./autogen.sh")));
		st.add(exec(Tools.conditionalExec(_cond, "./configure")));
		st.add(exec(Tools.conditionalExec(_cond, "make")));
		st.add(exec(Tools.conditionalExec(_cond, "make install")));
		return st;
	}
}