package pe.project.server.properties;

import java.io.File;
import java.io.FileNotFoundException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import pe.project.Main;
import pe.project.utils.FileUtils;

public class ServerProperties {
	private final static String FILE_NAME = "Properties.json";

	private boolean mDailyResetEnabled = false;
	private boolean mJoinMessagesEnabled = false;
	private boolean mTransferDataEnabled = true;

	public boolean getDailyResetEnabled() {
		return mDailyResetEnabled;
	}

	public boolean getJoinMessagesEnabled() {
		return mJoinMessagesEnabled;
	}

	public boolean getTransferDataEnabled() {
		return mTransferDataEnabled;
	}

	public void load(Main main) {
		final String fileLocation = main.getDataFolder() + File.separator + FILE_NAME;

		try {
			String content = FileUtils.readFile(fileLocation);
			if (content != null && content != "") {
				_loadFromString(main, content);
			}
		} catch (FileNotFoundException e) {
			main.getLogger().info("Properties.json file does not exist - using default values" + e);
		} catch (Exception e) {
			main.getLogger().severe("Caught exception: " + e);
			e.printStackTrace();
		}
	}

	private void _loadFromString(Main main, String content) throws Exception {
		if (content != null && content != "") {
			try {
				Gson gson = new Gson();

				//	Load the file - if it exists, then let's start parsing it.
				JsonObject object = gson.fromJson(content, JsonObject.class);
				if (object != null) {
					JsonElement dailyResetEnabled = object.get("dailyResetEnabled");
					if (dailyResetEnabled != null) {
						mDailyResetEnabled = dailyResetEnabled.getAsBoolean();
						main.getLogger().info("Properties: dailyResetEnabled = " + mDailyResetEnabled);
					}

					JsonElement joinMessagesEnabled = object.get("joinMessagesEnabled");
					if (joinMessagesEnabled != null) {
						mJoinMessagesEnabled = joinMessagesEnabled.getAsBoolean();
						main.getLogger().info("Properties: joinMessagesEnabled = " + mJoinMessagesEnabled);
					}

					JsonElement TransferDataEnabled = object.get("transferDataEnabled");
					if (TransferDataEnabled != null) {
						mTransferDataEnabled = TransferDataEnabled.getAsBoolean();
						main.getLogger().info("Properties: transferDataEnabled = " + mTransferDataEnabled);
					}
				}
			} catch (Exception e) {
				main.getLogger().severe("Caught exception: " + e);
				e.printStackTrace();
			}
		}
	}
}
