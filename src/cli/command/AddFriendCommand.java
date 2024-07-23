package cli.command;

import app.AppConfig;
import app.file.SavedFile;

import java.io.File;
import java.util.List;

public class AddFriendCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add_friend";
    }

    @Override
    public void execute(String args) {
        try {

           Boolean val=AppConfig.chordState.addFriend(args);
            if (val == null) {
                AppConfig.timestampedStandardPrint("No such key: " + args);
            } else {
                AppConfig.timestampedStandardPrint("add_files execute  "+args + ": " + val);
            }
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Invalid argument for view_files: " + args + ". Should be key, which is an int.");
        } catch (Exception e) {
            AppConfig.timestampedStandardPrint("Please wait...");
        }
    }

}