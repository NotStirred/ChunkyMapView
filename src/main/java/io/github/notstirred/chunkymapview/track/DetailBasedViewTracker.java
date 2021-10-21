package io.github.notstirred.chunkymapview.track;

import io.github.notstirred.chunkymapview.MapView;
import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.util.Validation;
import io.github.notstirred.chunkymapview.util.bb.AABBi2d;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static io.github.notstirred.chunkymapview.util.MathUtil.manhattanDistance;

/**
 * This is used for both tracking tiles that should be generated, and also frustum culling
 *
 * {@link DetailBasedViewTracker#loadedPositions} is a set of all positions in the frustum, if generated
 */
@RequiredArgsConstructor
public class DetailBasedViewTracker implements ViewTracker<TilePos, DetailBasedView, DetailBasedTile> {
    private static final int PADDING = 1;

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final ThreadPoolExecutor trackerExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    private final Executor generationExecutor = Executors.newFixedThreadPool(THREAD_COUNT);

    private final MapView<TilePos, DetailBasedView, DetailBasedTile, ByteBuffer> mapView;

    private final Map<TilePos, Entry> entries = new ConcurrentHashMap<>();

    private final Set<TilePos> loadedPositions = new HashSet<>();

    /** sorted queue of non-loaded required positions */
    private ArrayDeque<TilePos> requiredPositions = new ArrayDeque<>();

    /** positions waiting to be processed by the executor */
    private final Set<TilePos> waitingPositions = new HashSet<>(THREAD_COUNT << 4);

    private DetailBasedView oldView = null;

    @Override
    public void viewUpdated(@NonNull DetailBasedView newView) {
        if(oldView != null && oldView.equals(newView))
            return; //view hasn't changed

        trackerExecutor.execute(() -> viewUpdated0(newView));
    }

    private void viewUpdated0(@NonNull DetailBasedView newView) {
        DetailBasedView oldView = this.oldView;
        this.oldView = newView;

        if(oldView != null && oldView.equals(newView))
            return; //view hasn't changed

        this.requiredPositions.removeIf(pos -> !newView.contains(pos));

        if (oldView != null) {
            addRemoveDiff(oldView, newView,
                tilePos -> Validation.check(this.requiredPositions.add(tilePos), "Position already queued!"),
                tilePos -> {
                    //the position will not be in these sets if it has not been generated yet
                    this.waitingPositions.remove(tilePos);
                    this.loadedPositions.remove(tilePos);
                }
            );
        } else {
            addAllPositionsWithinView(newView);
        }

        TilePos[] positions = this.requiredPositions.toArray(new TilePos[0]);
        Vec2i centrePosition = viewCentre();
        Arrays.sort(positions, (pos1, pos2) -> {
            int levelDiff = -Integer.compare(pos1.level(), pos2.level());

            if(levelDiff == 0) {
                int level = pos1.level(); //must be the level of both, as the difference is 0

                return Integer.compare(
                        manhattanDistance(centrePosition.x() >> level, centrePosition.y() >> level, pos1.x(), pos1.z()),
                        manhattanDistance(centrePosition.x() >> level, centrePosition.y() >> level, pos2.x(), pos2.z())
                );
            } else
                return levelDiff;
        });
        this.requiredPositions = new ArrayDeque<>(Arrays.asList(positions));

        fillWaiting();
    }

    private void fillWaiting() {
        int toAdd = (THREAD_COUNT << 4) - this.waitingPositions.size();
        for (int i = 0; i < toAdd; i++) {
            TilePos pos = this.requiredPositions.poll();
            if(pos == null)
                break;
            this.waitingPositions.add(pos);
            this.entries.compute(pos, (_pos, entry) -> {
                if (entry == null) {
                    entry = new Entry(_pos);
                } else {
                    if (entry.tileFuture != null)
                        entry.tileFuture.cancel(false);
                    entry.schedule();
                }
                return entry;
            });
        }
    }

    private void addRemoveDiff(DetailBasedView oldView, DetailBasedView newView, Consumer<TilePos> added, Consumer<TilePos> removed) {
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
                            removed.accept(new TilePos(x, z, level));
                        }
                    }
                }
            }

            //added positions
            if (!oldView.hasLevel(level) || newView.hasLevel(level)) {
                for (int x = newMinExtents.x(); x <= newMaxExtents.x(); x++) {
                    for (int z = newMinExtents.y(); z <= newMaxExtents.y(); z++) {
                        if (!oldView.hasLevel(level) || !oldViewExtents.intersects(x, z)) {
                            added.accept(new TilePos(x, z, level));
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
                    this.requiredPositions.add(new TilePos(x, z, level));
                }
            }
        }
    }

    private void tileCompleted(DetailBasedTile tile) {
        this.waitingPositions.remove(tile.pos());
        this.loadedPositions.add(tile.pos());
        this.entries.remove(tile.pos());
        this.fillWaiting();
    }

    @Override
    public Vec2i viewCentre() {
        return oldView.extents().minExtents().added(oldView.extents().size().scaled(0.5f));
    }

    @Override
    public DetailBasedView view() {
        return oldView;
    }

    private class Entry {
        private CompletableFuture<DetailBasedTile> tileFuture;

        private final TilePos pos;

        public Entry(TilePos pos) {
            this.pos = pos;
            this.schedule();
        }

        public void schedule() {
            this.tileFuture = DetailBasedViewTracker.this.mapView.loadingFuture(this.pos, DetailBasedViewTracker.this.generationExecutor);
            this.tileFuture.thenAcceptAsync(DetailBasedViewTracker.this::tileCompleted, DetailBasedViewTracker.this.trackerExecutor);
        }
    }
}
