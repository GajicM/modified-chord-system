package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import app.file.SavedFile;
import servent.SKTokenMutex;
import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(String value) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			BigInteger hash = new BigInteger(1, messageDigest.digest(value.getBytes())).mod(BigInteger.valueOf(CHORD_SIZE));
			return hash.intValue();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	private HashSet<ServentInfo> friends;
	
	private Map<String, SavedFile> valueMap;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
		friends=new HashSet<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<String, SavedFile> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<String, SavedFile> valueMap) {
		this.valueMap = valueMap;
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(String k) {
		if (predecessorInfo == null) {
			return true;
		}
		int key;
		try{
		key=	Integer.parseInt(k);
		}catch (NumberFormatException e){
			key=chordHash(k);
		}
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(String key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (chordHash(key) < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= chordHash(key)) {
				return successorTable[i-1];
			}
			if (chordHash(key) > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (!newList2.isEmpty()) {
			predecessorInfo = newList2.getLast();
		} else if(!newList.isEmpty()) {
			predecessorInfo = newList.getLast();
		}
		
		updateSuccessorTable();
	}
	public boolean removeNode(ServentInfo node){
		synchronized (FaultDetection.deleteLock){

			AppConfig.timestampedStandardPrint(allNodeInfo.toString());
			boolean val=allNodeInfo.remove(node);

			if(val){
				allNodeInfo.sort(new Comparator<ServentInfo>() {

					@Override
					public int compare(ServentInfo o1, ServentInfo o2) {
						return o1.getChordId() - o2.getChordId();
					}

				});
				List<ServentInfo> allNodes=List.copyOf(allNodeInfo);
				allNodeInfo.clear();
				addNodes(allNodes);


				if(node==predecessorInfo){
					AppConfig.timestampedStandardPrint("YOOO HE WAS MY RPEDECESOR");
				}
				RemovedNodeMessage removedNodeMessage=new RemovedNodeMessage(AppConfig.myServentInfo.getListenerPort(),successorTable[0].getListenerPort(),node);
				MessageUtil.sendMessage(removedNodeMessage);

				for(Map.Entry<String,SavedFile> entry: backupMap.entrySet()){
					putValue(entry.getKey(),entry.getValue());
				}
				AppConfig.timestampedStandardPrint("after removing node "+node + "my valueMap is "+valueMap + "  predecesor "+predecessorInfo + "   suceccors : "+ Arrays.toString(successorTable));
			}

			return val;
		}

	}



	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(String key, SavedFile value) {

		if (isKeyMine(key)) {
			AppConfig.tokenMutex.requestCriticalSection();
			AppConfig.timestampedStandardPrint("I PUT IN MYSELF k:"+ key +" v:"+ value);
			valueMap.put(key, value);
			BackupMessage bm1=new BackupMessage(AppConfig.myServentInfo.getListenerPort(),getNextNodePort(),key,value);
			MessageUtil.sendMessage(bm1);
			if(getPredecessor()!=null){
				BackupMessage bm2=new BackupMessage(AppConfig.myServentInfo.getListenerPort(),getPredecessor().getListenerPort(),key,value);
				MessageUtil.sendMessage(bm2);
			}

			AppConfig.tokenMutex.releaseCriticalSection();

		} else {

			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);

		}


	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public SavedFile getValue(String key) throws Exception {

		if (isKeyMine(key)) {
			AppConfig.tokenMutex.requestCriticalSection();
			if (valueMap.containsKey(key)) {
				SavedFile f=valueMap.get(key);
				AppConfig.tokenMutex.releaseCriticalSection();
				return f;
			} else {
				return null;
			}

		}
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		throw new Exception("PLEASE WAIT");
	}
	public List<SavedFile> getValues(String addressPort,int originalAsker) throws Exception {
		int key= chordHash(addressPort);
		AppConfig.timestampedStandardPrint("usao ovde");
		if(AppConfig.myServentInfo.getChordId()==key){
			AppConfig.timestampedStandardPrint("usao  iii ovde");
			if(getValueMap().isEmpty())
				return new ArrayList<>();

			//TODO PROVERITI OVO
			List<SavedFile> toReturn=(List<SavedFile>) getValueMap().values();
			Optional<ServentInfo> originalAskerInfo=allNodeInfo.stream().filter(val->val.getChordId()==originalAsker).findFirst();
			return toReturn.stream().filter(val->(val.isPublic || originalAskerInfo.isPresent() && friends.contains(originalAskerInfo.get()))).toList();
		}
		ServentInfo nextNode = getNextNodeForKey(String.valueOf(key));
		AskViewMessage avm=new AskViewMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(),addressPort,originalAsker);
		MessageUtil.sendMessage(avm);
		throw new Exception("PLEASE WAIT");


	}





	public Map<String,SavedFile> backupMap=new HashMap<>();
	public void putBackupValue(String s, SavedFile savedFile) {
		AppConfig.timestampedStandardPrint("Im putting in my backup file "+ s);
		backupMap.put(s,savedFile);
		AppConfig.timestampedStandardPrint("Backup now has + "+backupMap);
	}

	public List<ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}

	public Boolean addFriend(String args) {
		ServentInfo toAdd=null;

		for(ServentInfo info:allNodeInfo){
			if(info.getChordId()==chordHash(args)){
				toAdd=info;
			}
		}
		if(toAdd!=null){
			boolean isAdded=friends.add(toAdd);;
			 if(isAdded){
				 AppConfig.timestampedStandardPrint("added friend "+toAdd);

			 }
			 return isAdded;
		}
		return null;
	}

	public boolean removeValue(String args,int originalAskerChordId) throws Exception {
		int key=chordHash(args);

		if(isKeyMine(String.valueOf(key))){
			AppConfig.tokenMutex.requestCriticalSection();
		  if(valueMap.remove(args)!=null){
			  backupMap.remove(args);

			 AppConfig.tokenMutex.releaseCriticalSection();
			  return true;
		  }
		  else {
			  AppConfig.tokenMutex.releaseCriticalSection();
			  throw new Exception();}
		}
		ServentInfo nextNode = getNextNodeForKey(String.valueOf(key));
		AskDeleteMessage askDeleteMessage=new AskDeleteMessage(AppConfig.myServentInfo.getListenerPort(),nextNode.getListenerPort(),args ,originalAskerChordId);
		MessageUtil.sendMessage(askDeleteMessage);

		return false;
	}

	public boolean removeValueFromBackup(String args){
		boolean isDeleted= backupMap.remove(args)!=null;

		AppConfig.timestampedStandardPrint(args +"isDeleted "+isDeleted+" from backup, backup now has"+backupMap );
		return isDeleted;

	}
}
