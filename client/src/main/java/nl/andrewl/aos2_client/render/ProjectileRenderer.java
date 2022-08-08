package nl.andrewl.aos2_client.render;

import nl.andrewl.aos2_client.Camera;
import nl.andrewl.aos2_client.model.ModelTransformData;
import nl.andrewl.aos2_client.render.model.Model;
import nl.andrewl.aos_core.model.Projectile;

import java.io.IOException;
import java.util.Collection;

public class ProjectileRenderer {
	private final ModelRenderer modelRenderer;
	private final Model bulletModel;

	public ProjectileRenderer(ModelRenderer modelRenderer) throws IOException {
		this.modelRenderer = modelRenderer;
		bulletModel = new Model("model/bullet.obj", "model/bullet.png");
	}

	public void render(Collection<Projectile> projectiles) {
		bulletModel.bind();
		for (var p : projectiles) {
			ModelTransformData.COMMON.tx().identity()
					.translate(p.getPosition())
					.rotateTowards(p.getVelocity(), Camera.UP)
					.scale(1, 1, p.getVelocity().length() / 5);
			ModelTransformData.COMMON.updateData();
			modelRenderer.render(bulletModel, ModelTransformData.COMMON);
		}
		bulletModel.unbind();
	}

	public void free() {
		bulletModel.free();
	}
}
