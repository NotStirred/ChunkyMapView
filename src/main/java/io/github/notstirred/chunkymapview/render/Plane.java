package io.github.notstirred.chunkymapview.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Plane {
    public static final FloatBuffer vertexBuffer = FloatBuffer.wrap(new float[] {
          //positions //texture coords
            0, 0, 0,    0.0f, 0.0f,
            1, 0, 0,    1.0f, 0.0f,
            0, 0, 1,    0.0f, 1.0f,
            1, 0, 1,    1.0f, 1.0f,
    });

    public static final IntBuffer indexBuffer = IntBuffer.wrap(new int[] {
            0, 2, 1, 3
    });

    public static int VAO;
    public static int EBO;
}
