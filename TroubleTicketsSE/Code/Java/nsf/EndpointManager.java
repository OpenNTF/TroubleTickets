package nsf;

import com.ibm.sbt.services.client.connections.profiles.Profile;
import com.ibm.sbt.services.client.connections.profiles.ProfileService;
import com.ibm.sbt.services.client.connections.profiles.ProfileServiceException;
import com.ibm.sbt.services.endpoints.BasicEndpoint;
import com.ibm.sbt.services.endpoints.ConnectionsBasicEndpoint;
import com.ibm.sbt.services.endpoints.ConnectionsSSOEndpoint;
//import com.ibm.sbt.services.endpoints.Endpoint;
import com.ibm.sbt.services.endpoints.SSOEndpoint;

public class EndpointManager {
	private static ConnectionsBasicEndpoint connBasicBean = null;
	private static ConnectionsSSOEndpoint connSSOBean = null;
	private static String connectionsURL = "";
	
	public final static String CONNECTIONS_BASIC = "basic";
	public final static String CONNECTIONS_SSO = "sso";
	public final static String CONNECTIONS = "connections";
	
	public EndpointManager() {

	}
	
	public static BasicEndpoint createBasicBean(String beanName, String URL) {
		if(beanName.equals(CONNECTIONS)) {
			setConnectionsURL(URL);
		
			connBasicBean = new ConnectionsBasicEndpoint();
			connBasicBean.setForceTrustSSLCertificate(true);
			connBasicBean.setUrl(getConnectionsURL());
			connBasicBean.setName(CONNECTIONS);
			connBasicBean.setAuthenticationService("communities/service/atom/communities/all");
			connBasicBean.setAuthenticationPage("TroubleTicketsSE.nsf/_BasicLogin.xsp?endpoint=connections");
			connBasicBean.setCredentialStore("PasswordStore");
			return connBasicBean;
		}else{
			return null;
		}
	}
	
	public SSOEndpoint createSSOBean(String beanName, String URL) {
		if(beanName.equals(CONNECTIONS)) {
			setConnectionsURL(URL);
			connSSOBean = new ConnectionsSSOEndpoint();
			connSSOBean.setForceTrustSSLCertificate(true);
			connSSOBean.setUrl(getConnectionsURL());
			return connSSOBean;
		}else{
			return null;
		}
	}
	
	public static BasicEndpoint getBasicBean(String beanName) {
		if(beanName.equals(CONNECTIONS)) {
			return connBasicBean;
		}else{
			return null;
		}
	}
	
	public static SSOEndpoint getSSOBean(String beanName) {
		if(beanName.equals(CONNECTIONS)) {
			return connSSOBean;
		}else{
			return null;
		}
	}
	
	public static String getConnectionsURL() {
		return connectionsURL;
	}

	public static void setConnectionsURL(String connectionsURL) {
		EndpointManager.connectionsURL = connectionsURL;
	}

	
	/**
	 * getConUserId calls the SBTSDK to get a Unique User Id
	 * 
	 * @param email
	 * @return
	 */
	public static String getConUserId(String email) {
		String result = "";
		try {
			ProfileService profileService = new ProfileService(connSSOBean);
			Profile profile = profileService.getProfile(email);
			result = profile.getName();
		} catch (ProfileServiceException e) {
			e.printStackTrace();
		}
		return result;
	}
}
