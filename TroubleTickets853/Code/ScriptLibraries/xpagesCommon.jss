var dateFormatter = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT);

/********************************************************/
/*  Roles                                               */
/********************************************************/
var roles = {
	ROLE_MANAGER:string = "[manager]",
	ROLE_STAFF:string = "[staff]",

	isManager: function() {	
		return roles.hasRole(roles.ROLE_MANAGER);
	},

	isStaff: function() {	
		return roles.hasRole(roles.ROLE_STAFF);
	},

	hasRole: function(role:string) {
		var values:java.util.Vector = roles.getRoles();
		return values.contains(role);
	},

	getRoles: function() {
		var userRoles:java.util.Vector = sessionScope.userRoles;
		if (!userRoles) {
			sessionScope.userRoles = userRoles = session.evaluate("@UserRoles");
		}
		return userRoles;
	}
}


/********************************************************/
/*  JSON                                                */
/********************************************************/
var jsonUtil = {
	toJsArray: function(vector:java.util.Vector) {
		var items = new Array();
		for (var element in vector) {
			items.push(element);
		}
		return items;
	},
	
	toJson: function(vector:java.util.Vector) {
		if (vector != null) {
			return toJson(this.toJsArray(vector));
		}
	},
	
	getObjectFromDoc: function(doc:Document, field:string) {
		var json = doc.getItemValueString(field);
		return (json && json.length > 0) ? fromJson(json) : [];
	}
}

/********************************************************/
/*  DOCUMENT                                            */
/********************************************************/
var docUtil = {
	getDateTimeField: function(doc:Document, fieldName:string) {
		var vector:java.util.Vector = doc.getItemValueDateTimeArray(fieldName);
		return (vector.size() > 0 ? vector.get(0) : null);
	}
}

/********************************************************/
/*  Navigation Utilities                                */
/********************************************************/
var navUtil = {
	setBackPage : function() {
		var search = context.getUrlParameter("search");
		sessionScope.backPage = view.getPageName() + (search.length > 0 ? "?search=" + escape(search) : "");
	},
	
	getBackPage : function() {
		return sessionScope.backPage ? sessionScope.backPage : "welcome.xsp"; 
	},
	
	gotoBackPage : function() {
		context.redirectToPage(navUtil.getBackPage());
	}
}

/********************************************************/
/*  MISC                                                */
/********************************************************/
// universal date formatter
function formatDate(dateValue) {
	if (!dateValue) {
		return "";
	}
	var date:Date;
	
	// handle NotesDateTime
	if ((typeof dateValue).indexOf("lotus") != -1) {
		var notesDate:NotesDateTime = dateValue;
		date = notesDate.toJavaDate();
	}
	
	// format and return
	if (!date) {
		return "";
	}
	return dateFormatter.format(date);
}

function setConfirmationMessage(message) {
	sessionScope.confirmMessage = message;
}

function setInformationMessage(message) {
	sessionScope.infoMessage = message;
}