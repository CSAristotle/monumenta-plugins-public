package com.playmonumenta.plugins.commands;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.MessagingUtils;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.GreedyStringArgument;

public class Bot {
	private static final InetSocketAddress SOCKET_ADDR = new InetSocketAddress("127.0.0.1", 8765);
	private static final int BUF_SIZE = 8096;
	private static final int TICK_PERIOD = 40;
	private static final int IDLE_TIMEOUT_TICKS = 20 * 60 * 3;

	public static void register(Plugin plugin) {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("command", new GreedyStringArgument());
		CommandAPI.getInstance().register("bot",
		                                  CommandPermission.fromString("monumenta.command.bot"),
		                                  arguments,
		                                  (sender, args) -> {
		                                      run(plugin, sender, (String)args[0]);
		                                  }
		);
	}

	private static void run(Plugin plugin, CommandSender sender, String command) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player");
		}

		Player player = (Player)sender;

		player.sendMessage(ChatColor.GOLD + "Started processing bot command...");

		JsonObject reqObj = new JsonObject();
		reqObj.addProperty("player_name", player.getName());
		reqObj.addProperty("player_uuid", player.getUniqueId().toString());
		reqObj.addProperty("action", "command");
		reqObj.addProperty("command", command);

		initiateBotRequest(plugin, player, reqObj.toString(),
			(JsonObject message) -> {
				player.sendMessage(ChatColor.GOLD + "Got bot message: " + message.toString());
			},
			(JsonObject message, int result) -> {
				player.sendMessage(ChatColor.GOLD + "Got final bot message result:" + Integer.toString(result) + " msg: " + message.toString());
			}
		);
	}

	@FunctionalInterface
	public interface GotMessageExecutor {
		/**
		 * Called on each message received from the bot
		 */
		void run(JsonObject message);
	}

	@FunctionalInterface
	public interface RequestCompleteExecutor {
		/**
		 * Called once the bot returns a message with a result code
		 */
		void run(JsonObject message, int result);
	}

	public static void initiateBotRequest(Plugin plugin, Player player, String request, GotMessageExecutor onMsg, RequestCompleteExecutor onComplete) {
		try {
			/* Create an async socket channel and start the process of connecting */
			Selector selector = Selector.open();
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(SOCKET_ADDR);
			channel.register(selector, SelectionKey.OP_CONNECT);

			new BukkitRunnable() {
				private ByteBuffer mReadBuffer = ByteBuffer.allocate(BUF_SIZE);
				private int mNotConnectedTicks = 0;
				private boolean mConnected = false;
				private int mNoMessageTicks = 0;

				@Override
				public void run() {
					try {
						if (!mConnected) {
							if (mNotConnectedTicks > 600) {
								player.sendMessage(ChatColor.RED + "Failed to connect to bot in 30s - aborting request");
								this.cancel();
								return;
							}
							mNotConnectedTicks += TICK_PERIOD;
						}

						mNoMessageTicks += TICK_PERIOD;
						if (mNoMessageTicks > IDLE_TIMEOUT_TICKS) {
							player.sendMessage(ChatColor.RED + "No messages received from bot in 3 minutes - aborting request");
							this.cancel();
							return;
						}

						/* Sent a request - got any messages yet? NON-BLOCKING! */
						if (selector.selectNow() != 0) {
							Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
							while (iterator.hasNext()) {
								// This is a wrapper for the request
								SelectionKey key = iterator.next();

								// Clear this service request
								iterator.remove();

								if (key.isConnectable()) {
									SocketChannel client = (SocketChannel)key.channel();
									if (client.finishConnect()) {
										mConnected = true;

										mNoMessageTicks = 0;

										// Now that we are connected, can listen for read events
										channel.register(selector, SelectionKey.OP_READ);

										ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
										channel.write(buffer);
									}
								}

								// Parse the read data
								if (key.isReadable()) {
									SocketChannel client = (SocketChannel)key.channel();

									int len = client.read(mReadBuffer);
									if (len > 0) {
										mNoMessageTicks = 0;

										// Important to make buffer readable apparently
										mReadBuffer.flip();

										String message = StandardCharsets.UTF_8.decode(mReadBuffer).toString();

										Gson gson = new Gson();

										JsonObject object = gson.fromJson(message, JsonObject.class);
										if (!object.has("result")) {
											// An incremental message
											onMsg.run(object);
										} else {
											// The last message in this request
											onComplete.run(object, object.get("result").getAsInt());
											channel.close();
											this.cancel();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						player.sendMessage(ChatColor.RED + "Bot command failed (protocol error)");
						MessagingUtils.sendStackTrace(player, e);
						this.cancel();
					}
				}
			}.runTaskTimer(plugin, 0, TICK_PERIOD);
		} catch (IOException e) {
			MessagingUtils.sendStackTrace(player, e);
		}
	}
}
