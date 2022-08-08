package nl.andrewl.aos2_client.render;

import nl.andrewl.aos2_client.model.ClientPlayer;
import nl.andrewl.aos2_client.model.OtherPlayer;
import nl.andrewl.aos2_client.render.model.Model;
import nl.andrewl.aos_core.model.PlayerMode;
import nl.andrewl.aos_core.model.item.BlockItemStack;
import nl.andrewl.aos_core.model.item.Inventory;
import nl.andrewl.aos_core.model.item.ItemTypes;
import nl.andrewl.aos_core.model.world.World;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Collection;

public class PlayerRenderer {
	private final ModelRenderer modelRenderer;

	private final Model playerModel;
	private final Model rifleModel;
	private final Model smgModel;
	private final Model shotgunModel;
	private final Model blockModel;

	public PlayerRenderer(ModelRenderer modelRenderer) throws IOException {
		this.modelRenderer = modelRenderer;
		playerModel = new Model("model/player_simple.obj", "model/simple_player.png");
		rifleModel = new Model("model/rifle.obj", "model/rifle.png");
		smgModel = new Model("model/smg.obj", "model/smg.png");
		blockModel = new Model("model/block.obj", "model/block.png");
		shotgunModel = new Model("model/shotgun.obj", "model/shotgun.png");
	}

	public void render(ClientPlayer myPlayer, Collection<OtherPlayer> players, World world) {
		Inventory inv = myPlayer.getInventory();
		playerModel.bind();
		for (var player : players) {
			if (player.getMode() == PlayerMode.SPECTATOR) continue;
			if (player.getTeam() != null) {
				modelRenderer.setAspectColor(player.getTeam().getColor());
			} else {
				modelRenderer.setAspectColor(new Vector3f(0.3f, 0.3f, 0.3f));
			}
			modelRenderer.render(playerModel, player.getModelTransformData(), player.getNormalTransformData());
		}
		playerModel.unbind();

		// Render guns!
		rifleModel.bind();
		if (inv.getSelectedItemStack() != null && inv.getSelectedItemStack().getType().getId() == ItemTypes.RIFLE.getId()) {
			modelRenderer.render(rifleModel, myPlayer.getHeldItemTransformData(), myPlayer.getHeldItemNormalTransformData());
		}
		for (var player : players) {
			if (player.getMode() == PlayerMode.SPECTATOR) continue;
			if (player.getHeldItemId() == ItemTypes.RIFLE.getId()) {
				modelRenderer.render(rifleModel, player.getHeldItemTransformData(), player.getHeldItemNormalTransformData());
			}
		}
		rifleModel.unbind();
		smgModel.bind();
		if (inv.getSelectedItemStack() != null && inv.getSelectedItemStack().getType().getId() == ItemTypes.AK_47.getId()) {
			modelRenderer.render(smgModel, myPlayer.getHeldItemTransformData(), myPlayer.getHeldItemNormalTransformData());
		}
		for (var player : players) {
			if (player.getMode() == PlayerMode.SPECTATOR) continue;
			if (player.getHeldItemId() == ItemTypes.AK_47.getId()) {
				modelRenderer.render(smgModel, player.getHeldItemTransformData(), player.getHeldItemNormalTransformData());
			}
		}
		smgModel.unbind();
		shotgunModel.bind();
		if (inv.getSelectedItemStack() != null && inv.getSelectedItemStack().getType().getId() == ItemTypes.WINCHESTER.getId()) {
			modelRenderer.render(shotgunModel, myPlayer.getHeldItemTransformData(), myPlayer.getHeldItemNormalTransformData());
		}
		for (var player : players) {
			if (player.getMode() == PlayerMode.SPECTATOR) continue;
			if (player.getHeldItemId() == ItemTypes.WINCHESTER.getId()) {
				modelRenderer.render(shotgunModel, player.getHeldItemTransformData(), player.getHeldItemNormalTransformData());
			}
		}
		shotgunModel.unbind();
		blockModel.bind();
		if (inv.getSelectedItemStack() != null && inv.getSelectedItemStack().getType().getId() == ItemTypes.BLOCK.getId()) {
			BlockItemStack stack = (BlockItemStack) myPlayer.getInventory().getSelectedItemStack();
			modelRenderer.setAspectColor(world.getPalette().getColor(stack.getSelectedValue()));
			modelRenderer.render(blockModel, myPlayer.getHeldItemTransformData(), myPlayer.getHeldItemNormalTransformData());
		}
		modelRenderer.setAspectColor(new Vector3f(0.5f, 0.5f, 0.5f));
		for (var player : players) {
			if (player.getMode() == PlayerMode.SPECTATOR) continue;
			if (player.getHeldItemId() == ItemTypes.BLOCK.getId()) {
				modelRenderer.setAspectColor(world.getPalette().getColor(player.getSelectedBlockValue()));
				modelRenderer.render(blockModel, player.getHeldItemTransformData(), player.getHeldItemNormalTransformData());
			}
		}
		blockModel.unbind();
	}

	public void free() {
		playerModel.free();
		rifleModel.free();
		smgModel.free();
		shotgunModel.free();
		blockModel.free();
	}
}
