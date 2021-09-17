package io.github.notstirred.track;

import com.sun.istack.internal.NotNull;

public interface ViewTracker<POS, VIEW> {

    void viewUpdated(@NotNull VIEW newView);

}
