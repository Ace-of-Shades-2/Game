package nl.andrewl.aos2_client.render.chunk;

import nl.andrewl.aos_core.model.world.Chunk;
import nl.andrewl.aos_core.model.world.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Highly-optimized class for generating chunk meshes, without any heap
 * allocations at runtime. Not thread safe.
 */
public final class ChunkMeshGenerator {
	private final FloatBuffer vertexBuffer;
	private final IntBuffer indexBuffer;

	private final Vector3i pos = new Vector3i();// Pre-allocated vector to hold current local chunk block position.
	private final Vector3f color = new Vector3f();// Pre-allocated vector to hold current block color.
	private final Vector3f norm = new Vector3f();// Pre-allocated vector to hold current face normal.

	private final Vector3f checkPos = new Vector3f();// Pre-allocated vector to hold world block position.
	private final Vector3i checkUtil = new Vector3i();// Pre-allocated utility vector to give to World for position stuff.

	public ChunkMeshGenerator() {
		vertexBuffer = BufferUtils.createFloatBuffer(300_000);
		indexBuffer = BufferUtils.createIntBuffer(100_000);
	}

	public ChunkMeshData generateMesh(Chunk chunk, World world) {
		vertexBuffer.clear();
		indexBuffer.clear();
		int idx = 0;
		for (int i = 0; i < Chunk.TOTAL_SIZE; i++) {
			Chunk.idxToXyz(i, pos);
			int x = pos.x;
			int y = pos.y;
			int z = pos.z;
			int worldX = Chunk.SIZE * chunk.getPosition().x + x;
			int worldY = Chunk.SIZE * chunk.getPosition().y + y;
			int worldZ = Chunk.SIZE * chunk.getPosition().z + z;
			byte block = chunk.getBlocks()[i];
			if (block <= 0) {
				continue; // Don't render empty blocks.
			}

			color.set(world.getPalette().getColor(block));

			// See /design/block_rendering.svg for a diagram of how these vertices are defined.
//			var a = new Vector3f(x, y + 1, z + 1);
//			var b = new Vector3f(x, y + 1, z);
//			var c = new Vector3f(x, y, z);
//			var d = new Vector3f(x, y, z + 1);
//			var e = new Vector3f(x + 1, y + 1, z);
//			var f = new Vector3f(x + 1, y + 1, z + 1);
//			var g = new Vector3f(x + 1, y, z + 1);
//			var h = new Vector3f(x + 1, y, z);

			// Top
			checkPos.set(worldX, worldY + 1, worldZ);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(0, 1, 0);
				genFace2(idx,
						x,		y+1,	z+1,	// a
						x+1,	y+1,	z+1,	// f
						x+1,	y+1,	z,		// e
						x,		y+1,	z		// b
				);
				idx += 4;
			}
			// Bottom
			checkPos.set(worldX, worldY - 1, worldZ);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(0, -1, 0);// c h g d
				genFace2(idx,
						x,		y,		z,		// c
						x+1,	y,		z,		// h
						x+1,	y,		z+1,	// g
						x,		y,		z+1		// d
				);
				idx += 4;
			}
			// Positive z
			checkPos.set(worldX, worldY, worldZ + 1);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(0, 0, 1);
				genFace2(idx,
						x+1,	y+1,	z+1,	// f
						x,		y+1,	z+1,	// a
						x,		y,		z+1,	// d
						x+1,	y,		z+1		// g
				);
				idx += 4;
			}
			// Negative z
			checkPos.set(worldX, worldY, worldZ - 1);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(0, 0, -1);
				genFace2(idx,
						x,		y+1,	z,		// b
						x+1,	y+1,	z,		// e
						x+1,	y,		z,		// h
						x,		y,		z		// c
				);
				idx += 4;
			}
			// Positive x
			checkPos.set(worldX + 1, worldY, worldZ);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(1, 0, 0);
				genFace2(idx,
						x+1,	y+1,	z,		// e
						x+1,	y+1,	z+1,	// f
						x+1,	y,		z+1,	// g
						x+1,	y,		z		// h
				);
				idx += 4;
			}
			// Negative x
			checkPos.set(worldX - 1, worldY, worldZ);
			if (world.getBlockAt(checkPos, checkUtil) == 0) {
				norm.set(-1, 0, 0);
				genFace2(idx,
						x,		y+1,	z+1,	// a
						x,		y+1,	z,		// b
						x,		y,		z,		// c
						x,		y,		z+1		// d
				);
				idx += 4;
			}
		}

		return new ChunkMeshData(vertexBuffer.flip(), indexBuffer.flip());
	}

	private void genFace(int currentIndex, float... vertices) {
		for (int i = 0; i < 12; i += 3) {
			vertexBuffer.put(vertices[i]);
			vertexBuffer.put(vertices[i+1]);
			vertexBuffer.put(vertices[i+2]);
			vertexBuffer.put(color.x);
			vertexBuffer.put(color.y);
			vertexBuffer.put(color.z);
			vertexBuffer.put(norm.x);
			vertexBuffer.put(norm.y);
			vertexBuffer.put(norm.z);
		}
		// Top-left triangle.
		indexBuffer.put(currentIndex);
		indexBuffer.put(currentIndex + 1);
		indexBuffer.put(currentIndex + 2);
		// Bottom-right triangle.
		indexBuffer.put(currentIndex + 2);
		indexBuffer.put(currentIndex + 3);
		indexBuffer.put(currentIndex);
	}

	private void genFace2(int currentIndex, float a1, float a2, float a3, float b1, float b2, float b3, float c1, float c2, float c3, float d1, float d2, float d3) {
		// A
		vertexBuffer.put(a1);
		vertexBuffer.put(a2);
		vertexBuffer.put(a3);
		vertexBuffer.put(color.x);
		vertexBuffer.put(color.y);
		vertexBuffer.put(color.z);
		vertexBuffer.put(norm.x);
		vertexBuffer.put(norm.y);
		vertexBuffer.put(norm.z);
		// B
		vertexBuffer.put(b1);
		vertexBuffer.put(b2);
		vertexBuffer.put(b3);
		vertexBuffer.put(color.x);
		vertexBuffer.put(color.y);
		vertexBuffer.put(color.z);
		vertexBuffer.put(norm.x);
		vertexBuffer.put(norm.y);
		vertexBuffer.put(norm.z);
		// C
		vertexBuffer.put(c1);
		vertexBuffer.put(c2);
		vertexBuffer.put(c3);
		vertexBuffer.put(color.x);
		vertexBuffer.put(color.y);
		vertexBuffer.put(color.z);
		vertexBuffer.put(norm.x);
		vertexBuffer.put(norm.y);
		vertexBuffer.put(norm.z);
		// D
		vertexBuffer.put(d1);
		vertexBuffer.put(d2);
		vertexBuffer.put(d3);
		vertexBuffer.put(color.x);
		vertexBuffer.put(color.y);
		vertexBuffer.put(color.z);
		vertexBuffer.put(norm.x);
		vertexBuffer.put(norm.y);
		vertexBuffer.put(norm.z);

		// Top-left triangle.
		indexBuffer.put(currentIndex);
		indexBuffer.put(currentIndex + 1);
		indexBuffer.put(currentIndex + 2);
		// Bottom-right triangle.
		indexBuffer.put(currentIndex + 2);
		indexBuffer.put(currentIndex + 3);
		indexBuffer.put(currentIndex);
	}
}
