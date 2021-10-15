package io.github.notstirred.chunkymapview.util.gl;

import io.github.notstirred.chunkymapview.util.Validation;

public class ReferenceCountedMetaTexture2D extends MetaTexture2D {
    private int references = 0;

    public ReferenceCountedMetaTexture2D(int elementWidth, int elementHeight, int xCount, int zCount, int internalFormat, int type, int format, int minScaling, int magScaling, int edgeClampS, int edgeClampT) {
        super(internalFormat, format, type, elementWidth, elementHeight, xCount, zCount, minScaling, magScaling, edgeClampS, edgeClampT);
    }

    public boolean anyRef() {
        return references > 0;
    }

    public void ref() {
        references++;
        Validation.check(references <= count, "More than maximum textures loaded for AreaTexture!");
    }

    public void deref() {
        references--;
        Validation.check(references >= 0, "Less than zero textures loaded for AreaTexture!");
    }
}
