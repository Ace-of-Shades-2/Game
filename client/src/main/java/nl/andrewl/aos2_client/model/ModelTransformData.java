package nl.andrewl.aos2_client.model;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record ModelTransformData(
		Matrix4f tx,
		float[] txData,
		Matrix3f norm,
		float[] normData
) {
	public static final ModelTransformData COMMON = new ModelTransformData();

	public ModelTransformData() {
		this(new Matrix4f(), new float[16], new Matrix3f(), new float[9]);
	}

	public void updateData() {
		tx.normal(norm);
		tx.get(txData);
		norm.get(normData);
	}
}
