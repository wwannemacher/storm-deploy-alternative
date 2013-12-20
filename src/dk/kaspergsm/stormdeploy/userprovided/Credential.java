package dk.kaspergsm.stormdeploy.userprovided;

import java.io.File;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kaspergsm.stormdeploy.Tools;

/**
 * Used to maintain idendity and credential
 * 
 * @author Kasper Grud Skat Madsen
 */
public class Credential {
	private static Logger log = LoggerFactory.getLogger(Credential.class);
	private String _identity, _credential;
	
	public static Credential fromYamlFile(File f) {
		HashMap<String, Object> credentials = Tools.readYamlConf(f);
		if (!credentials.containsKey("identity") || !credentials.containsKey("credential")) {
			log.error("credential.yaml must contain both identity and credential");
			System.exit(0);
		}
		
		return new Credential((String)credentials.get("identity"), (String)credentials.get("credential"));
	}
	
	public Credential(String identity, String credential) {
		_identity = identity;
		_credential = credential;
	}
	
	public String getIdentity() {
		return _identity;
	}
	
	public String getCredential() {
		return _credential;
	}
}