package servent.handler;

import app.AppConfig;
import app.file.SavedFile;
import servent.message.Message;
import servent.message.MessageType;

import java.io.File;

public class PutHandler implements MessageHandler {

	private Message clientMessage;
	
	public PutHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PUT) {
			String[] splitText = clientMessage.getMessageText().split(":");
			if (splitText.length == 2) {
					AppConfig.chordState.putValue(splitText[0], new SavedFile(new File(AppConfig.WORKSPACE_DIR+splitText[0])));
			} else {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}
		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
