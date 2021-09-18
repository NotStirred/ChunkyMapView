package io.github.notstirred.track;

import lombok.NonNull;

public interface ViewTracker<POS, VIEW> {

    void viewUpdated(@NonNull VIEW newView);

}
