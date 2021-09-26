package io.github.notstirred.chunkymapview.track;

import io.github.notstirred.chunkymapview.MapView;
import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.util.Validation;
import io.github.notstirred.chunkymapview.util.bb.AABBi2d;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private final MapView<TilePos, DetailBasedTile, ByteBuffer> mapView;

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
                    this.entries.computeIfPresent(tilePos, (pos, entry) -> {
                        entry.unload();
                        return null;
                    });
                    //the position will not be in these sets if it has not been generated yet
                    this.loadedPositions.remove(tilePos);
                    this.waitingPositions.remove(tilePos);
                }
            );
        } else {
            addAllPositionsWithinView(newView);
        }

        TilePos[] positions = this.requiredPositions.toArray(new TilePos[0]);
        Vec2i centrePosition = newView.extents().minExtents().added(newView.extents().size().scaled(0.5f));
        Arrays.sort(positions, (pos1, pos2) -> {
            int levelDiff = -Integer.compare(pos1.level(), pos2.level());

            if(levelDiff == 0) {
                int level = pos1.level(); //must be the level of both, as the difference is 0

                return Integer.compare(manhattanDistance(centrePosition.x() >> level, centrePosition.y() >> level, pos1.x(), pos1.z()),
                        manhattanDistance(centrePosition.x() >> level, centrePosition.y() >> level, pos2.x(), pos2.z()));
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
        this.loadedPositions.add(tile.pos());
        this.waitingPositions.remove(tile.pos());
        this.fillWaiting();
    }

    public static int manhattanDistance(int x1, int z1, int x2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(z1 - z2);
    }

    public Collection<DetailBasedTile> tiles() {
        return this.entries.values().stream().map(entry -> entry.tile).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private class Entry {
        @Nullable
        private DetailBasedTile tile;

        private CompletableFuture<DetailBasedTile> tileFuture;

        private final TilePos pos;

        public Entry(TilePos pos) {
            this.pos = pos;
            this.schedule();
        }

        public void schedule() {
            this.tileFuture = DetailBasedViewTracker.this.mapView.loadingFuture(this.pos, DetailBasedViewTracker.this.generationExecutor);
            this.tileFuture.thenAcceptAsync((tile) -> {
                this.tile = tile;
                DetailBasedViewTracker.this.tileCompleted(tile);
            }, DetailBasedViewTracker.this.trackerExecutor);
        }

        public void unload() {
            if(this.tileFuture != null) {
                this.tileFuture.cancel(false);
            }
            if(this.tile != null && this.tile.texture() != null) {
                DetailBasedViewTracker.this.mapView.scheduleTask(() -> {
                    if(this.tile.texture() != null)
                        DetailBasedViewTracker.this.mapView.freeTexture(this.tile.texture());
                });
            }
        }
    }
}
