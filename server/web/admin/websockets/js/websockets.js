var websocketHandler;

function ServerMessage() { }

ServerMessage.prototype.onLogin = function(data) { }

var serverMessage = new ServerMessage();

function createWebSocket(ipaddress, onOpen) {
	if (websocketHandler)
		websocketHandler.close();
	websocketHandler = new WebSocket("ws://"+ipaddress+"/websocket");
	websocketHandler.onopen = function(event) { $("#console").prepend('<p class="time">'+getTime()+'</p><p class="conConnection">Connected</p>'); onOpen(); };
	websocketHandler.onmessage = function(event) { consoleMessage(event.data); onServerMsg(JSON.parse(event.data)); };
	websocketHandler.onclose = function(event) { $("#console").prepend('<p class="time">'+getTime()+'</p><p class="conConnection">Disconnected</p>');  };
}

function sendCommand() {
	if (websocketHandler) {
		var cmd = $("#wscommand").val();
		websocketHandler.send(cmd);
		consoleMessageOut(cmd);
	} else {
		consoleError("Not connected");
	}
}

function getTime() {
	var ct = new XDate();
	return ct.toString("HH:mm:ss");
}

function consoleMessageOut(message) {
	$("#console").prepend('<p class="time">'+getTime()+'</p><p class="conMessageOut">'+message+'</p>');
}

function consoleMessage(message) {
	$("#console").prepend('<p class="time">'+getTime()+'</p><p class="conMessage">'+message+'</p>');
}

function consoleError(message) {
	$("#console").prepend('<p class="time">'+getTime()+'</p><p class="conError">ERROR: '+message+'</p>');
}


function onServerMsg(data) {
	console.log("Recieved message: %O", data);
	if (data.template) {
		switch(data.template) {
			case 'login':
				serverMessage.onLogin(data); break;
		}
	}
}