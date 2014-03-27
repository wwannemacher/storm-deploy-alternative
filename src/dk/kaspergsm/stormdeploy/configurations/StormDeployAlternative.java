package dk.kaspergsm.stormdeploy.configurations;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jclouds.scriptbuilder.domain.Statement;
import dk.kaspergsm.stormdeploy.Tools;

/**
 * Contains all methods to configure SnormDeployAlternative on remote node
 * 
 * @author Kasper Grud Skat Madsen
 */
public class StormDeployAlternative {

	public static List<Statement> download() {
		return Tools.download("~", "s3://storm-deploy-alternative/sda.tar.gz", true, true);
	}
	
	/**
	 * Run memoryMonitor.
	 * 	Requires tools.jar from active jvm is on path. Is automatically searched and found if it exists in /usr/lib/jvm
	 */
	public static List<Statement> runMemoryMonitor() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("su -c 'java -cp \"/home/ubuntu/sda/storm-deploy-alternative.jar:$( find `ls -d /usr/lib/jvm/* | sort -k1 -r` -name tools.jar | head -1 )\" dk.kaspergsm.stormdeploy.image.MemoryMonitor &' - ubuntu"));
		return st;
	}
	
	public static List<Statement> writeConfigurationFiles(String localConfigurationFile, String localCredentialFile) {	
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir ~/sda/conf"));
		st.addAll(Tools.echoFile(localConfigurationFile, "~/sda/conf/configuration.yaml"));
		st.addAll(Tools.echoFile(localCredentialFile, "~/sda/conf/credential.yaml"));
		return st;
	}
	
	public static List<Statement> writeLocalSSHKeys() {
		ArrayList<Statement> st = new ArrayList<Statement>();
		st.add(exec("mkdir ~/.ssh/"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + "id_rsa", "~/.ssh/id_rsa"));
		st.addAll(Tools.echoFile(Tools.getHomeDir() + ".ssh" + File.separator + "id_rsa.pub", "~/.ssh/id_rsa.pub"));
		return st;
	}
}