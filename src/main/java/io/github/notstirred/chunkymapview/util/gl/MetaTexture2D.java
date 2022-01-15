package io.github.notstirred.chunkymapview.util.gl;

import io.github.notstirred.chunkymapview.util.MathUtil;
import io.github.notstirred.chunkymapview.util.Validation;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL44C.glClearTexImage;

public class MetaTexture2D extends GLObject {
    protected final int internalFormat;
    protected final int format;
    protected final int type;

    protected final int elementWidth;
    protected final int elementHeight;

    protected final int xCount;
    protected final int zCount;
    protected final int count;

    public MetaTexture2D(int internalFormat, int format, int type, int elementWidth, int elementHeight, int xCount, int zCount, int minScaling, int magScaling, int edgeClampS, int edgeClampT) {
        super(glGenTextures());

        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;
        Validation.check(MathUtil.isPow2(xCount), "xCount must be a power of two");
        this.xCount = xCount;
        Validation.check(MathUtil.isPow2(zCount), "zCount must be a power of two");
        this.zCount = zCount;
        this.count = xCount*zCount;

        this.bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minScaling);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magScaling);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, edgeClampS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, edgeClampT);

        glTexImage2D(GL_TEXTURE_2D, 0, this.internalFormat, elementWidth*xCount, elementHeight*zCount, 0, this.format, this.type, ByteBuffer.allocateDirect(4 * elementWidth*elementHeight * count));
    }

    public void set(int xIdx, int zIdx, ByteBuffer buffer) {
        this.bind();
        glTexSubImage2D(GL_TEXTURE_2D, 0,
                xIdx * elementWidth, zIdx * elementHeight, this.elementWidth, this.elementHeight,
                this.format, this.type,
                buffer
        );
    }

    public void clear() {
        this.bind();
        if(GLUtils.ARB_clear_texture) {
            glClearTexImage(this.id, 0, GL_RGBA, GL_UNSIGNED_BYTE, (int[]) null);
        } else {
            //TODO: share this buffer between calls
            ByteBuffer buf = ByteBuffer.allocateDirect(this.elementWidth * this.xCount * this.elementHeight * this.zCount * 4);
            glTexSubImage2D(GL_TEXTURE_2D, 0,
                    0, 0, this.elementWidth*this.xCount, this.elementHeight*this.zCount,
                    this.format, this.type,
                    buf
            );
        }
    }

    @Override
    protected Runnable deleter(int id) {
        return () -> glDeleteTextures(id);
    }

    @Override
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }
}
