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
	private String _x509pkPathEC2 = null, _x509certPathEC2 = null;
	private String _identityEC2 = null, _credentialEC2 = null;
		
	public Credential(File f) {
		HashMap<String, Object> credentials = Tools.readYamlConf(f);
		if (credentials == null || credentials.size() == 0) {
			log.error("No credentials found. Please ensure credentials.yaml exists");
			System.exit(0);
		}
		
		// Parse ec2 credentials
		if (credentials.containsKey("ec2-identity"))
			_identityEC2 = (String)credentials.get("ec2-identity");
		if (credentials.containsKey("ec2-credential"))
			_credentialEC2 = (String)credentials.get("ec2-credential");
		if ((_identityEC2 == null && _credentialEC2 != null) || (_identityEC2 != null && _credentialEC2 == null)) {
			log.error("Incomplete credentials for Amazon EC2");
			System.exit(0);
		}		
		
		// Parse optional ec2 credentials
		if (credentials.containsKey("ec2-x509-certificate-path") && ((String)credentials.get("ec2-x509-certificate-path")).length() > 0) {
			_x509certPathEC2 = (String)credentials.get("ec2-x509-certificate-path");
			if (!new File(_x509certPathEC2).exists()) {
				log.error("Invalid ec2-x509-certificate-path. File not found!");
				System.exit(0);
			}
		}
		if (credentials.containsKey("ec2-x509-private-path")  && ((String)credentials.get("ec2-x509-private-path")).length() > 0) {
			_x509pkPathEC2 = (String)credentials.get("ec2-x509-private-path");
			if (!new File(_x509pkPathEC2).exists()) {
				log.error("Invalid ec2-x509-private-path. File not found!");
				System.exit(0);
			}
		}
		
		if ((_x509certPathEC2 == null && _x509pkPathEC2 != null) || _x509certPathEC2 != null && _x509pkPathEC2 == null) {
			log.error("Incomplete credentials for Amazon Web Services x509 credentials");
			System.exit(0);
		}
		
		// Parse cloudstack credentials
		if (credentials.containsKey("cs-identity"))
			_identityCloudStack = (String)credentials.get("cs-identity");
		if (credentials.containsKey("cs-credential"))
			_credentialCloudStack = (String)credentials.get("cs-credential");
		if ((_identityCloudStack == null && _credentialCloudStack != null) || _identityCloudStack != null && _credentialCloudStack == null) {
			log.error("Incomplete credentials for CloudStack");
			System.exit(0);
		}
	}
	
	public String get_ec2_X509PrivateKeyPath() {
		return _x509pkPathEC2;
	}
	
	public String get_ec2_X509CertificatePath() {
		return _x509certPathEC2;
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