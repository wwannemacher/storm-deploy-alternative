package dk.kaspergsm.stormdeploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.StatementList;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.kaspergsm.stormdeploy.configurations.Zookeeper;
import dk.kaspergsm.stormdeploy.userprovided.Configuration;

/**
 * Used to launch a new nodes
 * 
 * @author Kasper Grud Skat Madsen
 */
public class LaunchNodeThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(LaunchNodeThread.class);
	private String _instanceType, _clustername, _region, _image, _username;
	private Set<NodeMetadata> _newNodes = null;
	private List<Statement> _initScript;
	private ComputeService _compute;
	private List<Integer> _nodeids;
	private List<String> _daemons;
	
	/**
	 * @param compute
	 *            ComputeService from JClouds
	 * @param instanceType
	 *            Supported instanceType (e.g. m1.medium on aws-ec2)
	 * @param image
	 *            Image to deploy
	 * @param region
	 *            Region to deploy into (image must be in this region)
	 * @param clustername
	 *            Name of cluster to deploy
	 * @param nodeids
	 *            Set of nodeids being launched
	 * @param daemons
	 *            Set of daemons to launch on this set of nodes
	 * @param zkMyId
	 *            If contain(daemons, zk) then write this zkMyId on init
	 */
	public LaunchNodeThread(ComputeService compute, Configuration config, String instanceType, String clustername, List<Integer> nodeids, List<String> daemons, Integer zkMyId) {
		_region = config.getDeploymentLocation();
		_username = config.getImageUsername();
		_image = config.getDeploymentImage();
		_instanceType = instanceType;
		_clustername = clustername;
		_daemons = daemons;
		_compute = compute;
		_nodeids = nodeids;
		
		// Create initScript
		_initScript = new ArrayList<Statement>();
		_initScript.add(exec("echo \"" + daemons.toString() + "\" > ~/daemons"));
		if (zkMyId != null)
			_initScript.addAll(Zookeeper.writeZKMyIds(_username, zkMyId));

		// Run thread now
		this.start();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			_newNodes = (Set<NodeMetadata>) _compute.createNodesInGroup(
					_clustername,
					_nodeids.size(),
					_compute.templateBuilder()
							.hardwareId(_instanceType)
							.locationId(_region)
							.imageId(_image)
							.options(
									new TemplateOptions()
											.runAsRoot(false)
											.wrapInInitScript(true)
											.overrideLoginUser(_username)
											.inboundPorts(Tools.getPortsToOpen())
											.userMetadata("daemons", _daemons.toString())
											.runScript(new StatementList(_initScript))
											.overrideLoginCredentials(Tools.getPrivateKeyCredentials(_username))
											.authorizePublicKey(Tools.getPublicKey())).build());
		} catch (RunNodesException ex) {
			log.error("Problem launching instance", ex);
		}
	}

	public List<Integer> getNodeIds() {
		return _nodeids;
	}

	public Set<NodeMetadata> getNewNodes() {
		return _newNodes;
	}
}