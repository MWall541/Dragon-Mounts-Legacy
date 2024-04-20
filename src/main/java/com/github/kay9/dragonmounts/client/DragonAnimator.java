package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.util.CircularBuffer;
import com.github.kay9.dragonmounts.util.LerpedFloat;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Animation control class to put useless reptiles in motion.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@SuppressWarnings({"unused", "DataFlowIssue"})
public class DragonAnimator
{
    // constants
    private static final int JAW_OPENING_TIME_FOR_ATTACK = 5;

    private final TameableDragon dragon;

    // entity parameters
    private float partialTicks;
    private float moveTime;
    private float moveSpeed;
    private float lookYaw;
    private float lookPitch;
    private double prevRenderYawOffset;
    private double yawAbs;

    // timing vars
    private float animBase;
    private float cycleOfs;
    private float anim;
    private float ground;
    private float flutter;
    private float walk;
    private float sit;
    private float jaw;
    private float speed;

    // timing interp vars
    private final LerpedFloat animTimer = new LerpedFloat();
    private final LerpedFloat groundTimer = new LerpedFloat.Clamped(1, 0, 1);
    private final LerpedFloat flutterTimer = LerpedFloat.unit();
    private final LerpedFloat walkTimer = LerpedFloat.unit();
    private final LerpedFloat sitTimer = LerpedFloat.unit();
    private final LerpedFloat jawTimer = LerpedFloat.unit();
    private final LerpedFloat speedTimer = new LerpedFloat.Clamped(1, 0, 1);

    // trails
    private boolean initTrails = false;
    private final CircularBuffer yTrail = new CircularBuffer(8);
    private final CircularBuffer yawTrail = new CircularBuffer(16);
    private final CircularBuffer pitchTrail = new CircularBuffer(16);

    // model flags
    private boolean onGround;
    private boolean openJaw;
    private boolean wingsDown;

    // animation parameters
    private final float[] wingArm = new float[3];
    private final float[] wingForearm = new float[3];
    private final float[] wingArmFlutter = new float[3];
    private final float[] wingForearmFlutter = new float[3];
    private final float[] wingArmGlide = new float[3];
    private final float[] wingForearmGlide = new float[3];
    private final float[] wingArmGround = new float[3];
    private final float[] wingForearmGround = new float[3];

    // final X rotation angles for ground
    private final float[] xGround = {0, 0, 0, 0};

    // X rotation angles for ground
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private final float[][] xGroundStand = {
            {0.8f, -1.5f, 1.3f, 0},
            {-0.3f, 1.5f, -0.2f, 0},
    };
    private final float[][] xGroundSit = {
            {0.3f, -1.8f, 1.8f, 0},
            {-0.8f, 1.8f, -0.9f, 0},
    };

    // X rotation angles for walking
    // 1st dim - animation keyframe
    // 2nd dim - front, hind
    // 3rd dim - thigh, crus, foot, toe
    private final float[][][] xGroundWalk = {{
            {0.4f, -1.4f, 1.3f, 0},    // move down and forward
            {0.1f, 1.2f, -0.5f, 0}     // move back
    }, {
            {1.2f, -1.6f, 1.3f, 0},    // move back
            {-0.3f, 2.1f, -0.9f, 0.6f} // move up and forward
    }, {
            {0.9f, -2.1f, 1.8f, 0.6f}, // move up and forward
            {-0.7f, 1.4f, -0.2f, 0}    // move down and forward
    }};

    // final X rotation angles for walking
    private final float[] xGroundWalk2 = {0, 0, 0, 0};

    // Y rotation angles for ground, thigh only
    private final float[] yGroundStand = {-0.25f, 0.25f};
    private final float[] yGroundSit = {0.1f, 0.35f};
    private final float[] yGroundWalk = {-0.1f, 0.1f};

    // X rotation angles for air
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private final float[][] xAirAll = {{0, 0, 0, 0}, {0, 0, 0, 0}};

    // Y rotation angles for air, thigh only
    private final float[] yAirAll = {-0.1f, 0.1f};

    public DragonAnimator(TameableDragon dragon)
    {
        this.dragon = dragon;
    }

    public void setPartialTicks(float partialTicks)
    {
        this.partialTicks = partialTicks;
    }

    public void setMovement(float moveTime, float moveSpeed)
    {
        this.moveTime = moveTime;
        this.moveSpeed = moveSpeed;
    }

