package cli.command;

import app.AppConfig;
import app.ChordState;
import app.file.SavedFile;

import java.io.File;

public class DHTPutCommand implements CLICommand {

	@Override
	public String commandName() {
		return "dht_put";
	}

	@Override
	public void execute(String args) {
		String[] splitArgs = args.split(" ");

//		if (splitArgs.length == 2) {
//			int key = 0;
//			int value = 0;
//			try {
//				key = Integer.parseInt(splitArgs[0]);
//				value = Integer.parseInt(splitArgs[1]);
//
//				if (key < 0 || key >= ChordState.CHORD_SIZE) {
//					throw new NumberFormatException();
//				}
//				if (value < 0) {
//					throw new NumberFormatException();
//				}
//
//				AppConfig.chordState.putValue(splitArgs[0], new SavedFile(new File(splitArgs[0])));
//			} catch (NumberFormatException e) {
//				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= " + ChordState.CHORD_SIZE
//						+ ". 0 <= value.");
//			}
//		} else {
//			AppConfig.timestampedErrorPrint("Invalid arguments for put");
//		}

		if(splitArgs.length==1){
			AppConfig.chordState.putValue(splitArgs[0], new SavedFile(new File(AppConfig.WORKSPACE_DIR+splitArgs[0])));
		} else if (splitArgs.length==2){
			//TODO: za private i puyblic
			AppConfig.chordState.putValue(splitArgs[0], new SavedFile(new File(AppConfig.WORKSPACE_DIR+splitArgs[0]),splitArgs[1] ));
		}
		else
			AppConfig.timestampedErrorPrint("invalid args for put");


	}

}
