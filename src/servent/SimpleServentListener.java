package servent;

import app.AppConfig;
import app.Cancellable;
import servent.handler.*;
import servent.handler.faultDetection.*;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	public SimpleServentListener() {
		
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
					case NEW_NODE:
						messageHandler = new NewNodeHandler(clientMessage);
						break;
					case WELCOME:
						messageHandler = new WelcomeHandler(clientMessage);
						break;
					case SORRY:
						messageHandler = new SorryHandler(clientMessage);
						break;
					case UPDATE:
						messageHandler = new UpdateHandler(clientMessage);
						break;
					case PUT:
						messageHandler = new PutHandler(clientMessage);
						break;
					case ASK_GET:
						messageHandler = new AskGetHandler(clientMessage);
						break;
					case TELL_GET:
						messageHandler = new TellGetHandler(clientMessage);
						break;
					case TOKEN_REQUEST:
						messageHandler=new TokenRequestHandler(clientMessage);
						break;
					case TOKEN_REPLY:
						messageHandler=new TokenReplyHandler(clientMessage);
						break;
					case BACKUP:
						messageHandler = new BackupHandler(clientMessage);
						break;
					case PING:
						messageHandler= new PingHandler(clientMessage);
						break;
					case PONG:
						messageHandler=new PongHandler(clientMessage);
						break;
					case SUS:
						messageHandler= new SusHandler((SusMessage) clientMessage);
						break;
					case SUS_RESPONSE:
						messageHandler=new SusResponseHandler((SusResponseMessage) clientMessage);
						break;
					case REMOVED_NODE:
						messageHandler=new RemovedNodeHandler(clientMessage);
						break;
					case ASK_VIEW:
						messageHandler=new AskViewHandler((AskViewMessage) clientMessage);
						break;
					case TELL_VIEW:
						messageHandler=new TellViewHandler((TellViewMessage) clientMessage);
						break;
					case ASK_DELETE, TELL_DELETE:
						messageHandler=new DeleteHandler(clientMessage);
						break;
                    case POISON:
						break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
