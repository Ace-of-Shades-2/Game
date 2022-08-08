package nl.andrewl.aos2_server;

import nl.andrewl.aos2_server.cli.ingame.PlayerCommandHandler;
import nl.andrewl.aos2_server.model.ServerPlayer;
import nl.andrewl.aos_core.net.client.ChatMessage;
import nl.andrewl.aos_core.net.client.ChatWrittenMessage;

/**
 * Component that handles the various tasks associated with client chats.
 */
public class ChatManager {
	private final Server server;
	private final PlayerCommandHandler commandHandler;

	public ChatManager(Server server) {
		this.server = server;
		this.commandHandler = new PlayerCommandHandler(server);
	}

	public void handle(ClientCommunicationHandler handler, ServerPlayer player, ChatWrittenMessage msg) {
		if (msg.message().startsWith("/")) {
			commandHandler.handle(msg.message(), player, handler);
		} else {
			server.getPlayerManager().broadcastTcpMessage(new ChatMessage(
					System.currentTimeMillis(),
					player.getUsername(),
					msg.message()
			));
		}
	}
}
