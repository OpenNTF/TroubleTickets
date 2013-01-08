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
	
	//var assigneeId = nsf.XPagesTickets.getConUserId("FrankAdams@renovations.com");
	var assigneeId = nsf.XPagesTickets.getConUserId(assigneeEmail);
	print('assigneeId='+assigneeId);
	//var submitterId = nsf.XPagesTickets.getConUserId("SamanthaDaryn@renovations.com");
	var submitterId = nsf.XPagesTickets.getConUserId(submitterEmail);
	print('submitterId='+submitterId);
	var homePageUrl = nsf.XPagesTickets.getHomePageUrl();
	var iconUrl = nsf.XPagesTickets.getIconUrl();
	var eeUrl = nsf.XPagesTickets.getEEUrl();
	print("ee="+eeUrl);
	var ticketUrl = nsf.XPagesTickets.getTicketUrl();
	print("ticketUrl="+ticketUrl);
	
//	nsf.XPagesTickets.sendAssigneeEmail(assigneeEmail, ticketUrl, eeUrl);
	
	var activity = {
		    "generator": {
		        "image": {
		            "url": iconUrl
		        },
		        "id": "TroubleTicketsGener",
		        "displayName": "Trouble Ticket",
		        "url": homePageUrl
		    },
		    "actor": {
		        "id": submitterId
		    },
		    "verb": "assigned",
		    "title": "${Actor} assigned a ${Object} in the ${Target} to you.",
		    "content": "A new trouble ticket has been assigned to you.",
		    "updated": "2012-01-01T12:00:00.000Z",
		    "object": {
		        "summary": doc.getItemValueString('Summary'),
		        "objectType": "ticket",
		        "id": doc.getUniversalID(),
		        "displayName": "Trouble Ticket",
		        "url": nsf.XPagesTickets.getTicketUrl()
		    },
		    "target": {
		        "summary": "Trouble Ticket App",
		        "objectType": "application",
		        "id": "TroubleTicket",
		        "displayName": "Trouble Ticket App",
		        "url": homePageUrl
		    },
		    "openSocial": {
		        "embed": {
		            "url" : nsf.XPagesTickets.getEEUrl()
		        },
		        "deliverTo" : [{
                    "objectType" : "person",
                    "id" : assigneeId
                }]
		    },
		    "connections": {
		        "rollupid": doc.getUniversalID(),
		        "actionable": "true"
		    }
		};
			var svc = new sbt.ActivityStreamsService(@Endpoint('connections'),
					'/connections/opensocial/basic/rest/activitystreams/@public/@all/@all');
			var msg = svc.post(null, activity);


	// create temp, in-memory doc to pass arguments to agent
	var req:NotesDocument = database.createDocument();
	req.replaceItemValue("unid", doc.getUniversalID());
	req.replaceItemValue("assignTo", assignedTo);
	
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