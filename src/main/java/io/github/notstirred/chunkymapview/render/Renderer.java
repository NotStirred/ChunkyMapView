package io.github.notstirred.chunkymapview.render;

import io.github.notstirred.chunkymapview.collections.cache.Cache;
import io.github.notstirred.chunkymapview.collections.cache.SortedCache;
import io.github.notstirred.chunkymapview.tile.MetaTilePos;
import io.github.notstirred.chunkymapview.util.bb.MutableAABBf2d;
import io.github.notstirred.chunkymapview.util.gl.GLUtils;
import io.github.notstirred.chunkymapview.util.gl.ReferenceTrackingMetaTexture2D;
import io.github.notstirred.chunkymapview.util.vec.MutVec2f;
import io.github.notstirred.chunkymapview.util.vec.Vec2f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class Renderer {
    private long window;
    private int width = 1280, height = 720;

    private int planeProgram;

    Vector3f cameraPos = new Vector3f();
    Vector3f cameraDir = new Vector3f(1, 0, 0);

    double horizontalAngle = 0;
    double verticalAngle = 0;

    double lastTime = 0;

    boolean grabMouse = true;
    boolean grabMousePressedLastFrame = false;

    boolean viewCam = true;
    boolean viewCamPressedLastFrame = false;

    double movementSpeed = 100;
    double rotationSpeed = 0.5;
    double panSpeed = 5;

    private final int mvpLoc;
    private final int colorLoc;
    private final int texLoc;

    Vector3f[] colorsByScale = {
        new Vector3f( 1.0f, 0.0f, 0.0f),
        new Vector3f( 0.0f, 1.0f, 0.0f),
        new Vector3f( 0.0f, 0.0f, 1.0f),
        new Vector3f( 1.0f, 1.0f, 0.0f),
        new Vector3f( 1.0f, 0.0f, 1.0f),
        new Vector3f( 0.0f, 1.0f, 1.0f),
        new Vector3f( 0.5f, 0.5f, 0.5f),
        new Vector3f( 1.0f, 1.0f, 1.0f)
    };

    public Renderer() {
        init();

        mvpLoc = glGetUniformLocation(planeProgram, "MVP");
        colorLoc = glGetUniformLocation(planeProgram, "in_color");
        texLoc = glGetUniformLocation(planeProgram, "tex");
    }

    public boolean render(int highestLevel, Cache<MetaTilePos, ReferenceTrackingMetaTexture2D> textures, MutableAABBf2d viewExtents) {
        if(glfwWindowShouldClose(window))
            return false;

        glfwPollEvents();
        glViewport(0, 0, width, height);

        glClearColor(0, 0, 0, 1);
        {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(planeProgram);

            glBindVertexArray(Plane.VAO);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, Plane.EBO);

            Matrix4f vp;

            if(viewCam) {
                Vec2f size = viewExtents.size();
                Matrix4f proj = new Matrix4f().ortho(size.x(), 0, size.y(), 0, 0.1f, 100.0f);

                Vector3f eye = new Vector3f(viewExtents.minExtents().x(), 1, viewExtents.maxExtents().y());
                Matrix4f view = new Matrix4f().lookAt(
                        eye,
                        new Vector3f(eye).add(new Vector3f(0, -1, 0)),
                        new Vector3f(0, 0, -1)
                );

                vp = proj.mul(view);
            } else {
                Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(70.0f), (float) width / (float)height, 0.1f, 1000000.0f);
                Matrix4f view = new Matrix4f().lookAt(
                        cameraPos,
                        new Vector3f(cameraPos).add(cameraDir),
                        new Vector3f(0,  1, 0)
                );

                vp = proj.mul(view);
            }

            float[] mvpArray = new float[16];
            for (Map.Entry<MetaTilePos, ReferenceTrackingMetaTexture2D> entry : ((SortedCache<MetaTilePos, ReferenceTrackingMetaTexture2D>)textures).entrySet()) {
                MetaTilePos pos = entry.getKey();
                if(pos.level() < highestLevel)
                    continue;

                ReferenceTrackingMetaTexture2D texture = entry.getValue();

                //select texture unit 0
                glActiveTexture(GL_TEXTURE0);
                //bind our texture to the active 2D texture unit
                texture.bind();

                int shift = MetaTilePos.METATILE_BITS + pos.level();

                Matrix4f mvp = new Matrix4f(vp).translate(new Vector3f(pos.x() << shift, -pos.level(), pos.z() << shift)).scale((1 << shift) * 0.95f);

                mvp.get(mvpArray);

                glUniform1i(texLoc, 0); //set texture unit 0

                glUniformMatrix4fv(mvpLoc, false, mvpArray);
                Vector3f color = colorsByScale[pos.level() & 7];
                glUniform4f(colorLoc, color.x, color.y, color.z, 1);
                glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_INT, 0);
            }
            glBindVertexArray(0);
            glUseProgram(0);
        }

        glfwSwapBuffers(window);
        return true;
    }

    private void init() {
        glfwInit();
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        window = glfwCreateWindow(width, height, "Simple textured quad", NULL, NULL);

        glfwMakeContextCurrent(window);

        glfwShowWindow(window);
        try (MemoryStack frame = MemoryStack.stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }
        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback(System.err);

        glfwSwapInterval(0);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glfwSetCursorPos(window, width/2.0, height/2.0);

        createPlaneBufferObjects();
        try {
            planeProgram = GLUtils.createQuadProgram("tile.vs", "tile.fs");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void createPlaneBufferObjects() {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Plane.vertexBuffer.array(), GL_STATIC_DRAW);

        Plane.EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, Plane.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Plane.indexBuffer.array(), GL_STATIC_DRAW);

        Plane.VAO = glGenVertexArrays();
        glBindVertexArray(Plane.VAO);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
    }

    public void update(MutVec2f viewPos, MutVec2f viewSize) {
        double currentTime = glfwGetTime();
        double deltaTime = Math.min(currentTime - lastTime, 0.1);
        lastTime = currentTime;

        if(grabMouse) {
            double[] xpos = new double[1];
            double[] ypos = new double[1];
            glfwGetCursorPos(window, xpos, ypos);

            glfwSetCursorPos(window, width/2.0, height/2.0);
            horizontalAngle += rotationSpeed * deltaTime * (width / 2.0 - xpos[0]);
            verticalAngle += rotationSpeed * deltaTime * (height / 2.0 - ypos[0]);
            if(horizontalAngle > 2*Math.PI) {
                horizontalAngle -= 2*Math.PI;
            } else if(horizontalAngle < 0) {
                horizontalAngle += 2*Math.PI;
            }
            if(verticalAngle > Math.PI/2-0.0001) {
                verticalAngle = Math.PI/2-0.0001;
            } else if(verticalAngle < -Math.PI/2+0.0001) {
                verticalAngle = -Math.PI/2+0.0001;
            }
        }

        Vector3d direction = new Vector3d(
                cos(verticalAngle) * sin(horizontalAngle),
                sin(verticalAngle),
                cos(verticalAngle) * cos(horizontalAngle)
        );
        Vector3d right = new Vector3d(
                sin(horizontalAngle - 3.14f/2.0f),
                0,
                cos(horizontalAngle - 3.14f/2.0f)
        );
        Vector3d up = new Vector3d(right).cross(direction);

        cameraDir = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        //update
        {
            if(grabMouse) {
                double speedDelta = movementSpeed * deltaTime;
                if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    System.exit(0);
                }
                if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                    cameraPos.add((float) (direction.x*speedDelta), (float) (direction.y*speedDelta), (float) (direction.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                    cameraPos.add((float) (-right.x*speedDelta), (float) (-right.y*speedDelta), (float) (-right.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                    cameraPos.add((float) (-direction.x*speedDelta), (float) (-direction.y*speedDelta), (float) (-direction.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                    cameraPos.add((float) (right.x*speedDelta), (float) (right.y*speedDelta), (float) (right.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                    cameraPos.add((float) (up.x*speedDelta), (float) (up.y*speedDelta), (float) (up.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
                    cameraPos.add((float) (-up.x*speedDelta), (float) (-up.y*speedDelta), (float) (-up.z*speedDelta));
                }
                if(glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
                    viewPos.addY((float) (deltaTime * panSpeed) * Math.max(viewSize.x(), viewSize.y()));
                }
                if(glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
                    viewPos.addX((float) (deltaTime * panSpeed) * Math.max(viewSize.x(), viewSize.y()));
                }
                if(glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
                    viewPos.subY((float) (deltaTime * panSpeed) * Math.max(viewSize.x(), viewSize.y()));
                }
                if(glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
                    viewPos.subX((float) (deltaTime * panSpeed) * Math.max(viewSize.x(), viewSize.y()));
                }
                if(glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
                    viewSize.mulX((float) (1 - deltaTime));
                    viewSize.mulY((float) (1 - deltaTime));
                }
                if(glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) {
                    viewSize.mulX((float) (1 + deltaTime));
                    viewSize.mulY((float) (1 + deltaTime));
                }
                if(glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
                    if(!viewCamPressedLastFrame) {
                        viewCamPressedLastFrame = true;
                        viewCam = !viewCam;
                    }
                } else {
                    viewCamPressedLastFrame = false;
                }
            }
            if(glfwGetKey(window, GLFW_KEY_U) == GLFW_PRESS) {
                if(!grabMousePressedLastFrame) {
                    grabMousePressedLastFrame = true;
                    if(grabMouse)
                        grabMouse = !grabMouse;
                    else {
                        glfwSetCursorPos(window, width/2.0, height/2.0);
                        grabMouse = !grabMouse;
                    }
                }
            } else {
                grabMousePressedLastFrame = false;
            }
        }
    }
}
