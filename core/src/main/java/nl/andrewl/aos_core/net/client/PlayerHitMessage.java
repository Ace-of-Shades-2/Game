package nl.andrewl.aos_core.net.client;

import nl.andrewl.record_net.Message;

/**
 * A message that's sent to a single client to tell them that they hit a player
 * with some information about which player and where they got it.
 */
public record PlayerHitMessage(
		int playerId,
		float px, float py, float pz,
		boolean headshot
) implements Message {}
