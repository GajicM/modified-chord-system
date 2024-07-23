package cli.command;

import app.AppConfig;
import app.file.SavedFile;
import servent.SKTokenMutex;

public class DHTGetCommand implements CLICommand {

	@Override
	public String commandName() {
		return "dht_get";
	}

	@Override
	public void execute(String args) {
		try {
			String key = args;

			SavedFile val = AppConfig.chordState.getValue(key);
			if (val == null) {
				AppConfig.timestampedStandardPrint("No such key: " + key);
			} else {
				AppConfig.timestampedStandardPrint("dht_get execute"+key + ": " + val);
			}
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Invalid argument for dht_get: " + args + ". Should be key, which is an int.");
		} catch (Exception e) {
			AppConfig.timestampedStandardPrint("Please wait...");
        }
    }

}
