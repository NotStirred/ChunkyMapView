package io.github.notstirred.chunkymapview.track;

import lombok.NonNull;

import java.util.Collection;

public interface ViewTracker<POS, VIEW, TILE> {

    void viewUpdated(@NonNull VIEW newView);

    Collection<TILE> tiles();

}
