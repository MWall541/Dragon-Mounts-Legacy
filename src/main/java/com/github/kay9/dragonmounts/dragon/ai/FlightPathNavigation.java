package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * todo: Currently a very poor attempt at modifying {@link net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation} and {@link AmphibiousNodeEvaluator}
 * so it's in need of an actual evaluation trying to figure out how it all works and developing our own.
 * The Amphibious-type approaches were selected as their meant to be friendly to both land and water, so we just
 * modified it to be friendly to land and air.
 * For now, it works.
 */
public class FlightPathNavigation extends PathNavigation
{
    private final TameableDragon dragon;

    public FlightPathNavigation(TameableDragon dragon, Level pLevel)
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

    @Override
    protected Vec3 getTempMobPos()
    {
        return new Vec3(dragon.getX(), dragon.getY(0.5), dragon.getZ());
    }

    @Override
    protected boolean canUpdatePath()
    {
        return true;
    }

    @Override
    protected double getGroundY(Vec3 pVec)
    {
        return dragon.canFly()? pVec.y() : WalkNodeEvaluator.getFloorLevel(level, BlockPos.containing(pVec));
    }

    @Override
    protected boolean canMoveDirectly(Vec3 from, Vec3 to)
    {
        return dragon.isFlying() && isClearForMovementBetween(dragon, from, to, true);
    }

    @Override
    public boolean isStableDestination(BlockPos pPos)
    {
        return dragon.canFly() || super.isStableDestination(pPos);
    }

    private class NodeEvaluator extends WalkNodeEvaluator
    {
        private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

        @Override
        public Node getStart()
        {
            return dragon.isFlying()?
                    getStartNode(new BlockPos(Mth.floor(mob.getBoundingBox().minX), Mth.floor(mob.getBoundingBox().minY + (mob.getBbHeight() * 0.5)), Mth.floor(mob.getBoundingBox().minZ))) :
                    super.getStart();
        }

        @Override
        public Target getGoal(double pX, double pY, double pZ)
        {
            int y = Mth.floor(dragon.isFlying()? (pY + dragon.getBbHeight() * 0.5) : pY);
            return getTargetFromNode(getNode(Mth.floor(pX), y, Mth.floor(pZ)));
        }

        @Nullable
        protected Node findAcceptedNode(int pX, int pY, int pZ, int pVerticalDeltaLimit, double nodeFloor, Direction pDirection, BlockPathTypes pPathType)
        {
            Node node = null;
            BlockPos.MutableBlockPos checkCarat = new BlockPos.MutableBlockPos();
            double currentFloor = getFloorLevel(checkCarat.set(pX, pY, pZ));
            if (currentFloor - nodeFloor > Math.max(1.125, mob.maxUpStep()))
            {
                return null;
            }
            else
            {
                BlockPathTypes pathTypeAtPos = getCachedBlockType(mob, pX, pY, pZ);
                float malusAtPos = mob.getPathfindingMalus(pathTypeAtPos);
                double halfMobHeight = mob.getBbWidth() * 0.5;
                if (malusAtPos >= 0.0F)
                {
                    node = getNodeAndUpdateCostToMax(pX, pY, pZ, pathTypeAtPos, malusAtPos);
                }

                if (doesBlockHavePartialCollision(pPathType) && node != null && node.costMalus >= 0.0F && !canReachWithoutCollision(node))
                {
                    node = null;
                }

                if (pathTypeAtPos != BlockPathTypes.WALKABLE && (!dragon.canFly() || pathTypeAtPos != BlockPathTypes.OPEN))
                {
                    if ((node == null || node.costMalus < 0.0F) && pVerticalDeltaLimit > 0 && (pathTypeAtPos != BlockPathTypes.FENCE || canWalkOverFences()) && pathTypeAtPos != BlockPathTypes.UNPASSABLE_RAIL && pathTypeAtPos != BlockPathTypes.TRAPDOOR && pathTypeAtPos != BlockPathTypes.POWDER_SNOW)
                    {
                        node = findAcceptedNode(pX, pY + 1, pZ, pVerticalDeltaLimit - 1, nodeFloor, pDirection, pPathType);
                        if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && mob.getBbWidth() < 1.0F)
                        {
                            double d2 = (double) (pX - pDirection.getStepX()) + 0.5D;
                            double d3 = (double) (pZ - pDirection.getStepZ()) + 0.5D;
                            AABB aabb = new AABB(d2 - halfMobHeight, getFloorLevel(checkCarat.set(d2, (double) (pY + 1), d3)) + 0.001D, d3 - halfMobHeight, d2 + halfMobHeight, (double) mob.getBbHeight() + getFloorLevel(checkCarat.set((double) node.x, (double) node.y, (double) node.z)) - 0.002D, d3 + halfMobHeight);
                            if (hasCollisions(aabb))
                            {
                                node = null;
                            }
                        }
                    }

                    if (!isAmphibious() && pathTypeAtPos == BlockPathTypes.WATER && !canFloat())
                    {
                        if (getCachedBlockType(mob, pX, pY - 1, pZ) != BlockPathTypes.WATER)
                        {
                            return node;
                        }

                        while (pY > mob.level().getMinBuildHeight())
                        {
                            --pY;
                            pathTypeAtPos = getCachedBlockType(mob, pX, pY, pZ);
                            if (pathTypeAtPos != BlockPathTypes.WATER)
                            {
                                return node;
                            }

                            node = getNodeAndUpdateCostToMax(pX, pY, pZ, pathTypeAtPos, mob.getPathfindingMalus(pathTypeAtPos));
                        }
                    }

                    if (!dragon.canFly() && pathTypeAtPos == BlockPathTypes.OPEN)
                    {
                        int j = 0;
                        int i = pY;

                        while (pathTypeAtPos == BlockPathTypes.OPEN)
                        {
                            --pY;
                            if (pY < mob.level().getMinBuildHeight())
                            {
                                return getBlockedNode(pX, i, pZ);
                            }

//                            if (j++ >= mob.getMaxFallDistance())
//                            {
//                                return getBlockedNode(pX, pY, pZ);
//                            }

                            pathTypeAtPos = getCachedBlockType(mob, pX, pY, pZ);
                            malusAtPos = mob.getPathfindingMalus(pathTypeAtPos);
                            if (pathTypeAtPos != BlockPathTypes.OPEN && malusAtPos >= 0.0F)
                            {
                                node = getNodeAndUpdateCostToMax(pX, pY, pZ, pathTypeAtPos, malusAtPos);
                                break;
                            }

                            if (malusAtPos < 0.0F)
                            {
                                return getBlockedNode(pX, pY, pZ);
                            }
                        }
                    }

                    if (doesBlockHavePartialCollision(pathTypeAtPos) && node == null)
                    {
                        node = getNode(pX, pY, pZ);
                        node.closed = true;
                        node.type = pathTypeAtPos;
                        node.costMalus = pathTypeAtPos.getMalus();
                    }

                }

                return node;
            }
        }

