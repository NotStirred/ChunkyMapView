package io.github.notstirred.chunkymapview.track;

import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.NonNull;

import java.util.Collection;

public interface ViewTracker<POS extends TilePos, VIEW extends View<POS>, TILE extends Tile> {

    void viewUpdated(@NonNull VIEW newView);

    Vec2i viewCentre();
}
