package nl.andrewl.aos2_client.render;

import nl.andrewl.aos2_client.Camera;
import nl.andrewl.aos2_client.Client;
import nl.andrewl.aos2_client.config.ClientConfig;
import nl.andrewl.aos2_client.control.*;
import nl.andrewl.aos2_client.model.ClientPlayer;
import nl.andrewl.aos2_client.render.chunk.ChunkRenderer;
import nl.andrewl.aos2_client.render.gui.GuiRenderer;
import org.joml.Matrix4f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

/**
 * This component manages all the view-related aspects of the client, such as
 * chunk rendering, window setup and removal, and other OpenGL functions. It
 * should generally only be invoked on the main thread, since this is where the
 * OpenGL context exists.
 */
public class GameRenderer {
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 500f;

	private final ClientConfig.DisplayConfig config;
	private final ChunkRenderer chunkRenderer;
	private final GuiRenderer guiRenderer;
	private final ModelRenderer modelRenderer;
	private final Camera camera;
	private final Client client;
	private final InputHandler inputHandler;

	private final PlayerRenderer playerRenderer;
	private final ProjectileRenderer projectileRenderer;
	private final TeamRenderer teamRenderer;

	private final long windowHandle;
	private final int screenWidth;
	private final int screenHeight;

	private final Matrix4f perspectiveTransform;
	private final float[] perspectiveTransformData = new float[16];

	public GameRenderer(Client client, InputHandler inputHandler, Camera camera) {
		this.config = client.getConfig().display;
		this.client = client;
		this.inputHandler = inputHandler;
		this.camera = camera;
		camera.setToPlayer(client.getMyPlayer());
		this.perspectiveTransform = new Matrix4f();

		// Initialize window!

		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit()) throw new IllegalStateException("Could not initialize GLFW.");
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		long monitorId = glfwGetPrimaryMonitor();
		GLFWVidMode primaryMonitorSettings = glfwGetVideoMode(monitorId);
		if (primaryMonitorSettings == null) throw new IllegalStateException("Could not get information about the primary monitory.");
		if (config.fullscreen) {
			screenWidth = primaryMonitorSettings.width();
			screenHeight = primaryMonitorSettings.height();
			windowHandle = glfwCreateWindow(screenWidth, screenHeight, "Ace of Shades 2", monitorId, 0);
		} else {
			screenWidth = 1000;
			screenHeight = 800;
			windowHandle = glfwCreateWindow(screenWidth, screenHeight, "Ace of Shades 2", 0, 0);
		}
		if (windowHandle == 0) throw new RuntimeException("Failed to create GLFW window.");
		inputHandler.setWindowId(windowHandle);

		// Setup callbacks.
		glfwSetKeyCallback(windowHandle, new PlayerInputKeyCallback(inputHandler));
		glfwSetCursorPosCallback(windowHandle, new PlayerViewCursorCallback(inputHandler));
		glfwSetMouseButtonCallback(windowHandle, new PlayerInputMouseClickCallback(inputHandler));
		glfwSetScrollCallback(windowHandle, new PlayerInputMouseScrollCallback(inputHandler));
		glfwSetCharCallback(windowHandle, new PlayerCharacterInputCallback(inputHandler));
		glfwSetWindowFocusCallback(windowHandle, (window, focused) -> {
			if (!focused) inputHandler.switchToExitMenuContext();
		});
		if (config.captureCursor) {
			glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		}
		glfwSetInputMode(windowHandle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
		glfwSetCursorPos(windowHandle, 0, 0);

		glfwMakeContextCurrent(windowHandle);
		glfwSwapInterval(1);
		glfwShowWindow(windowHandle);

		GL.createCapabilities();
//		GLUtil.setupDebugMessageCallback(System.out);
		glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glCullFace(GL_BACK);

		this.chunkRenderer = new ChunkRenderer();

		try {
			this.guiRenderer = new GuiRenderer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.modelRenderer = new ModelRenderer();
		try {
			this.projectileRenderer = new ProjectileRenderer(modelRenderer);
			this.teamRenderer = new TeamRenderer(modelRenderer);
			this.playerRenderer = new PlayerRenderer(modelRenderer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		updatePerspective(config.fov);
	}

	public float getAspectRatio() {
		return (float) screenWidth / (float) screenHeight;
	}

	/**
	 * Updates the rendering perspective used to render the game.
	 */
	public void updatePerspective(float fov) {
		float fovRad = (float) Math.toRadians(fov);
		if (fovRad >= Math.PI) {
			fovRad = (float) (Math.PI - 0.01f);
		} else if (fovRad <= 0) {
			fovRad = 0.01f;
		}
		perspectiveTransform.setPerspective(fovRad, getAspectRatio(), Z_NEAR, Z_FAR);
		perspectiveTransform.get(perspectiveTransformData);
		chunkRenderer.setPerspective(perspectiveTransformData);
		modelRenderer.setPerspective(perspectiveTransformData);
	}

	public boolean windowShouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}

	public Camera getCamera() {
		return camera;
	}

	public GuiRenderer getGuiRenderer() {
		return guiRenderer;
	}

	public void draw() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		ClientPlayer myPlayer = client.getMyPlayer();
		if (inputHandler.isNormalContextActive() && inputHandler.getNormalContext().isScopeEnabled()) {
			updatePerspective(15);
		} else {
			updatePerspective(config.fov);
		}
		myPlayer.updateHeldItemTransform(camera, client.getInputHandler());

		chunkRenderer.draw(camera, client.getWorld().getChunkMeshesToDraw());

		// Model rendering
		modelRenderer.start(camera.getViewTransformData());
		playerRenderer.render(client.getMyPlayer(), client.getPlayers().values(), client.getWorld());
		projectileRenderer.render(client.getProjectiles().values());
		teamRenderer.render(client.getTeams().values());
		modelRenderer.end();

		// GUI rendering
		guiRenderer.start();
		guiRenderer.drawNameplates(myPlayer, camera.getViewTransformData(), perspectiveTransformData);
		guiRenderer.drawNvg(screenWidth, screenHeight, client);
		guiRenderer.end();

		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
	}

	public void freeWindow() {
		playerRenderer.free();
		projectileRenderer.free();
		teamRenderer.free();
		modelRenderer.free();
		guiRenderer.free();
		chunkRenderer.free();
		GL.destroy();
		Callbacks.glfwFreeCallbacks(windowHandle);
		glfwSetErrorCallback(null);
		glfwDestroyWindow(windowHandle);
		glfwTerminate();
	}
}
