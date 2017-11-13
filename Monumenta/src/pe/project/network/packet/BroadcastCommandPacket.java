package pe.project.network.packet;

import org.bukkit.Bukkit;

import pe.project.Main;
import pe.project.utils.PacketUtils;

public class BroadcastCommandPacket implements Packet {
	private String mCommand;

	public BroadcastCommandPacket(String command) {
		mCommand = command;
	}

	// TODO - Ugh, this is so annoying
	// Want to just be able to call BroadcastCommandPacket.getPacketChannel() without an object
	// But making that static just causes other problems
	public static String getStaticPacketChannel() {
		return "Monumenta.Bungee.Broadcast.BroadcastCommand";
	}

	@Override
	public String getPacketChannel() {
		return "Monumenta.Bungee.Broadcast.BroadcastCommand";
	}

	@Override
	public String getPacketData() throws Exception {
		String[] data = {mCommand};
		return PacketUtils.encodeStrings(data);
	}

	public static void handlePacket(Main main, String data) throws Exception {
		if (main.mServerProporties.getBroadcastCommandEnabled() == true) {
			String[] rcvStrings = PacketUtils.decodeStrings(data);
			if (rcvStrings == null || rcvStrings.length != 1) {
				throw new Exception("Received string data is null or invalid length");
			}

			main.getLogger().info("Executing broadcast received command '" + rcvStrings[0] + "'");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), rcvStrings[0]);
		}
	}
}