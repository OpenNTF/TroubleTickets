package nsf;

import java.io.IOException;
import java.util.HashMap;

import javax.faces.context.FacesContext;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.sbt.services.client.ClientServicesException;
import com.ibm.sbt.services.client.SBTServiceException;
import com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamService;
import com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamServiceException;
import com.ibm.sbt.services.client.connections.profiles.Profile;
import com.ibm.sbt.services.client.connections.profiles.ProfileService;
import com.ibm.sbt.services.client.connections.profiles.ProfileServiceException;
import com.ibm.sbt.services.endpoints.ConnectionsOAuth2Endpoint;
import com.ibm.sbt.services.endpoints.EndpointFactory;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class XPagesTickets {
	
	public static boolean updateNotifcation(String documentId){
		StringBuilder b = new StringBuilder(256);
		String json = "{\"actor\": { \"id\": \"\"},\"verb\": \"assigned\",\"object\": {\"id\": \"\"},\"connections\": {\"actionable\": \"false\"}}";
		try {
			JsonGenerator.toJson(JsonJavaFactory.instance,b,json,true);
		} catch (JsonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ActivityStreamService svc = new com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamService("connections");
		
		try {
			Object msg = svc.updateData("/connections/opensocial/basic/rest/activitystreams/@me/@actions/@all/activitystreams:"+documentId, null,json,null);
		} catch (ClientServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static String getInternetEmail(String canonical) {
		Session session = ExtLibUtil.getCurrentSession(FacesContext
				.getCurrentInstance());
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
	
	/**
	 * Cleans up the id 
	 * @param id
	 * @return
	 */
	public static String cleanupId(String id){
		return id.replace("urn:lsid:lconn.ibm.com:profiles.person:","");
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

			com.ibm.sbt.services.client.connections.profiles.ProfileService profileService = new ProfileService(
					"connections");

			/*
			 * Gets the Profile Information
			 */
			Profile profile = profileService.getProfile(email);
			result = profile.getUserid();

		} catch (ProfileServiceException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Generates a Post to an Activity Stream
	 * 
	 * @param iconUrl
	 * @param homePageUrl
	 * @param submitterId
	 * @param assigneeId
	 * @param uniDocId
	 * @param docId
	 * @return
	 */
	public static String generatePost(String iconUrl, String homePageUrl,
			String submitterId, String assigneeId, String uniDocId, String docId) {
		
		/**
		 * Creating as an UNID
		 */
		assigneeId = "urn:lsid:lconn.ibm.com:profiles.person:"  +assigneeId;
		submitterId = "urn:lsid:lconn.ibm.com:profiles.person:" +submitterId;
		
		JsonJavaObject activity = new com.ibm.commons.util.io.json.JsonJavaObject();

		JsonJavaObject image = new com.ibm.commons.util.io.json.JsonJavaObject();
		image.putJsonProperty("url", iconUrl);

		JsonJavaObject generator = new com.ibm.commons.util.io.json.JsonJavaObject();
		generator.putJsonProperty("image", image);
		generator.putJsonProperty("id", "TroubleTicketsGener");
		generator.putJsonProperty("displayName", "Trouble Ticket");
		generator.putJsonProperty("url", homePageUrl);
		activity.putJsonProperty("generator", generator);

		JsonJavaObject actor = new com.ibm.commons.util.io.json.JsonJavaObject();
		actor.putJsonProperty("id", submitterId);
		activity.putJsonProperty("actor", actor);

		activity.putJsonProperty("verb", "assigned");
		activity.putJsonProperty("title",
				"${Actor} assigned a ${Object} in the ${Target} to you.");
		activity.putJsonProperty("content",
				"A new trouble ticket has been assigned to you.");

		JsonJavaObject embed = new com.ibm.commons.util.io.json.JsonJavaObject();
		embed.putJsonProperty("url", nsf.XPagesTickets.getEEUrl()+"&platform=connections");

		JsonJavaObject deliverTo = new com.ibm.commons.util.io.json.JsonJavaObject();
		deliverTo.putJsonProperty("objectType", "person");
		deliverTo.putJsonProperty("id", assigneeId);

		JsonJavaObject[] deliverToArray = new JsonJavaObject[1];
		deliverToArray[0] = deliverTo;

		JsonJavaObject opensocial = new com.ibm.commons.util.io.json.JsonJavaObject();
		opensocial.putJsonProperty("embed", embed);
		opensocial.putJsonProperty("deliverTo", deliverToArray);

		activity.putJsonProperty("openSocial", opensocial);

		JsonJavaObject object = new com.ibm.commons.util.io.json.JsonJavaObject();
		object.putJsonProperty("summary", docId);
		object.putJsonProperty("objectType", "ticket");
		object.putJsonProperty("id", uniDocId);
		object.putJsonProperty("displayName", "Trouble Ticket");
		object.putJsonProperty("url", nsf.XPagesTickets.getTicketUrl());

		activity.putJsonProperty("object", object);

		JsonJavaObject target = new com.ibm.commons.util.io.json.JsonJavaObject();
		target.putJsonProperty("summary", "Trouble Ticket App");
		target.putJsonProperty("objectType", "application");
		target.putJsonProperty("id", "TroubleTicket");
		target.putJsonProperty("displayName", "Trouble Ticket App");
		target.putJsonProperty("url", homePageUrl);

		activity.putJsonProperty("target", target);

		JsonJavaObject conn = new com.ibm.commons.util.io.json.JsonJavaObject();
		conn.putJsonProperty("rollupid", uniDocId);
		conn.putJsonProperty("actionable", "true");

		activity.putJsonProperty("connections", conn);

		HashMap<String, String> header = new java.util.HashMap<String, String>();
		header.put("Content-Type", "application/json");
		String activityId = null;
		try {
			ActivityStreamService svc = new ActivityStreamService(
					"connections");

			activityId = svc.postEntry("@me", "@public","@all", activity);

		} catch (ActivityStreamServiceException e) {
			e.printStackTrace();
		}
		System.out.println("post successful"+activityId);
		if(activityId!=null)
			activityId=activityId.replace("urn:lsid:lconn.ibm.com:activitystreams:", "" );
		return activityId;
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

	/**
	 * 
	 * @param email
	 * @param ticketLink
	 * @param eeUrl
	 */
	public static void sendAssigneeEmail(String email, String ticketLink,
			String eeUrl) {
		Session session = ExtLibUtil.getCurrentSession(FacesContext
				.getCurrentInstance());
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
			doc.appendItemValue("Subject",
					"A New Trouble Ticket Has Been Assigned To You");
			doc.appendItemValue("SendTo", email);

			boolean convertMime = session.isConvertMime();
			session.setConvertMIME(false);

			MIMEEntity topMime = doc.createMIMEEntity("Body");

			// Set up the multipart header
			MIMEHeader header = topMime.createHeader("Content-Type");

			// EE needs to use multipart/alternative or the email clients will
			// turn the
			// content into an attachment.
			header.setHeaderVal("multipart/alternative");
			header = topMime.createHeader("MIME-Version");
			header.setHeaderVal("1.0");
			header = topMime.createHeader("Content-transfer-encoding");
			header.setHeaderVal("7bit");

			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("A new trouble ticket was assigned to you. "
					+ ticketLink);
			mime.setContentFromText(stream, "text/plain",
					MIMEEntity.ENC_IDENTITY_7BIT);
			stream.close();
			stream.recycle();
			mime.recycle();

			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("A new <a href=\"" + ticketLink
					+ "\">trouble ticket</a> was assigned to you.");
			mime.setContentFromText(stream, "text/html",
					MIMEEntity.ENC_IDENTITY_7BIT);
			stream.close();
			stream.recycle();
			mime.recycle();

			mime = topMime.createChildEntity();
			stream = session.createStream();
			stream.writeText("{\"url\":\"" + eeUrl + "\"}");
			mime.setContentFromText(stream, "application/embed+json",
					MIMEEntity.ENC_IDENTITY_7BIT);
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
		if (i != -1) {
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
			if (b != null) {
				b.recycle();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}

	public static String createHTMLPart() {
		return "A new <a href=\"" + getTicketUrl()
				+ "\">trouble ticket</a> was assigned to you.";
	}
	public static String createEscalatedHTMLPart() {
		return "A <a href=\"" + getTicketUrl()
				+ "\">trouble ticket</a> was escalated to you.";
	}

	public static String createTextPart() {
		return "A new trouble ticket was assigned to you. " + getTicketUrl();
	}
	public static String createEscalatedTextPart() {
		return "A trouble ticket was escalated. " + getTicketUrl();
	}
	public static String createJSONPart() {
		String url = getEEUrl();
		return "{\"url\":\"" + url + "\"}";
	}
	

}
