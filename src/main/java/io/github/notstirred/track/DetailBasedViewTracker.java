package io.github.notstirred.track;

import com.sun.istack.internal.NotNull;
import io.github.notstirred.tile.TilePos;
import io.github.notstirred.util.bb.AABBi2d;
import io.github.notstirred.util.vec.Vec2i;

import java.util.HashSet;
import java.util.Set;

public class DetailBasedViewTracker implements ViewTracker<TilePos, DetailBasedView> {
    private static final int PADDING = 2;

    Set<TilePos> positions = new HashSet<>();

    private DetailBasedView oldView = null;

    public Set<TilePos> getPositions() {
        return positions;
    }

    @Override
    public void viewUpdated(@NotNull DetailBasedView newView) {
        DetailBasedView oldView = this.oldView;

        if(oldView != null && oldView.equals(newView))
            return;

        if (oldView != null) {
            addRemoveDiff(oldView, newView);
        } else {
            positions.clear();
            addAllPositionsWithinView(newView);
        }

        this.oldView = newView;
    }

    private void addRemoveDiff(DetailBasedView oldView, DetailBasedView newView) {
        for (int level = Math.min(oldView.lowestLevel(), newView.lowestLevel()); level < Math.max(oldView.highestLevel(), newView.highestLevel()); level++) {

            if (oldView.hasLevel(level) && newView.hasLevel(level) && oldView.resolution().equals(newView.resolution())
                    && oldView.extents().equals(newView.extents())) { //nothing changed, skip this level
                continue;
            }

            AABBi2d oldViewExtents = getViewExtentsForLevel(level, oldView.extents().minExtents(), oldView.extents().maxExtents());
            Vec2i oldMinExtents = oldViewExtents.minExtents();
            Vec2i oldMaxExtents = oldViewExtents.maxExtents();

            AABBi2d newViewExtents = getViewExtentsForLevel(level, newView.extents().minExtents(), newView.extents().maxExtents());
            Vec2i newMinExtents = newViewExtents.minExtents();
            Vec2i newMaxExtents = newViewExtents.maxExtents();

            //removed positions
            if (!newView.hasLevel(level) || oldView.hasLevel(level)) {
                for (int x = oldMinExtents.x(); x <= oldMaxExtents.x(); x++) {
                    for (int z = oldMinExtents.y(); z <= oldMaxExtents.y(); z++) {
                        if (!newView.hasLevel(level) || !newViewExtents.intersects(x, z)) {
                            positions.remove(new TilePos(x, z, level));
                        }
                    }
                }
            }

            //added positions
            if (!oldView.hasLevel(level) || newView.hasLevel(level)) {
                for (int x = newMinExtents.x(); x <= newMaxExtents.x(); x++) {
                    for (int z = newMinExtents.y(); z <= newMaxExtents.y(); z++) {
                        if (!oldView.hasLevel(level) || !oldViewExtents.intersects(x, z)) {
                            positions.add(new TilePos(x, z, level));
                        }
                    }
                }
            }
        }
    }

    private AABBi2d getViewExtentsForLevel(int level, Vec2i minExtents, Vec2i maxExtents) {
        return new AABBi2d(
                (minExtents.x() >> level) - PADDING,
                (minExtents.y() >> level) - PADDING,
                (maxExtents.x() >> level) + PADDING,
                (maxExtents.y() >> level) + PADDING
        );
    }

    private void addAllPositionsWithinView(DetailBasedView newView) {
        for (int level = newView.lowestLevel(); level < newView.highestLevel(); level++) {
            Vec2i minExtents = newView.extents().minExtents();
            Vec2i maxExtents = newView.extents().maxExtents();

            AABBi2d viewExtentsForLevel = getViewExtentsForLevel(level, minExtents, maxExtents);

            for (int x = viewExtentsForLevel.minExtents().x(); x <= viewExtentsForLevel.maxExtents().x(); x++) {
                for (int z = viewExtentsForLevel.minExtents().y(); z <= viewExtentsForLevel.maxExtents().y(); z++) {
                    positions.add(new TilePos(x, z, level));
                }
            }
        }
    }
}
