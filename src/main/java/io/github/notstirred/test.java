package io.github.notstirred;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.*;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Renders a simple textured quad using OpenGL 3.0.
 *
 * @author Kai Burjack
 */
class SimpleTexturedQuad {
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null)
            throw new IOException("Classpath resource not found: " + resource);
        File file = new File(url.getFile());
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();
        } else {
            buffer = BufferUtils.createByteBuffer(bufferSize);
            InputStream source = url.openStream();
            if (source == null)
                throw new FileNotFoundException(resource);
            try {
                byte[] buf = new byte[8192];
                while (true) {
                    int bytes = source.read(buf, 0, buf.length);
                    if (bytes == -1)
                        break;
                    if (buffer.remaining() < bytes)
                        buffer = resizeBuffer(buffer, Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes));
                    buffer.put(buf, 0, bytes);
                }
                buffer.flip();
            } finally {
                source.close();
            }
        }
        return buffer;
    }

    public static int createShader(String resource, int type, String version) throws IOException {
        int shader = glCreateShader(type);

        ByteBuffer source = ioResourceToByteBuffer(resource, 8192);

        if ( version == null ) {
            PointerBuffer strings = BufferUtils.createPointerBuffer(1);
            IntBuffer lengths = BufferUtils.createIntBuffer(1);

            strings.put(0, source);
            lengths.put(0, source.remaining());

            glShaderSource(shader, strings, lengths);
        } else {
            PointerBuffer strings = BufferUtils.createPointerBuffer(2);
            IntBuffer lengths = BufferUtils.createIntBuffer(2);

            ByteBuffer preamble = memUTF8("#version " + version + "\n", false);

            strings.put(0, preamble);
            lengths.put(0, preamble.remaining());

            strings.put(1, source);
            lengths.put(1, source.remaining());

            glShaderSource(shader, strings, lengths);
        }

        glCompileShader(shader);
        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        String shaderLog = glGetShaderInfoLog(shader);
        if (shaderLog.trim().length() > 0) {
            System.err.println(shaderLog);
        }
        if (compiled == 0) {
            throw new AssertionError("Could not compile shader");
        }
        return shader;
    }

    long window;
    int width = 1024;
    int height = 768;

    int vao;
    int quadProgram;
    int quadProgram_inputPosition;
    int quadProgram_inputTextureCoords;

    GLCapabilities caps;
    GLFWKeyCallback keyCallback;
    Callback debugProc;

    void init() throws IOException {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        window = glfwCreateWindow(width, height, "Simple textured quad", NULL, NULL);
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        try (MemoryStack frame = MemoryStack.stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }
        caps = GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();
        createTexture();
        createQuadProgram();
        createFullScreenQuad();
    }

    void createQuadProgram() throws IOException {
        int program = glCreateProgram();
        int vshader = createShader("tile.vs", GL_VERTEX_SHADER, null);
        int fshader = createShader("tile.fs", GL_FRAGMENT_SHADER, null);
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        int linked = glGetProgrami(program, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(program);
        if (programLog.trim().length() > 0)
            System.err.println(programLog);
        if (linked == 0)
            throw new AssertionError("Could not link program");
        glUseProgram(program);
        int texLocation = glGetUniformLocation(program, "tex");
        glUniform1i(texLocation, 0);
        quadProgram_inputPosition = glGetAttribLocation(program, "position");
        quadProgram_inputTextureCoords = glGetAttribLocation(program, "texCoords");
        glUseProgram(0);
        this.quadProgram = program;
    }

    void createFullScreenQuad() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int positionVbo = glGenBuffers();
        FloatBuffer fb = BufferUtils.createFloatBuffer(2 * 6);
        fb.put(-1.0f).put(-1.0f);
        fb.put(1.0f).put(-1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(-1.0f).put(1.0f);
        fb.put(-1.0f).put(-1.0f);
        fb.flip();
        glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glVertexAttribPointer(quadProgram_inputPosition, 2, GL_FLOAT, false, 0, 0L);
        glEnableVertexAttribArray(quadProgram_inputPosition);
        int texCoordsVbo = glGenBuffers();
        fb = BufferUtils.createFloatBuffer(2 * 6);
        fb.put(0.0f).put(1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(1.0f).put(0.0f);
        fb.put(1.0f).put(0.0f);
        fb.put(0.0f).put(0.0f);
        fb.put(0.0f).put(1.0f);
        fb.flip();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordsVbo);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glVertexAttribPointer(quadProgram_inputTextureCoords, 2, GL_FLOAT, true, 0, 0L);
        glEnableVertexAttribArray(quadProgram_inputTextureCoords);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    static ByteBuffer texture() {
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(512*512*3);

        byte[] bytes = new byte[512*512*3];
        new Random().nextBytes(bytes);
        imageBuffer.put(bytes);

        imageBuffer.flip();
        return imageBuffer;
    }

    static void createTexture() throws IOException {
        ByteBuffer data = texture();
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 512, 512, 0, GL_RGB, GL_UNSIGNED_BYTE, data);
    }

    void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(quadProgram);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glUseProgram(0);
    }

    void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glViewport(0, 0, width, height);
            render();
            glfwSwapBuffers(window);
        }
    }

    void run() throws IOException {
        init();
        loop();
        if (debugProc != null)
            debugProc.free();
        keyCallback.free();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) throws IOException {
        new SimpleTexturedQuad().run();
    }
}