        private Node getNodeAndUpdateCostToMax(int pX, int pY, int pZ, BlockPathTypes pType, float pCostMalus)
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
                if (this.hasCollisions(aabb))
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

        private Node getBlockedNode(int pX, int pY, int pZ)
        {
            Node node = getNode(pX, pY, pZ);
            node.type = BlockPathTypes.BLOCKED;
            node.costMalus = -1.0F;
            return node;
        }

        @Override
        public int getNeighbors(Node[] pOutputArray, Node pNode)
        {
            int i = super.getNeighbors(pOutputArray, pNode);
            BlockPathTypes blockpathtypes = getCachedBlockType(mob, pNode.x, pNode.y + 1, pNode.z);
            BlockPathTypes blockpathtypes1 = getCachedBlockType(mob, pNode.x, pNode.y, pNode.z);
            int j;
            if (mob.getPathfindingMalus(blockpathtypes) >= 0.0F && blockpathtypes1 != BlockPathTypes.STICKY_HONEY)
            {
                j = Mth.floor(Math.max(1.0F, mob.getStepHeight()));
            }
            else
            {
                j = 0;
            }

            double d0 = getFloorLevel(new BlockPos(pNode.x, pNode.y, pNode.z));
            Node node = findAcceptedNode(pNode.x, pNode.y + 1, pNode.z, Math.max(0, j - 1), d0, Direction.UP, blockpathtypes1);
            Node node1 = findAcceptedNode(pNode.x, pNode.y - 1, pNode.z, j, d0, Direction.DOWN, blockpathtypes1);
            if (isVerticalNeighborValid(node, pNode))
            {
                pOutputArray[i++] = node;
            }

            if (isVerticalNeighborValid(node1, pNode) && blockpathtypes1 != BlockPathTypes.TRAPDOOR)
            {
                pOutputArray[i++] = node1;
            }

            return i;
        }

        @Override
        protected double getFloorLevel(BlockPos pPos)
        {
            return dragon.canFly() && level.getBlockState(pPos).isAir()?
                    pPos.getY() + (dragon.getBbHeight() * 0.5) :
                    getFloorLevel(level, pPos);
        }

        private boolean isVerticalNeighborValid(Node neighbor, Node pNode)
        {
            return isNeighborValid(neighbor, pNode) && neighbor.type == BlockPathTypes.OPEN;
        }
    }
}
