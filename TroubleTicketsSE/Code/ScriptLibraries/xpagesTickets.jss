function postSave(doc:NotesDocument) {	
	var agent:NotesAgent = database.getAgent("xpagesPostSave");
	agent.runWithDocumentContext(doc);
}

function submitTicket(doc:NotesDocument) {	
	var agent:NotesAgent = database.getAgent("xpagesSubmitTicket");
	agent.runWithDocumentContext(doc);
}

function assignTicket(doc:NotesDocument, assignedTo) {
	var connEnabled = isConnectionsEnabled();
	if(connEnabled) {
		var assigneeEmail = nsf.XPagesTickets.getInternetEmail(assignedTo);
		
		var submitted=doc.getItemValueString('SubmittedBy');
		//fix this assignee is the same as submitter!
		var submitterEmail = nsf.XPagesTickets.getInternetEmail(assigneeEmail);
		
		var assigneeId = nsf.XPagesTickets.getConUserId(assigneeEmail);
		print('assigneeId='+assigneeId);
		var submitterId = nsf.XPagesTickets.getConUserId(submitterEmail);
		print('submitterId='+submitterId);
		var homePageUrl = nsf.XPagesTickets.getHomePageUrl();
		var iconUrl = nsf.XPagesTickets.getIconUrl();
		var eeUrl = nsf.XPagesTickets.getEEUrl();
		print("ee="+eeUrl);
		var ticketUrl = nsf.XPagesTickets.getTicketUrl();
		print("ticketUrl="+ticketUrl);
			
		var docId = doc.getItemValueString('Summary');
		var uniDocId = doc.getUniversalID();
		
		var id=nsf.XPagesTickets.generatePost( iconUrl,  homePageUrl,  submitterId,  assigneeId,  uniDocId,  docId);			
	}
	
	// create temp, in-memory doc to pass arguments to agent
	var req:NotesDocument = database.createDocument();
	req.replaceItemValue("unid", doc.getUniversalID());
	req.replaceItemValue("assignTo", assignedTo);
	if(connEnabled) {
		req.replaceItemValue("activityId", id);
	}else{
		req.replaceItemValue("activityId", doc.getUniversalID());
	}
	
	// execute agent which is a wrapper to invoke same LotusScript code the Notes client uses
	var agent:NotesAgent = database.getAgent("xpagesAssignTicket");
	agent.runWithDocumentContext(req);
	
	req.recycle();
}

function escalateTicket(doc:NotesDocument, escalatedTo) {
	var connEnabled = isConnectionsEnabled();
	if(connEnabled) {
		var submitted=doc.getItemValueString('SubmittedBy');
		var submitterEmail = nsf.XPagesTickets.getInternetEmail(submitted);
		var submitterId = nsf.XPagesTickets.getConUserId(submitterEmail);
		var homePageUrl = nsf.XPagesTickets.getHomePageUrl();
			
		var summary = doc.getItemValueString('Summary');
		var id=nsf.XPagesTickets.generateEscalationPost(summary, submitterId, escalatedTo);			
	}
}

function postCommunityUpdate(doc:NotesDocument, poster) {
	var communityId = "urn:lsid:lconn.ibm.com:communities.community:"  +getCommunityId();

	var submitterEmail = nsf.XPagesTickets.getInternetEmail(poster);
	print('submitterEmail='+submitterEmail);
	var submitterId = nsf.ConnectionsBean.getConUserId(submitterEmail);
	print('submitterId='+submitterId);
	
	var endpoint = nsf.ConnectionsBean.getSSOBean("connections");
	var activitySvc = new com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamService(endpoint);

	var uniDocId = doc.getUniversalID();
	var title = doc.getItemValueString('Summary');
	
	var postPayload = new com.ibm.commons.util.io.json.JsonJavaObject();
    var actor =new com.ibm.commons.util.io.json.JsonJavaObject();
    var object =new com.ibm.commons.util.io.json.JsonJavaObject();
    actor.put("id", "@self");
    object.put("objectType", "Post");
    var randomGen = java.util.Random(19580427);
    var randomNum = randomGen.nextInt();
    object.put("id", randomNum);
    object.put("displayName", poster + " created a new ticket: " + title);

    postPayload.put("actor", actor);
    postPayload.put("verb", "created");
    postPayload.put("title", poster + " created a new ticket: " + title);
    postPayload.put("object", object);

    var entry = activitySvc.postEntry(communityId, "@public", "@all", postPayload);
}

