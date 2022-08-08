package nl.andrewl.aos2_client.render;

import nl.andrewl.aos2_client.model.ModelTransformData;
import nl.andrewl.aos2_client.render.model.Model;
import nl.andrewl.aos_core.model.Team;

import java.io.IOException;
import java.util.Collection;

public class TeamRenderer {
	private final ModelRenderer modelRenderer;
	private final Model flagModel;

	public TeamRenderer(ModelRenderer modelRenderer) throws IOException {
		this.modelRenderer = modelRenderer;
		this.flagModel = new Model("model/flag.obj", "model/flag.png");
	}

	public void render(Collection<Team> teams) {
		flagModel.bind();
		for (Team team : teams) {
			ModelTransformData.COMMON.tx().identity()
					.translate(team.getSpawnPoint().x() - 0.25f, team.getSpawnPoint().y(), team.getSpawnPoint().z() - 0.25f);
			ModelTransformData.COMMON.updateData();
			modelRenderer.setAspectColor(team.getColor());
			modelRenderer.render(flagModel, ModelTransformData.COMMON);
		}
		flagModel.unbind();
	}

	public void free() {
		flagModel.free();
	}
}
