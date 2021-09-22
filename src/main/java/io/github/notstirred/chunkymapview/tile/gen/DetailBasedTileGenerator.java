package io.github.notstirred.chunkymapview.tile.gen;

import io.github.notstirred.chunkymapview.tile.TilePos;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;

import java.nio.ByteBuffer;

public class DetailBasedTileGenerator implements TileGenerator<TilePos, ByteBuffer> {
    @Override
    public ByteBuffer generate(TilePos tilePos) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {

        }

        int size = 16 * 16 * 3;
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(size);

        int level = tilePos.level();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int x1 = ((tilePos.x() << level) * 16 + (x << level));
                int z1 = ((tilePos.z() << level) * 16 + (z << level));

                double noise = Noise.gradientCoherentNoise3D(x1/128.0, 0, z1/128.0, 0, NoiseQuality.BEST);
                byte val = (byte)(noise * 256);
                imageBuffer.put(val);
                imageBuffer.put(val);
                imageBuffer.put(val);
            }
        }

        imageBuffer.flip();
        return imageBuffer;
    }
}