    public void setLook(float lookYaw, float lookPitch)
    {
        // don't twist the neck
        this.lookYaw = Mth.clamp(lookYaw, -120, 120);
        this.lookPitch = Mth.clamp(lookPitch, -90, 90);
    }

    /**
     * Applies the animations on the model. Called every frame before the model
     * is rendered.
     *
     * @param model model to animate
     */
    public void animate(DragonModel model)
    {
        anim = animTimer.get(partialTicks);
        ground = groundTimer.get(partialTicks);
        flutter = flutterTimer.get(partialTicks);
        walk = walkTimer.get(partialTicks);
        sit = sitTimer.get(partialTicks);
        jaw = jawTimer.get(partialTicks);
        speed = speedTimer.get(partialTicks);

        animBase = anim * ((float) Math.PI) * 2;
        cycleOfs = Mth.sin(animBase - 1) + 1;

        // check if the wings are moving down and trigger the event
        boolean newWingsDown = cycleOfs > 1;
        if (newWingsDown && !wingsDown && flutter != 0) dragon.onWingsDown(speed);
        wingsDown = newWingsDown;

        // update flags
        model.back.visible = !dragon.isSaddled();

        cycleOfs = (cycleOfs * cycleOfs + cycleOfs * 2) * 0.05f;

        // reduce up/down amplitude
        cycleOfs *= Mth.clampedLerp(0.5f, 1, flutter);
        cycleOfs *= Mth.clampedLerp(1, 0.5f, ground);

        // animate body parts
        animHeadAndNeck(model);
        animTail(model);
        animWings(model);
        animLegs(model);
    }

    public void tick()
    {
        setOnGround(!dragon.isFlying());

        // init trails
        if (!initTrails)
        {
            yTrail.fill((float) dragon.getY());
            yawTrail.fill(dragon.yBodyRot);
            pitchTrail.fill(getModelPitch());
            initTrails = true;
        }

        // don't move anything during death sequence
        if (dragon.getHealth() <= 0)
        {
            animTimer.sync();
            groundTimer.sync();
            flutterTimer.sync();
            walkTimer.sync();
            sitTimer.sync();
            jawTimer.sync();
            return;
        }

        float speedMax = 0.05f;
        float xD = (float) dragon.getX() - (float) dragon.xo;
        float yD = (float) dragon.getY() - (float) dragon.yo;
        float zD = (float) dragon.getZ() - (float) dragon.zo;
        float speedEnt = (xD * xD + zD * zD);
        float speedMulti = Mth.clamp(speedEnt / speedMax, 0, 1);

        // update main animation timer
        float animAdd = 0.035f;

        // depend timing speed on movement
        if (!onGround)
        {
            animAdd += (1 - speedMulti) * animAdd;
        }

        animTimer.add(animAdd);

        // update ground transition
        float groundVal = groundTimer.get();
        if (onGround)
        {
            groundVal *= 0.95f;
            groundVal += 0.08f;
        }
        else
        {
            groundVal -= 0.1f;
        }
        groundTimer.set(groundVal);

        // update flutter transition
        boolean flutterFlag = !onGround && (dragon.horizontalCollision || yD > -0.1 || speedEnt < speedMax);
        flutterTimer.add(flutterFlag? 0.1f : -0.1f);

        // update walking transition
        boolean walkFlag = moveSpeed > 0.1 && !dragon.isInSittingPose();
        float walkVal = 0.1f;
        walkTimer.add(walkFlag? walkVal : -walkVal);

        // update sitting transisiton
        float sitVal = sitTimer.get();
        sitVal += dragon.isInSittingPose()? 0.1f : -0.1f;
        sitVal *= 0.95f;
        sitTimer.set(sitVal);

        // TODO: find better attack animation method
//        int ticksSinceLastAttack = dragon.getTicksSince
//
//        boolean jawFlag = (ticksSinceLastAttack >= 0 && ticksSinceLastAttack < JAW_OPENING_TIME_FOR_ATTACK);
//        jawTimer.add(jawFlag? 0.2f : -0.2f);

        // update speed transition
        boolean speedFlag = speedEnt > speedMax || dragon.isNearGround();
        float speedValue = 0.05f;
        speedTimer.add(speedFlag? speedValue : -speedValue);

        // update trailers
        double yawDiff = dragon.yBodyRot - prevRenderYawOffset;
        prevRenderYawOffset = dragon.yBodyRot;

        // filter out 360 degrees wrapping
        if (yawDiff < 180 && yawDiff > -180) yawAbs += yawDiff;

        yTrail.update((float) dragon.getY());
        yawTrail.update((float) -yawAbs);
        pitchTrail.update(getModelPitch());
    }

