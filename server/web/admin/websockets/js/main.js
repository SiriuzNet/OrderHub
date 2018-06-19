var apiURL = "localhost:8081";

$(function() {
	$("#connectButton").click(function() {
		createWebSocket($("#wsaddress").val(), function() { });
	});
	
	$("#sendCommand").click(function() {
		sendCommand();
	});
	
	ServerMessage.prototype.onLogin = function(data) {
		
	};
});