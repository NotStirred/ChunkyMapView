package io.github.notstirred.chunkymapview.util.gl;

import io.github.notstirred.chunkymapview.util.Validation;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class AreaTexture extends GLObject {
    private int textureReferences = 0;

    private final int internalFormat;
    private final int format;
    private final int type;

    private final int elementWidth;
    private final int elementHeight;

    private final int xCount;
    private final int zCount;
    private final int count;

    public AreaTexture(int elementWidth, int elementHeight, int xCount, int zCount, int internalFormat, int type, int format, int minScaling, int magScaling, int edgeClampS, int edgeClampT) {
        super(glGenTextures());

        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;

        this.xCount = xCount;
        this.zCount = zCount;
        this.count = xCount*zCount;

        this.bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minScaling);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magScaling);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, edgeClampS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, edgeClampT);

        glTexImage2D(GL_TEXTURE_2D, 0, this.internalFormat, elementWidth*xCount, elementHeight*zCount, 0, this.format, this.type, (ByteBuffer) null);
    }

    public void set(int x, int z, ByteBuffer buffer) {
        this.bind();
        glTexSubImage2D(GL_TEXTURE_2D, 0,
                x, z, this.elementWidth, this.elementHeight,
                this.format, this.type,
                buffer
        );
    }

    public boolean nonePresent() {
        return textureReferences == 0;
    }

    public void ref() {
        textureReferences++;
        Validation.check(textureReferences <= count, "More than maximum textures loaded for AreaTexture!");
    }

    public void deref() {
        textureReferences--;
        Validation.check(textureReferences >= 0, "Less than zero textures loaded for AreaTexture!");
    }

    @Override
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    @Override
    protected Runnable deleter(int id) {
        return () -> glDeleteTextures(id);
    }
}