    protected void animHeadAndNeck(DragonModel model)
    {
        model.neck.setPos(0, 14, -8);
        model.neck.setRotation(0, 0, 0);

        float health = dragon.getHealthFraction();
        float neckSize;

        for (int i = 0; i < model.neckProxy.length; i++)
        {
            float vertMulti = (i + 1) / (float) model.neckProxy.length;

            float baseRotX = Mth.cos((float) i * 0.45f + animBase) * 0.15f;
            baseRotX *= Mth.clampedLerp(0.2f, 1, flutter);
            baseRotX *= Mth.clampedLerp(1, 0.2f, sit);
            float ofsRotX = Mth.sin(vertMulti * ((float) Math.PI) * 0.9f) * 0.75f;

            // basic up/down movement
            model.neck.xRot = baseRotX;
            // reduce rotation when on ground
            model.neck.xRot *= terpSmoothStep(1, 0.5f, walk);
            // flex neck down when hovering
            model.neck.xRot += (1 - speed) * vertMulti;
            // lower neck on low health
            model.neck.xRot -= Mth.clampedLerp(0, ofsRotX, ground * health);
            // use looking yaw
            model.neck.yRot = (float) Math.toRadians(lookYaw) * vertMulti * speed;

            // update scale
            float v = Mth.clampedLerp(1.6f, 1, vertMulti);
            ((ModelPartAccess) (Object) model.neck).setRenderScale(v, v, 0.6f);

            // hide the first and every second scale
            model.neckScale.visible = i % 2 != 0 || i == 0;

            // update proxy
            model.neckProxy[i].update();

            // move next proxy behind the current one
            neckSize = DragonModel.NECK_SIZE * ((ModelPartAccess) (Object) model.neck).getZScale() - 1.4f;
            model.neck.x -= Mth.sin(model.neck.yRot) * Mth.cos(model.neck.xRot) * neckSize;
            model.neck.y += Mth.sin(model.neck.xRot) * neckSize;
            model.neck.z -= Mth.cos(model.neck.yRot) * Mth.cos(model.neck.xRot) * neckSize;
        }

        model.head.xRot = (float) Math.toRadians(lookPitch) + (1 - speed);
        model.head.yRot = model.neck.yRot;
        model.head.zRot = model.neck.zRot * 0.2f;

        model.head.x = model.neck.x;
        model.head.y = model.neck.y;
        model.head.z = model.neck.z;

        model.jaw.xRot = jaw * 0.75f;
        model.jaw.xRot += (1 - Mth.sin(animBase)) * 0.1f * flutter;
    }