function closeTicket(doc:NotesDocument, resolution) {
	// create temp, in-memory doc to pass arguments to agent
	var req:NotesDocument = database.createDocument();
	req.replaceItemValue("unid", doc.getUniversalID());
	req.replaceItemValue("resolution", resolution);
	
	// execute agent which is a wrapper to invoke same LotusScript code the Notes client uses
	var agent:NotesAgent = database.getAgent("xpagesCloseTicket");
	agent.runWithDocumentContext(req);
	
	req.recycle();
}

function addComment(doc:NotesDocument, comment) {
	// create temp, in-memory doc to pass arguments to agent
	var req:NotesDocument = database.createDocument();
	req.replaceItemValue("unid", doc.getUniversalID());
	req.replaceItemValue("comment", comment);
	
	// execute agent which is a wrapper to invoke same LotusScript code the Notes client uses
	var agent:NotesAgent = database.getAgent("xpagesAddComment");
	agent.runWithDocumentContext(req);
	
	req.recycle();
}

function isDraft(doc:NotesDocument) {
	var status = doc.getItemValueString("Status"); 
	return ("" != status && status == "Draft");
}

function isClosed(doc:NotesDocument) {
	var status = doc.getItemValueString("Status"); 
	return ("" != status && status == "Closed");
}

function isOpen(doc:NotesDocument) {
	var status = doc.getItemValueString("Status"); 
	return ("" != status && (status == "Submitted" || status == "Assigned"));
}

function getConnectionsURL() {
	var url = BeanFactory.getConnectionsURL();
	if(null == url || "" == url) {
		var idStoreView:NotesView = database.getView("idStoreLookup");
		if(null != idStoreView){
			var idStoreDoc = idStoreView.getFirstDocument();
			if(null != idStoreDoc){
				url = idStoreDoc.getItemValueString("URL");
			}
		}
	}
	return url;
}

function getCommunityId() {
	var communityId = "";
	var idStoreView:NotesView = database.getView("idStoreLookup");
	if(null != idStoreView){
		var idStoreDoc = idStoreView.getFirstDocument();
		if(null != idStoreDoc){
			communityId = idStoreDoc.getItemValueString("communityId");
		}
	}
	return communityId;
}

function getForumId() {
	var forumId = "";
	var idStoreView:NotesView = database.getView("idStoreLookup");
	if(null != idStoreView){
		var idStoreDoc = idStoreView.getFirstDocument();
		if(null != idStoreDoc){
			forumId = idStoreDoc.getItemValueString("forumId");
		}
	}
	return forumId;
}

function getEndpoint(endpointName:String) {
	var endpoint = null;
	
	//Retrieve the login type for Connections: 'BASIC' or 'SSO'
	var loginType = "";
	var idStoreView:NotesView = database.getView("idStoreLookup");
	if(null != idStoreView){
		var idStoreDoc = idStoreView.getFirstDocument();
		if(null != idStoreDoc){
			loginType = idStoreDoc.getItemValueString("Login");
		}
	}
	
	//Based on the login type, retrieve the correct endpoint from the EndpointFactory
	if("" != loginType) {
		if(loginType == BeanFactory.CONNECTIONS_BASIC) {
			endpoint = BeanFactory.getBasicBean(BeanFactory.CONNECTIONS);
			if(null == endpoint) {
				endpoint = BeanFactory.createBasicBean(endpointName, getConnectionsURL());
			}
		}else if(loginType == BeanFactory.CONNECTIONS_SSO) {
			endpoint = BeanFactory.getSSOBean(BeanFactory.CONNECTIONS);
			if(null == endpoint) {
				endpoint = BeanFactory.createSSOBean(endpointName, getConnectionsURL());
			}
		}
	}
	//if(null != endpoint && endpoint instanceof ConnectionsSSOEndpoint && !endpoint.isAuthenticated()) {
	//	endpoint.authenticate(true);
	//}
	return endpoint;
}
