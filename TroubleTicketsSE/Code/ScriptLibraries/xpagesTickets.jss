function postSave(doc:NotesDocument) {	
	var agent:NotesAgent = database.getAgent("xpagesPostSave");
	agent.runWithDocumentContext(doc);
}

function submitTicket(doc:NotesDocument) {	
	var agent:NotesAgent = database.getAgent("xpagesSubmitTicket");
	agent.runWithDocumentContext(doc);
}

function assignTicket(doc:NotesDocument, assignedTo) {
	
	var assigneeEmail = nsf.XPagesTickets.getInternetEmail(assignedTo);
	print('assigneeEmail='+assigneeEmail);
	var submitterEmail = nsf.XPagesTickets.getInternetEmail(doc.getItemValueString('SubmittedBy'));
	print('submitterEmail='+submitterEmail);
	
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
	
	var activityId=nsf.XPagesTickets.generatePost( iconUrl,  homePageUrl,  submitterId,  assigneeId,  uniDocId,  docId);			
	
	// create temp, in-memory doc to pass arguments to agent
	var req:NotesDocument = database.createDocument();
	req.replaceItemValue("unid", doc.getUniversalID());
	req.replaceItemValue("assignTo", assignedTo);
	req.replaceItemValue("activityId", activityId);
	print("activityId 1="+activityId);
	// execute agent which is a wrapper to invoke same LotusScript code the Notes client uses
	var agent:NotesAgent = database.getAgent("xpagesAssignTicket");
	agent.runWithDocumentContext(req);
	
	req.recycle();
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