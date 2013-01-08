package nsf;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import sbt.ConnectionsService;
import sbt.XmlNavigator;

import com.ibm.xsp.extlib.sbt.services.client.ClientServicesException;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class XPagesTickets {
	
	public static String getInternetEmail(String canonical) {
		Session session = ExtLibUtil.getCurrentSession(FacesContext.getCurrentInstance());
		Database db = null;
		View usersView = null;
		Document personDoc = null;
		try {
			db = session.getDatabase("", "names.nsf");
			usersView = db.getView("$Users");
			personDoc = usersView.getDocumentByKey(canonical, false);
			String email = personDoc.getItemValueString("InternetAddress");
			return email;
		} catch (NotesException e) {
			e.printStackTrace();
		} finally {
			recycle(personDoc);
			recycle(usersView);
			recycle(db);
		}
		return null;
	}
	
	public static String getConUserId(String email) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		ConnectionsService conService = new ConnectionsService("/profiles/atom/profile.do");
		try {
			Object obj = conService.get(params, "xml");
			if(obj instanceof org.w3c.dom.Document) {
				org.w3c.dom.Document doc = (org.w3c.dom.Document)obj;
				XmlNavigator xmlNav = new XmlNavigator(doc);
				return xmlNav.stringValue("feed/entry/contributor/userid");
			}
		} catch (ClientServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getHomePageUrl() {
		 try {
			return ExtLibUtil.getCurrentDatabase().getHttpURL();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static void sendAssigneeEmail(String email, String ticketLink, String eeUrl) {
		Session session = ExtLibUtil.getCurrentSession(FacesContext.getCurrentInstance());
		lotus.domino.DbDirectory dir = null;
		Database mail = null;
		Document doc = null;
		MIMEEntity mime = null;
		lotus.domino.Stream stream = null;
		try {
			dir = session.getDbDirectory("");
			mail = dir.openMailDatabase();
			doc = mail.createDocument();
			doc.appendItemValue("Form", "Memo");
			doc.appendItemValue("Subject", "A New Trouble Ticket Has Been Assigned To You");
			doc.appendItemValue("SendTo", email);
			
			boolean convertMime = session.isConvertMime();
			session.setConvertMIME(false);

			MIMEEntity topMime = doc.createMIMEEntity("Body");

			// Set up the multipart header
			MIMEHeader header = topMime.createHeader("Content-Type");

			// EE needs to use multipart/alternative or the email clients will turn the
			// content into an attachment.
			header.setHeaderVal("multipart/alternative");
			header = topMime.createHeader("MIME-Version");
			header.setHeaderVal("1.0");
			header = topMime.createHeader("Content-transfer-encoding");
			header.setHeaderVal("7bit");
			
			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("A new trouble ticket was assigned to you. " + ticketLink);
			mime.setContentFromText(stream, "text/plain", MIMEEntity.ENC_IDENTITY_7BIT);
			stream.close();
			stream.recycle();
			mime.recycle();
			
			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("A new <a href=\"" + ticketLink + "\">trouble ticket</a> was assigned to you.");
			mime.setContentFromText(stream, "text/html", MIMEEntity.ENC_IDENTITY_7BIT);
			stream.close();
			stream.recycle();
			mime.recycle();
			
			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("{\"url\":\"" + eeUrl + "\"}");
			mime.setContentFromText(stream, "application/embed+json", MIMEEntity.ENC_IDENTITY_7BIT);
			stream.close();
			stream.recycle();
			mime.recycle();
			
			doc.save();
			doc.send();
			
			// Set mime conversion back to what it was originally
			session.setConvertMIME(convertMime);

		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			recycle(dir);
			recycle(mail);
			recycle(doc);
		}
	}
	
	public static String getIconUrl() {
		String url = getHomePageUrl();
		int i = url.indexOf("?");
		if(i != -1) {
			url = url.substring(0, i);
		}
		return url + "/assigned.png";
	}
	
	public static String getTicketUrl() {
		return ExtLibUtil.getXspContext().getUrl().toString();
	}
	
	public static String getEEUrl() {
		return getTicketUrl().replace("viewTicket.xsp", "myViewTicket.xsp");
	}
	
	private static void recycle(Base b) {
		try {
			if(b != null) {
				b.recycle();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}
	
	public static String createHTMLPart()
	{
		return "A new <a href=\"" + getTicketUrl() + "\">trouble ticket</a> was assigned to you.";
	}
	
	public static String createTextPart()
	{
		return "A new trouble ticket was assigned to you. " + getTicketUrl();
	}
	
	public static String createJSONPart()
	{
		String url = getEEUrl();
		return "{\"url\":\"" + url + "\"}";
	}

}
