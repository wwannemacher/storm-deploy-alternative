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
	private String _x509pkPath = null, _x509certPath = null;
	private String _identity, _credential;
	
	public static Credential fromYamlFile(File f) {
		HashMap<String, Object> credentials = Tools.readYamlConf(f);
		if (!credentials.containsKey("identity") || !credentials.containsKey("credential")) {
			log.error("credential.yaml must contain both identity and credential");
			System.exit(0);
		}
		
		Credential c = new Credential((String)credentials.get("identity"), (String)credentials.get("credential"));		
		if (credentials.containsKey("x509-certificate-path"))
			c.setX509CerfiticatePath((String)credentials.get("x509-certificate-path"));
		if (credentials.containsKey("x509-private-path"))
			c.setX509PrivateKeyPath((String)credentials.get("x509-private-path"));
		return c;
	}
	
	public Credential(String identity, String credential) {
		_identity = identity;
		_credential = credential;
	}
	
	public void setX509PrivateKeyPath(String path) {
		_x509pkPath = path;
	}
	
	public String getX509PrivateKeyPath() {
		return _x509pkPath;
	}
	
	public void setX509CerfiticatePath(String path) {
		_x509certPath = path;
	}
	
	public String getX509CertificatePath() {
		return _x509certPath;
	}
	
	public String getIdentity() {
		return _identity;
	}
	
	public String getCredential() {
		return _credential;
	}
}