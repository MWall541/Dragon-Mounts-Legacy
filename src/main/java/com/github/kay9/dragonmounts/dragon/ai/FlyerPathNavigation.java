package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

// While FlyingPathNavigation has everything we need, it doesn't use getGroundY when setting wantedY, so extend the base...
public class FlyerPathNavigation extends PathNavigation
{
    private final TameableDragon dragon;

    public FlyerPathNavigation(TameableDragon dragon, Level pLevel)
    {
        super(dragon, pLevel);
        this.dragon = dragon;
    }

    @Override
    protected PathFinder createPathFinder(int pMaxVisitedNodes)
    {
        this.nodeEvaluator = new NodeEvaluator();
        return new PathFinder(nodeEvaluator, pMaxVisitedNodes);
    }

    /**
     * If we have (or should have) a direct line of sight to our goal
     */
    @Override
    protected boolean canMoveDirectly(Vec3 from, Vec3 to)
    {
        // only when we're flying, otherwise we'd always move directly and ignore negative malus
        return dragon.isFlying() && isClearForMovementBetween(dragon, from, to, true);
    }

    @Override
    protected boolean canUpdatePath()
    {
        return canFloat() && isInLiquid() || !mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos()
    {
        return mob.position();
    }

    @Nullable
    @Override
    public Path createPath(Entity pEntity, int pAccuracy)
    {
        return createPath(pEntity.blockPosition(), pAccuracy);
    }

    /**
     * Is a stable spot to path find to
     */
    @Override
    public boolean isStableDestination(BlockPos pPos)
    {
        return dragon.canFly() || super.isStableDestination(pPos);
    }

    @Override
    protected void followThePath()
    {
        var nextNode = getPath().getNextNode();

        if (dragon.isFlying())
        {
            // reduce midair circling by allowing "close enough" clauses and advancing to next node
            float maxDistDifference = mob.getBbWidth() * 2.5f;
            Vec3i moveTo = nextNode.asBlockPos();
            float xDif = (float) Math.abs(mob.getX() - moveTo.getX());
            float yDif = (float) Math.abs(mob.getY() - moveTo.getY());
            float zDif = (float) Math.abs(mob.getZ() - moveTo.getZ());
            float dist = Mth.sqrt(xDif * xDif + yDif * yDif + zDif * zDif);

            if (dist < maxDistDifference)
            {
                getPath().advance();
                doStuckDetection(getTempMobPos());
                return; // we can't risk super trying to advance again.
            }
        }
        // lift off if the open node is higher up. Lower bounds the dragon takes care of it's own flight...
        else if (nextNode.type == BlockPathTypes.OPEN && nextNode.y > dragon.getY() && canLiftOff(dragon))
            dragon.liftOff();

        super.followThePath();
    }

    public void setCanPassDoors(boolean can)
    {
        nodeEvaluator.setCanPassDoors(can);
    }

    public static boolean canLiftOff(TameableDragon dragon)
    {
        return dragon.canFly()
                && !dragon.isLeashed()
                && dragon.level().noCollision(dragon, dragon.getBoundingBox().move(0, dragon.getJumpPower(), 0));
    }

    private class NodeEvaluator extends WalkNodeEvaluator
    {
        // used to cache collisions for fast lookup
        private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

        /**
         * The first node that starts the path
         */
        @Override
        public Node getStart()
        {
            return dragon.isFlying() || (dragon.isInWater() && isAmphibious())?
                    getStartNode(new BlockPos(Mth.floor(mob.getBoundingBox().minX), Mth.floor(mob.getBoundingBox().minY + 0.5), Mth.floor(mob.getBoundingBox().minZ))) :
                    super.getStart();
        }

        @Override
        protected boolean canStartAt(BlockPos pPos)
        {
            return mob.getPathfindingMalus(getBlockPathType(mob, pPos)) >= 0f;
        }

        /**
         * Generate a Node (target) for each desired BlockPos we want to path to.
         */
        @Override
        public Target getGoal(double pX, double pY, double pZ)
        {
            int y = Mth.floor(dragon.canFly() || isAmphibious()? (pY + 0.5) : pY);
            return getTargetFromNode(getNode(Mth.floor(pX), y, Mth.floor(pZ)));
        }

        /**
         * Find an acceptable node for this area context.
         * Copied and pasted from super findAcceptedNode, with modification.
         * super tries its best to avoid OPEN path types, while we want to use them since
         * we can fly.
         * This is for FINDING POSSIBLE NODES. Meaning nodes that can LEAD US TO FLY OR SWIM.
         * Meaning: don't do any isFlying checks to do logic. We aren't validating nodes based on our current state,
         * we validate them based on a state that COULD BE true. Hence, FINDING an acceptable node.
         */
        @Nullable
        protected Node findAcceptedNode(int nX, int nY, int nZ, int stepHeightDelta, double originNodeFloor, Direction pDirection, BlockPathTypes pPathType)
        {
            Node node = null;
            BlockPos.MutableBlockPos checkCarat = new BlockPos.MutableBlockPos();
            BlockPathTypes pathTypeAtPos = getCachedBlockType(mob, nX, nY, nZ);
            float malusAtPos = mob.getPathfindingMalus(pathTypeAtPos); // lower the malus here, the more it's preferred by the mob. Negative values however means they are not traversable at all.

            // if the pos is traversable at all...
            if (malusAtPos >= 0.0F)
            {
                // get the node here and set the cost to malusAtPos, if it's greater than the one it had.
                node = getNodeAndSetCost(nX, nY, nZ, pathTypeAtPos, malusAtPos);
            }

            // if the pos is fence or closed door, and it has a traversable cost, and can get to the current node without collisions, block it. Since we can't move through fences or closed doors.
            if (doesBlockHavePartialCollision(pPathType) && node != null && node.costMalus >= 0.0F && !canReachWithoutCollision(node))
            {
                node = null;
            }

            // if the type at the pos is not desirable...
            if (pathTypeAtPos != BlockPathTypes.WALKABLE && (!isAmphibious() || pathTypeAtPos != BlockPathTypes.WATER) && (!dragon.canFly() || pathTypeAtPos != BlockPathTypes.OPEN))
            {
                // if we don't have a traversable node and the pos above the origin node is open, and the pathType at the pos isn't blocked...
                if ((node == null || node.costMalus < 0.0F) && stepHeightDelta > 0 && (pathTypeAtPos != BlockPathTypes.FENCE || canWalkOverFences()) && pathTypeAtPos != BlockPathTypes.UNPASSABLE_RAIL && pathTypeAtPos != BlockPathTypes.TRAPDOOR && pathTypeAtPos != BlockPathTypes.POWDER_SNOW)
                {
                    // recursive; get the node above nY.
                    node = findAcceptedNode(nX, nY + 1, nZ, stepHeightDelta - 1, originNodeFloor, pDirection, pPathType);
                    // if its walkable, and we're small
                    if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && mob.getBbWidth() < 1.0F)
                    {
                        // if the mob's size can't fit in the selected node position, block it. Obviously we can't go there.
                        double halfMobWidth = mob.getBbWidth() * 0.5;
                        double xMinusDirStep = (double) (nX - pDirection.getStepX()) + 0.5D;
                        double zMinusDirStep = (double) (nZ - pDirection.getStepZ()) + 0.5D;
                        AABB aabb = new AABB(
                                xMinusDirStep - halfMobWidth,
                                getFloorLevel(checkCarat.set(xMinusDirStep, (nY + 1), zMinusDirStep)) + 0.001D,
                                zMinusDirStep - halfMobWidth,
                                xMinusDirStep + halfMobWidth,
                                (double) mob.getBbHeight() + getFloorLevel(checkCarat.set(node.x, node.y, (double) node.z)) - 0.002D,
                                zMinusDirStep + halfMobWidth);
                        if (hasCollisions(aabb))
                        {
                            node = null;
                        }
                    }
                }

                if (!isAmphibious() && pathTypeAtPos == BlockPathTypes.WATER && !canFloat())
                {
                    if (getCachedBlockType(mob, nX, nY - 1, nZ) != BlockPathTypes.WATER)
                    {
                        return node;
                    }

                    while (nY > mob.level().getMinBuildHeight())
                    {
                        --nY;
                        pathTypeAtPos = getCachedBlockType(mob, nX, nY, nZ);
                        if (pathTypeAtPos != BlockPathTypes.WATER)
                        {
                            return node;
                        }

                        node = getNodeAndSetCost(nX, nY, nZ, pathTypeAtPos, mob.getPathfindingMalus(pathTypeAtPos));
                    }
                }

                // this block is for cliff control and how we handle them.
                // can we walk off the cliff safely?
                if (!dragon.canFly() && pathTypeAtPos == BlockPathTypes.OPEN)
                {
                    int heightChecked = 0;
                    int originalY = nY;

                    while (pathTypeAtPos == BlockPathTypes.OPEN)
                    {
                        --nY;
                        if (nY < mob.level().getMinBuildHeight())
                        {
                            return getBlockedNode(nX, originalY, nZ);
                        }

                        // dragons who are unable to fly can take fall damage
                        if (heightChecked++ >= mob.getMaxFallDistance())
                        {
                            return getBlockedNode(nX, nY, nZ);
                        }

                        // get the new path type at the lower y cords now.
                        pathTypeAtPos = getCachedBlockType(mob, nX, nY, nZ);
                        // update the malus here to.
                        malusAtPos = mob.getPathfindingMalus(pathTypeAtPos);

                        // if the cliff is lower than our ground clearance OR we found something other than open, use the node there.
                        if (pathTypeAtPos != BlockPathTypes.OPEN && malusAtPos >= 0.0F)
                        {
                            node = getNodeAndSetCost(nX, nY, nZ, pathTypeAtPos, malusAtPos);
                            break;
                        }

                        // we can't traverse here at all.
                        if (malusAtPos < 0.0F)
                            return getBlockedNode(nX, nY, nZ);
                    }
                }

                // if we don't have a node and there's a door or fence here, get the node there and block it.
                if (doesBlockHavePartialCollision(pathTypeAtPos) && node == null)
                {
                    node = getNode(nX, nY, nZ);
                    node.closed = true;
                    node.type = pathTypeAtPos;
                    node.costMalus = pathTypeAtPos.getMalus();
                }

            }

            // prefer to stay out of the air for easier ground paths...
            if (node != null && pathTypeAtPos == BlockPathTypes.OPEN)
                node.costMalus++;

            return node;
        }

        /**
         * Get the node for this location and make set its priority
         */
        private Node getNodeAndSetCost(int pX, int pY, int pZ, BlockPathTypes pType, float pCostMalus)
        {
            Node node = getNode(pX, pY, pZ);
            node.type = pType;
            node.costMalus = Math.max(node.costMalus, pCostMalus);
            return node;
        }

        private static boolean doesBlockHavePartialCollision(BlockPathTypes pBlockPathType)
        {
            return pBlockPathType == BlockPathTypes.FENCE || pBlockPathType == BlockPathTypes.DOOR_WOOD_CLOSED || pBlockPathType == BlockPathTypes.DOOR_IRON_CLOSED;
        }

        private boolean canReachWithoutCollision(Node pNode)
        {
            AABB aabb = mob.getBoundingBox();
            Vec3 vec3 = new Vec3((double) pNode.x - mob.getX() + aabb.getXsize() / 2.0D, (double) pNode.y - mob.getY() + aabb.getYsize() / 2.0D, (double) pNode.z - mob.getZ() + aabb.getZsize() / 2.0D);
            int i = Mth.ceil(vec3.length() / aabb.getSize());
            vec3 = vec3.scale((1.0F / (float) i));

            for (int j = 1; j <= i; ++j)
            {
                aabb = aabb.move(vec3);
                if (hasCollisions(aabb))
                {
                    return false;
                }
            }

            return true;
        }

        private boolean hasCollisions(AABB atBox)
        {
            return collisionCache.computeIfAbsent(atBox, $ -> !level.noCollision(mob, atBox));
        }

        /**
         * Get the node at the position and block it.
         * Does that mean the node is unusable? unsure.
         */
        private Node getBlockedNode(int pX, int pY, int pZ)
        {
            Node node = getNode(pX, pY, pZ);
            node.type = BlockPathTypes.BLOCKED;
            node.costMalus = -1.0F;
            return node;
        }

        /**
         * Generate nodes for valid neighboring areas.
         * WalkNodeEvaluator does not take up or down into account,
         * so we have to do it here.
         */
        @Override
        public int getNeighbors(Node[] pOutputArray, Node pNode)
        {
            int i = super.getNeighbors(pOutputArray, pNode);
            BlockPathTypes pathTypeAbove = getCachedBlockType(mob, pNode.x, pNode.y + 1, pNode.z);
            BlockPathTypes pathType = getCachedBlockType(mob, pNode.x, pNode.y, pNode.z);

            int j = 0;
            if (mob.getPathfindingMalus(pathTypeAbove) >= 0.0F && pathType != BlockPathTypes.STICKY_HONEY)
                j = Mth.floor(Math.max(1.0F, mob.getStepHeight()));

            double d0 = getFloorLevel(new BlockPos(pNode.x, pNode.y, pNode.z));
            Node node = findAcceptedNode(pNode.x, pNode.y + 1, pNode.z, Math.max(0, j - 1), d0, Direction.UP, pathType);
            Node node1 = findAcceptedNode(pNode.x, pNode.y - 1, pNode.z, j, d0, Direction.DOWN, pathType);
            if (isVerticalNeighborValid(node, pNode))
            {
                pOutputArray[i++] = node;
            }

            if (isVerticalNeighborValid(node1, pNode) && pathType != BlockPathTypes.TRAPDOOR)
            {
                pOutputArray[i++] = node1;
            }

            return i;
        }

        @Override
        protected double getFloorLevel(BlockPos pPos)
        {
            return dragon.canFly() && level.getBlockState(pPos).isAir()?
                    pPos.getY() + 0.5 :
                    getFloorLevel(level, pPos);
        }

        private boolean isVerticalNeighborValid(Node neighbor, Node pNode)
        {
            return isNeighborValid(neighbor, pNode) &&
                    ((dragon.canFly() && pNode.type == BlockPathTypes.OPEN) ||
                            (isAmphibious() && neighbor.type == BlockPathTypes.WATER));
        }
    }
}
