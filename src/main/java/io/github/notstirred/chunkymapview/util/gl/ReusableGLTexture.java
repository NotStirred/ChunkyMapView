package io.github.notstirred.chunkymapview.util.gl;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class ReusableGLTexture extends GLObject {

    private final int width;
    private final int height;
    private final int colorFormat;
    private final int type;
    private final int target;
    private boolean allocated = false;

    public ReusableGLTexture(int width, int height, int colorFormat, int type, int target, int minScaling, int magScaling, int edgeClamp) {
        super(glGenTextures());
        glBindTexture(target, this.id);

        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minScaling);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magScaling);

        glTexParameteri(target, GL_TEXTURE_WRAP_S, edgeClamp);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, edgeClamp);

        this.width = width;
        this.height = height;
        this.colorFormat = colorFormat;
        this.type = type;
        this.target = target;
    }

    public void bind() {
        glBindTexture(this.target, this.id);
    }

    public void setTexture(ByteBuffer buffer, boolean mipmaps) {
        this.bind();
        if(!allocated) {
            allocated = true;
            glTexImage2D(this.target, 0, this.colorFormat, this.width, this.height, 0, this.colorFormat, this.type, buffer);
            if (mipmaps) {
                glGenerateMipmap(this.target);
            }
        } else {
            glTexSubImage2D(this.target, 0, 0, 0, this.width, this.height, this.colorFormat, this.type, buffer);
        }
    }

    @Override
    protected Runnable deleter(int id) {
        return () -> glDeleteTextures(id);
    }
}
