package io.github.notstirred.tile.cache;

import lombok.NonNull;

public interface TileCache<POS, DATA> {

    /**
     * @return the existing value (if any)
     */
    DATA get(@NonNull POS pos);

    /**
     * put value into the cache. if data is null remove it
     * @return the existing value (if any)
     */
    DATA put(@NonNull POS pos, DATA data);

    default DATA remove(@NonNull POS pos) {
        return put(pos, null);
    }
}