    protected void animWings(DragonModel model)
    {
        // move wings slower while sitting
        float aSpeed = sit > 0? 0.6f : 1;

        // animation speeds
        float a1 = animBase * aSpeed * 0.35f;
        float a2 = animBase * aSpeed * 0.5f;
        float a3 = animBase * aSpeed * 0.75f;

        if (ground < 1)
        {
            // fluttering
            wingArmFlutter[0] = 0.125f - Mth.cos(animBase) * 0.2f;
            wingArmFlutter[1] = 0.25f;
            wingArmFlutter[2] = (Mth.sin(animBase) + 0.125f) * 0.8f;

            wingForearmFlutter[0] = 0;
            wingForearmFlutter[1] = -wingArmFlutter[1] * 2;
            wingForearmFlutter[2] = -(Mth.sin(animBase + 2) + 0.5f) * 0.75f;

            // gliding
            wingArmGlide[0] = -0.25f - Mth.cos(animBase * 2) * Mth.cos(animBase * 1.5f) * 0.04f;
            wingArmGlide[1] = 0.25f;
            wingArmGlide[2] = 0.35f + Mth.sin(animBase) * 0.05f;

            wingForearmGlide[0] = 0;
            wingForearmGlide[1] = -wingArmGlide[1] * 2;
            wingForearmGlide[2] = -0.25f + (Mth.sin(animBase + 2) + 0.5f) * 0.05f;
        }

        if (ground > 0)
        {
            // standing
            wingArmGround[0] = 0;
            wingArmGround[1] = 1.4f - Mth.sin(a1) * Mth.sin(a2) * 0.02f;
            wingArmGround[2] = 0.8f + Mth.sin(a2) * Mth.sin(a3) * 0.05f;

            // walking
            wingArmGround[1] += Mth.sin(moveTime * 0.5f) * 0.02f * walk;
            wingArmGround[2] += Mth.cos(moveTime * 0.5f) * 0.05f * walk;

            wingForearmGround[0] = 0;
            wingForearmGround[1] = -wingArmGround[1] * 2;
            wingForearmGround[2] = 0;
        }

        // interpolate between fluttering and gliding
        slerpArrays(wingArmGlide, wingArmFlutter, wingArm, flutter);
        slerpArrays(wingForearmGlide, wingForearmFlutter, wingForearm, flutter);

        // interpolate between flying and grounded
        slerpArrays(wingArm, wingArmGround, wingArm, ground);
        slerpArrays(wingForearm, wingForearmGround, wingForearm, ground);

        // apply angles
        mirrorRotate(model.wingArms[0], model.wingArms[1], wingArm[0], wingArm[1], wingArm[2]);
//        model.wingArm.xRot += 1 - speed;

        mirrorRotate(model.wingForearms[0], model.wingForearms[1], wingForearm[0],wingForearm[1],wingForearm[2]);


        // interpolate between folded and unfolded wing angles
        float[] yFold = new float[]{2.7f, 2.8f, 2.9f, 3.0f};
        float[] yUnfold = new float[]{0.1f, 0.9f, 1.7f, 2.5f};

        // set wing finger angles
        float rotX = 0;
        float rotYOfs = Mth.sin(a1) * Mth.sin(a2) * 0.03f;
        float rotYMulti = 1;

        for (int i = 0; i < model.wingFingers[0].length; i++)
        {
            mirrorRotate(model.wingFingers[0][i],
                    model.wingFingers[1][i],
                    rotX += 0.005f,
                    terpSmoothStep(yUnfold[i], yFold[i] + rotYOfs * rotYMulti, ground),
                    0);

            rotYMulti -= 0.2f;
        }
    }

