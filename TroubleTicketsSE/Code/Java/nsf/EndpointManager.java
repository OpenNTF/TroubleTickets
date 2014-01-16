package nsf;

import java.io.Serializable;
import lotus.domino.NotesException;

import com.ibm.sbt.services.endpoints.BasicEndpoint;
import com.ibm.sbt.services.endpoints.ConnectionsBasicEndpoint;
import com.ibm.sbt.services.endpoints.ConnectionsSSOEndpoint;
import com.ibm.sbt.services.endpoints.SSOEndpoint;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class EndpointManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ConnectionsBasicEndpoint connBasicBean = null;
	private ConnectionsSSOEndpoint connSSOBean = null;
	private String connectionsURL = "";
	
	public final static String CONNECTIONS_BASIC = "basic";
	public final static String CONNECTIONS_SSO = "sso";
	public final static String CONNECTIONS = "connections";
	
	public EndpointManager() {

	}
	
	public BasicEndpoint createBasicBean(String beanName, String URL) {
		if(beanName.equals(CONNECTIONS)) {
			setConnectionsURL(URL);
			connBasicBean = new ConnectionsBasicEndpoint();
			connBasicBean.setForceTrustSSLCertificate(true);
			connBasicBean.setUrl(getConnectionsURL());
			connBasicBean.setName(CONNECTIONS);
			connBasicBean.setAuthenticationService("communities/service/atom/communities/all");
			try {
				connBasicBean.setAuthenticationPage(ExtLibUtil.getCurrentDatabase().getFileName()+"/_BasicLogin.xsp?endpoint=connections");
				System.out.println(ExtLibUtil.getCurrentDatabase().getFileName());
			} catch (NotesException e) {
				e.printStackTrace();
			} 
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
	
	public BasicEndpoint getBasicBean(String beanName) {
		if(beanName.equals(CONNECTIONS)) {
			return connBasicBean;
		}else{
			return null;
		}
	}
	
	public SSOEndpoint getSSOBean(String beanName) {
		if(beanName.equals(CONNECTIONS)) {
			return connSSOBean;
		}else{
			return null;
		}
	}
	
	public String getConnectionsURL() {
		return connectionsURL;
	}

	public void setConnectionsURL(String connectionsURL) {
		this.connectionsURL = connectionsURL;
	}

	public boolean isAuthSSO() {
		return connSSOBean != null;		
	}
	
	public boolean isAuthBasic() {
		return connBasicBean != null;		
	}
}
