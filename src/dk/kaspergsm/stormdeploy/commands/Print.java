package dk.kaspergsm.stormdeploy.commands;

import java.util.ArrayList;
import java.util.Set;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;

/**
 * Called to print overview of cluster
 * 
 * @author Kasper Grud Skat Madsen
 */
public class Print {	
	@SuppressWarnings("unchecked")
	public static void run(String clustername, ComputeServiceContext computeContext) {
		
		/**
		 * Parse current running nodes for cluster
		 */
		ArrayList<NodeMetadata> zkNodes = new ArrayList<NodeMetadata>();
		ArrayList<NodeMetadata> workerNodes = new ArrayList<NodeMetadata>();
		NodeMetadata ui = null;
		NodeMetadata nimbus = null;
		for (NodeMetadata n : (Set<NodeMetadata>) computeContext.getComputeService().listNodes()) {			
			if (n.getStatus() != Status.TERMINATED &&
					n.getGroup() != null &&
					n.getGroup().toLowerCase().equals(clustername.toLowerCase()) &&
					n.getUserMetadata().containsKey("daemons")) {
				String daemons = n.getUserMetadata().get("daemons").replace("[", "").replace("]", "");
				
				for (String daemon : daemons.split(",")) {
					if (daemon.trim().toLowerCase().equals("master"))
						nimbus = n;
					if (daemon.trim().toLowerCase().equals("worker"))
						workerNodes.add(n);
					if (daemon.trim().toLowerCase().equals("zk"))
						zkNodes.add(n);
					if (daemon.trim().toLowerCase().equals("ui"))
						ui = n;
				}
			}
		}
		
		/**
		 * Generate print
		 */
	    String printFormat = "%-20s %-30s %-30s";
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(printFormat, "daemon", "private ip", "public ip"));
		sb.append("\n");
		
		if (nimbus != null) {
			sb.append(String.format(printFormat, "nimbus", nimbus.getPrivateAddresses().iterator().next(), nimbus.getPublicAddresses().iterator().next()));
			sb.append("\n");
		}
			
		if (ui != null) {
			sb.append(String.format(printFormat, "ui", ui.getPrivateAddresses().iterator().next(), ui.getPublicAddresses().iterator().next()));
			sb.append("\n");
		}
			
		for (NodeMetadata n : workerNodes) {
			sb.append(String.format(printFormat, "worker", n.getPrivateAddresses().iterator().next(), n.getPublicAddresses().iterator().next()));
			sb.append("\n");
		}
			
		for (NodeMetadata n : zkNodes) {
			sb.append(String.format(printFormat, "zk", n.getPrivateAddresses().iterator().next(), n.getPublicAddresses().iterator().next()));
			sb.append("\n");
		}
			
		// Print now
		System.out.println(sb.toString());
	}
	
}
