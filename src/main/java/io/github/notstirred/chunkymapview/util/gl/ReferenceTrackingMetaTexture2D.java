package io.github.notstirred.chunkymapview.util.gl;

import io.github.notstirred.chunkymapview.util.ResettingRecyclingSupplier;
import io.github.notstirred.chunkymapview.util.Validation;

import java.util.BitSet;

public class ReferenceTrackingMetaTexture2D extends MetaTexture2D implements ResettingRecyclingSupplier.Recyclable {
    private final BitSet present;

    public ReferenceTrackingMetaTexture2D(int elementWidth, int elementHeight, int xCount, int zCount, int internalFormat, int type, int format, int minScaling, int magScaling, int edgeClampS, int edgeClampT) {
        super(internalFormat, format, type, elementWidth, elementHeight, xCount, zCount, minScaling, magScaling, edgeClampS, edgeClampT);
        this.present = new BitSet(this.count);
    }

    public void ref(int x, int z) {
        boolean existing = this.present.get(x + z * this.xCount);
        Validation.check(!existing, "Already referenced tile referenced again!");
        this.present.set(x + z*this.xCount, true);
    }

    public void deref(int x, int z) {
        boolean existing = this.present.get(x + z * this.xCount);
        Validation.check(existing, "Non referenced tile dereferenced again!");
        this.present.set(x + z*this.xCount, false);
    }

    public boolean anyRef() {
        return !this.present.isEmpty();
    }

    public boolean contains(int x, int z) {
        return this.present.get(x + z*this.xCount);
    }

    @Override
    public void reset() {
        this.present.clear();
        this.clear();
    }
}
