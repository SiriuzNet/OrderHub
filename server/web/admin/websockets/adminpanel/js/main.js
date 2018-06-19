//var apiURL = "localhost:8085";
//var apiURL = "siriuz.net:8085";
var apiURL = "178.62.79.249:8080";

$(function() {
	$("#showConsoleButton").click(function() {
		var console = $("#console");
		if (console.css("display") == "none")
			console.css("display", "block");
		else
			console.css("display", "none");
	});
	
	createWebSocket(apiURL, function() { 
		//sendCommand({ "template" : "login", "user" : "adminpanel", "password" : "one2tribeadmin" });
		sendCommand({ "template" : "login", "user" : "admin", "password" : "admin" });
	});

	ServerMessage.prototype.onLogin = function(data) {
		$("#getLanguageMapButton").click(function() {
			sendCommand({ "template" : "getLocalisationMap", "lang" : "en-us" });
		});
		$("#addNewTranslationKeyButton").click(function() {
			sendCommand({ "template" : "addNewTranslationKey", "key" : $("#newTranslationKey").val() });
		});
	};
	
	ServerMessage.prototype.onGetLocalisationMap = function(data) {
		var table = $("#localisationTable");
		table.empty();
		table.append("<th>Translation key</th><th>Localised text</th>");
		for (var key in data.map) {
			$("#localisationTable").append('<tr><td class="key">'+key+'</td><td><input class="localisedText" type="text" value="'+data.map[key]+'"/><button onClick="setLocalisedText(\''+key+'\',$(\'#language\').val(), $(this).parent().find(\'input\').val());">U</button></td></tr>');
		}
	};
});