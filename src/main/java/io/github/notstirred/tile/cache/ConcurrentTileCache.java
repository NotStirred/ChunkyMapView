package io.github.notstirred.tile.cache;

import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentTileCache<POS, DATA> implements TileCache<POS, DATA> {
    private final Map<POS, DATA> map = new ConcurrentHashMap<>();


    @Override
    public DATA get(@NonNull POS pos) {
        return map.get(pos);
    }

    @Override
    public DATA put(@NonNull POS pos, DATA data) {
        if (data != null) {
            return map.put(pos, data);
        } else {
            return map.remove(pos);
        }
    }
}