    @SuppressWarnings("UnusedAssignment")
    protected void animTail(DragonModel model)
    {
        model.tail.x = 0;
        model.tail.y = 16;
        model.tail.z = 62;

        model.tail.xRot = 0;
        model.tail.yRot = 0;
        model.tail.zRot = 0;

        float rotXStand = 0;
        float rotYStand = 0;
        float rotXSit = 0;
        float rotYSit = 0;
        float rotXAir = 0;
        float rotYAir = 0;

        for (int i = 0; i < model.tailProxy.length; i++)
        {
            float vertMulti = (i + 1) / (float) model.tailProxy.length;

            // idle
            float amp = 0.1f + i / (model.tailProxy.length * 2f);

            rotXStand = (i - model.tailProxy.length * 0.6f) * -amp * 0.4f;
            rotXStand += (Mth.sin(animBase * 0.2f) * Mth.sin(animBase * 0.37f) * 0.4f * amp - 0.1f) * (1 - sit);
            rotXSit = rotXStand * 0.8f;

            rotYStand = (rotYStand + Mth.sin(i * 0.45f + animBase * 0.5f)) * amp * 0.4f;
            rotYSit = Mth.sin(vertMulti * ((float) Math.PI)) * ((float) Math.PI) * 1.2f - 0.5f; // curl to the left

            rotXAir -= Mth.sin(i * 0.45f + animBase) * 0.04f * Mth.clampedLerp(0.3f, 1, flutter);

            // interpolate between sitting and standing
            model.tail.xRot = Mth.clampedLerp(rotXStand, rotXSit, sit);
            model.tail.yRot = Mth.clampedLerp(rotYStand, rotYSit, sit);

            // interpolate between flying and grounded
            model.tail.xRot = Mth.clampedLerp(rotXAir, model.tail.xRot, ground);
            model.tail.yRot = Mth.clampedLerp(rotYAir, model.tail.yRot, ground);

            // body movement
            float angleLimit = 160 * vertMulti;
            float yawOfs = Mth.clamp(yawTrail.get(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);
            float pitchOfs = Mth.clamp(pitchTrail.get(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);

            model.tail.xRot += Math.toRadians(pitchOfs);
            model.tail.xRot -= (1 - speed) * vertMulti * 2;
            model.tail.yRot += Math.toRadians(180 - yawOfs);

            if (model.tailHornRight != null)
            {
                // display horns near the tip
                var atIndex = i > model.tailProxy.length - 7 && i < model.tailProxy.length - 3;
                model.tailHornLeft.visible = model.tailHornRight.visible = atIndex;
            }

            // update scale
            float neckScale = Mth.clampedLerp(1.5f, 0.3f, vertMulti);
            ((ModelPartAccess) (Object) model.tail).setRenderScale(neckScale, neckScale, neckScale);

            // update proxy
            model.tailProxy[i].update();

            // move next proxy behind the current one
            float tailSize = DragonModel.TAIL_SIZE * ((ModelPartAccess) (Object) model.tail).getZScale() - 0.7f;
            model.tail.y += Mth.sin(model.tail.xRot) * tailSize;
            model.tail.z -= Mth.cos(model.tail.yRot) * Mth.cos(model.tail.xRot) * tailSize;
            model.tail.x -= Mth.sin(model.tail.yRot) * Mth.cos(model.tail.xRot) * tailSize;
        }
    }

    protected void animLegs(DragonModel model)
    {
        // dangling legs for flying
        if (ground < 1)
        {
            float footAirOfs = cycleOfs * 0.1f;
            float footAirX = 0.75f + cycleOfs * 0.1f;

            xAirAll[0][0] = 1.3f + footAirOfs;
            xAirAll[0][1] = -(0.7f * speed + 0.1f + footAirOfs);
            xAirAll[0][2] = footAirX;
            xAirAll[0][3] = footAirX * 0.5f;

            xAirAll[1][0] = footAirOfs + 0.6f;
            xAirAll[1][1] = footAirOfs + 0.8f;
            xAirAll[1][2] = footAirX;
            xAirAll[1][3] = footAirX * 0.5f;
        }

        // 0 - front leg, right side
        // 1 - hind leg, right side
        // 2 - front leg, left side
        // 3 - hind leg, left side
        for (int i = 0; i < model.legs.length; i++)
        {
            var thigh = model.legs[i][0];
            var crus = model.legs[i][1];
            var foot = model.legs[i][2];
            var toe = model.legs[i][3];

            thigh.z = (i % 2 == 0)? 4 : 46;

            // final X rotation angles for air
            float[] xAir = xAirAll[i % 2];

            // interpolate between sitting and standing
            slerpArrays(xGroundStand[i % 2], xGroundSit[i % 2], xGround, sit);

            // align the toes so they're always horizontal on the ground
            xGround[3] = -(xGround[0] + xGround[1] + xGround[2]);

            // apply walking cycle
            if (walk > 0)
            {
                // interpolate between the keyframes, based on the cycle
                splineArrays(moveTime * 0.2f, i > 1, xGroundWalk2,
                        xGroundWalk[0][i % 2], xGroundWalk[1][i % 2], xGroundWalk[2][i % 2]);
                // align the toes so they're always horizontal on the ground
                xGroundWalk2[3] -= xGroundWalk2[0] + xGroundWalk2[1] + xGroundWalk2[2];

                slerpArrays(xGround, xGroundWalk2, xGround, walk);
            }

            float yAir = yAirAll[i % 2];
            float yGround;

            // interpolate between sitting and standing
            yGround = terpSmoothStep(yGroundStand[i % 2], yGroundSit[i % 2], sit);

            // interpolate between standing and walking
            yGround = terpSmoothStep(yGround, yGroundWalk[i % 2], walk);

            // interpolate between flying and grounded
            thigh.yRot = terpSmoothStep(yAir, yGround, ground);
            thigh.xRot = terpSmoothStep(xAir[0], xGround[0], ground);
            crus.xRot = terpSmoothStep(xAir[1], xGround[1], ground);
            foot.xRot = terpSmoothStep(xAir[2], xGround[2], ground);
            toe.xRot = terpSmoothStep(xAir[3], xGround[3], ground);

            if (i > 1) thigh.yRot *= -1;
        }
    }

    public float getModelPitch()
    {
        return getModelPitch(partialTicks);
    }

    public float getModelPitch(float pt)
    {
        float pitchMovingMax = 90;
        float pitchMoving = Mth.clamp(yTrail.get(pt, 5, 0) * 10, -pitchMovingMax, pitchMovingMax);
        float pitchHover = 60;
        return terpSmoothStep(pitchHover, pitchMoving, speed);
    }

    @SuppressWarnings("SameReturnValue")
    public float getModelOffsetX()
    {
        return 0;
    }

    public float getModelOffsetY()
    {
        return 1.5f + (-sit * 0.6f);
    }

    public float getModelOffsetZ()
    {
        return -1.5f;
    }

    public void setOnGround(boolean onGround)
    {
        this.onGround = onGround;
    }

    public void setOpenJaw(boolean openJaw)
    {
        this.openJaw = openJaw;
    }

    private static void mirrorRotate(ModelPart rightLimb, ModelPart leftLimb, float xRot, float yRot, float zRot)
    {
        rightLimb.xRot = xRot;
        rightLimb.yRot = yRot;
        rightLimb.zRot = zRot;
        leftLimb.xRot = xRot;
        leftLimb.yRot = -yRot;
        leftLimb.zRot = -zRot;
    }

    private static void slerpArrays(float[] a, float[] b, float[] c, float x)
    {
        if (a.length != b.length || b.length != c.length)
        {
            throw new IllegalArgumentException();
        }

        if (x <= 0)
        {
            System.arraycopy(a, 0, c, 0, a.length);
            return;
        }
        if (x >= 1)
        {
            System.arraycopy(b, 0, c, 0, a.length);
            return;
        }

        for (int i = 0; i < c.length; i++)
        {
            c[i] = terpSmoothStep(a[i], b[i], x);
        }
    }

    private static float terpSmoothStep(float a, float b, float x)
    {
        if (x <= 0)
        {
            return a;
        }
        if (x >= 1)
        {
            return b;
        }
        x = x * x * (3 - 2 * x);
        return a * (1 - x) + b * x;
    }

    private static void splineArrays(float x, boolean shift, float[] result, float[]... nodes)
    {
        int i1 = (int) x % nodes.length;
        int i2 = (i1 + 1) % nodes.length;
        int i3 = (i1 + 2) % nodes.length;

        float[] a1 = nodes[i1];
        float[] a2 = nodes[i2];
        float[] a3 = nodes[i3];

        float xn = x % nodes.length - i1;

        if (shift) terpCatmullRomSpline(xn, result, a2, a3, a1, a2);
        else terpCatmullRomSpline(xn, result, a1, a2, a3, a1);
    }

    private static final float[][] CR = {
            {-0.5f, 1.5f, -1.5f, 0.5f},
            {1.0f, -2.5f, 2.0f, -0.5f},
            {-0.5f, 0.0f, 0.5f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f}
    };

    // http://www.java-gaming.org/index.php?topic=24122.0
    private static void terpCatmullRomSpline(float x, float[] result, float[]... knots)
    {
        int nknots = knots.length;
        int nspans = nknots - 3;
        int knot = 0;
        if (nspans < 1)
        {
            throw new IllegalArgumentException("Spline has too few knots");
        }
        x = Mth.clamp(x, 0, 0.9999f) * nspans;

        int span = (int) x;
        if (span >= nknots - 3)
        {
            span = nknots - 3;
        }

        x -= span;
        knot += span;

        int dimension = result.length;
        for (int i = 0; i < dimension; i++)
        {
            float knot0 = knots[knot][i];
            float knot1 = knots[knot + 1][i];
            float knot2 = knots[knot + 2][i];
            float knot3 = knots[knot + 3][i];

            float c3 = CR[0][0] * knot0 + CR[0][1] * knot1 + CR[0][2] * knot2 + CR[0][3] * knot3;
            float c2 = CR[1][0] * knot0 + CR[1][1] * knot1 + CR[1][2] * knot2 + CR[1][3] * knot3;
            float c1 = CR[2][0] * knot0 + CR[2][1] * knot1 + CR[2][2] * knot2 + CR[2][3] * knot3;
            float c0 = CR[3][0] * knot0 + CR[3][1] * knot1 + CR[3][2] * knot2 + CR[3][3] * knot3;

            result[i] = ((c3 * x + c2) * x + c1) * x + c0;
        }
    }
}
