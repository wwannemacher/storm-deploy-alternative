package dk.kaspergsm.stormdeploy.userprovided;

import java.io.File;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kaspergsm.stormdeploy.Tools;

/**
 * Used to maintain credentials
 * 
 * @author Kasper Grud Skat Madsen
 */
public class Credential {
	private static Logger log = LoggerFactory.getLogger(Credential.class);
	private String _identityCloudStack = null, _credentialCloudStack = null;
	private String _identityEC2 = null, _credentialEC2 = null;
	private String _x509pkPath = null, _x509certPath = null;
		
	public Credential(File f) {
		HashMap<String, Object> credentials = Tools.readYamlConf(f);
		
		if (credentials.containsKey("ec2-identity"))
			_identityEC2 = (String)credentials.get("ec2-identity");
		if (credentials.containsKey("ec2-credential"))
			_credentialEC2 = (String)credentials.get("ec2-credential");
		if (credentials.containsKey("cs-identity"))
			_identityCloudStack = (String)credentials.get("cs-identity");
		if (credentials.containsKey("cs-credential"))
			_credentialCloudStack = (String)credentials.get("cs-credential");
	}
	
	public String get_ec2_X509PrivateKeyPath() {
		return _x509pkPath;
	}
	
	public String get_ec2_X509CertificatePath() {
		return _x509certPath;
	}
	
	public String get_ec2_identity() {
		return _identityEC2;
	}
	
	public String get_ec2_credential() {
		return _credentialEC2;
	}
	
	public String get_cs_identity() {
		return _identityCloudStack;
	}
	
	public String get_cs_credential() {
		return _credentialCloudStack;
	}
}