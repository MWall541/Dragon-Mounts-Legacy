package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Generic model for all winged tetrapod dragons.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonModel extends EntityModel<TameableDragon>
{
    // model constants
    public static final int NECK_SIZE = 10;
    public static final int TAIL_SIZE = 10;
    public static final int VERTS_NECK = 7;
    public static final int VERTS_TAIL = 12;
    public static final int HEAD_OFS = -16;

    // model parts
    public final ModelPart head;
    public final ModelPart neck;
    public final ModelPart neckScale;
    public final ModelPart tail;
    public final ModelPart tailHornLeft;
    public final ModelPart tailHornRight;
    public final ModelPart tailScaleLeft;
    public final ModelPart tailScaleMiddle;
    public final ModelPart tailScaleRight;
    public final ModelPart jaw;
    public final ModelPart body;
    public final ModelPart back;
    public final ModelPart forethigh;
    public final ModelPart forecrus;
    public final ModelPart forefoot;
    public final ModelPart foretoe;
    public final ModelPart hindthigh;
    public final ModelPart hindcrus;
    public final ModelPart hindfoot;
    public final ModelPart hindtoe;
    public final ModelPart wingArm;
    public final ModelPart wingForearm;
    public final ModelPart[] wingFinger = new ModelPart[4];

    // model attributes
    public final ModelPartProxy[] neckProxy = new ModelPartProxy[VERTS_NECK];
    public final ModelPartProxy[] tailProxy = new ModelPartProxy[VERTS_TAIL];
    public final ModelPartProxy[] thighProxy = new ModelPartProxy[4];

    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float pitch;
    public float size;

    public DragonModel(ModelPart root)
    {
        super(RenderType::entityCutout);

        this.body = root.getChild("body");
        this.back = body.getChild("back");
        this.neck = root.getChild("neck");
        this.neckScale = neck.getChild("neck_scale");
        this.head = root.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail = root.getChild("tail");
        this.tailScaleLeft = tail.getChild("left_tail_scale");
        this.tailScaleMiddle = tail.getChild("middle_tail_scale");
        this.tailScaleRight = tail.getChild("right_tail_scale");
        this.tailHornRight = tail.getChild("right_tail_spike");
        this.tailHornLeft = tail.getChild("left_tail_spike");
        this.wingArm = root.getChild("wing_arm");
        this.wingForearm = wingArm.getChild("wing_forearm");
        this.forethigh = root.getChild("forethigh");
        this.forecrus = forethigh.getChild("forecrus");
        this.forefoot = forecrus.getChild("forefoot");
        this.foretoe = forefoot.getChild("foretoe");
        this.hindthigh = root.getChild("hindthigh");
        this.hindcrus = hindthigh.getChild("hindcrus");
        this.hindfoot = hindcrus.getChild("hindfoot");
        this.hindtoe = hindfoot.getChild("hindtoe");

        for (int i = 1; i < 5; i++) wingFinger[i - 1] = wingForearm.getChild("wing_finger_" + i);

        // initialize model proxies
        for (int i = 0; i < neckProxy.length; i++) neckProxy[i] = new ModelPartProxy(neck);
        for (int i = 0; i < tailProxy.length; i++) tailProxy[i] = new ModelPartProxy(tail);
        for (int i = 0; i < 4; i++) thighProxy[i] = new ModelPartProxy(i % 2 == 0? forethigh : hindthigh);
    }


    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        buildBody(root);
        buildNeck(root);
        buildHead(root);
        buildTail(root);
        buildWing(root);
        buildLegs(root);

        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void buildBody(PartDefinition root)
    {
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12, 0, -16, 24, 24, 64)
                        .texOffs(0, 32).addBox(-1, -6, 10, 2, 6, 12).addBox(-1, -6, 30, 2, 6, 12),
                PartPose.offset(0, 4, 8));
        body.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 32).addBox(-1, -6, -10, 2, 6, 12), PartPose.ZERO);
    }

    private static void buildNeck(PartDefinition root)
    {
        PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(112, 88).addBox(-5, -5, -5, NECK_SIZE, NECK_SIZE, NECK_SIZE), PartPose.ZERO);
        neck.addOrReplaceChild("neck_scale", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -3, 2, 4, 6), PartPose.ZERO);
    }

    private static void buildHead(PartDefinition root)
    {
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(56, 88).addBox(-6, -1, -8 + HEAD_OFS, 12, 5, 16) // upper jaw
                        .texOffs(0, 0).addBox(-8, -8, 6 + HEAD_OFS, 16, 16, 16) // upper head
                        .texOffs(48, 0).addBox(-5, -3, -6 + HEAD_OFS, 2, 2, 4) // nostril
                        .mirror().addBox(3, -3, -6 + HEAD_OFS, 2, 2, 4), // nostril
                PartPose.ZERO);
        addHorns(head);
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 88).addBox(-6, 0, -16, 12, 4, 16), PartPose.offset(0, 4, 8 + HEAD_OFS));
    }

    private static void addHorns(PartDefinition head)
    {
        int hornThick = 3;
        int hornLength = 12;

        float hornOfs = -(hornThick / 2f);

        float hornPosX = -5;
        float hornPosY = -8;
        float hornPosZ = 0;

        float hornRotX = 0.523599f;
        float hornRotY = -0.523599f;
        float hornRotZ = 0;

        head.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(28, 32).addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX, hornPosY, hornPosZ, hornRotX, hornRotY, hornRotZ));
        head.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(28, 32).mirror().addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX * -1, hornPosY, hornPosZ, hornRotX, hornRotY * -1, hornRotZ));
    }

    private static void buildTail(PartDefinition root)
    {
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(152, 88).addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE), PartPose.ZERO);
        CubeListBuilder tailSpikeCube = CubeListBuilder.create().texOffs(0, 0).addBox(-1, -8, -3, 2, 4, 6);
        tail.addOrReplaceChild("left_tail_scale", tailSpikeCube, PartPose.rotation(0, 0, 0.785398f));
        tail.addOrReplaceChild("middle_tail_scale", tailSpikeCube, PartPose.ZERO);
        tail.addOrReplaceChild("right_tail_scale", tailSpikeCube, PartPose.rotation(0, 0, -0.785398f));
        addTailSpikes(tail);
    }

    private static void addTailSpikes(PartDefinition tail)
    {
        int hornThick = 3;
        int hornLength = 32;

        float hornOfs = -(hornThick / 2f);

        float hornPosX = 0;
        float hornPosY = hornOfs;
        float hornPosZ = TAIL_SIZE / 2f;

        float hornRotX = -0.261799f;
        float hornRotY = -2.53073f;
        float hornRotZ = 0;

        tail.addOrReplaceChild("right_tail_spike", CubeListBuilder.create().texOffs(0, 117).addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX, hornPosY, hornPosZ, hornRotX, hornRotY, hornRotZ));
        tail.addOrReplaceChild("left_tail_spike", CubeListBuilder.create().texOffs(0, 117).mirror().addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX * -1, hornPosY, hornPosZ, hornRotX, hornRotY * -1, hornRotZ));
    }

    private static void buildWing(PartDefinition root)
    {
        PartDefinition wingArm = root.addOrReplaceChild("wing_arm", CubeListBuilder.create()
                        .texOffs(0, 152).addBox(-28, -3, -3, 28, 6, 6) // bone
                        .texOffs(116, 232).addBox(-28, 0, 2, 28, 0, 24), // skin
                PartPose.offset(-10, 5, 4));

        PartDefinition wingForearm = wingArm.addOrReplaceChild("wing_forearm", CubeListBuilder.create().texOffs(0, 164).addBox(-48, -2, -2, 48, 4, 4), PartPose.offset(-28, 0, 0)); // bone

        CubeListBuilder shortSkins = CubeListBuilder.create()
                .texOffs(0, 172).addBox(-70, -1, -1, 70, 2, 2) // bone
                .texOffs(-49, 176).addBox(-70, 0, 1, 70, 0, 48); // skin

        PartPose shortSkinsPos = PartPose.offset(-47, 0, 0);
        for (int i = 1; i < 4; i++) wingForearm.addOrReplaceChild("wing_finger_" + i, shortSkins, shortSkinsPos);

        wingForearm.addOrReplaceChild("wing_finger_4", CubeListBuilder.create()
                .texOffs(0, 172).addBox(-70, -1, -1, 70, 2, 2) // bone
                .texOffs(-32, 224).addBox(-70, 0, 1, 70, 0, 32), // shortskin
                shortSkinsPos);
    }

    private static void buildLegs(PartDefinition root)
    {
        buildLeg(root, false);
        buildLeg(root, true);
    }

    private static void buildLeg(PartDefinition root, boolean hind)
    {
        // thinner legs for skeletons
//        boolean skeleton = breed == EnumDragonBreed.GHOST; todo during rendering

        float baseLength = 26;
        String baseName = hind? "hind" : "fore";

        // thigh variables
        float thighPosX = -11;
        float thighPosY = 18;
        float thighPosZ = 4;

        int thighThick = 9; /* - (skeleton? 2 : 0);*/
        int thighLength = (int) (baseLength * (hind? 0.9f : 0.77f));

        if (hind)
        {
            thighThick++;
            thighPosY -= 5;
        }

        float thighOfs = -(thighThick / 2f);

        PartDefinition thigh = root.addOrReplaceChild(baseName + "thigh", CubeListBuilder.create().texOffs(112, hind? 29 : 0).addBox(thighOfs, thighOfs, thighOfs, thighThick, thighLength, thighThick), PartPose.offset(thighPosX, thighPosY, thighPosZ));

        // crus variables
        float crusPosX = 0;
        float crusPosY = thighLength + thighOfs;
        float crusPosZ = 0;

        int crusThick = thighThick - 2;
        int crusLength = (int) (baseLength * (hind? 0.7f : 0.8f));

        if (hind)
        {
            crusThick--;
            crusLength -= 2;
        }

        float crusOfs = -(crusThick / 2f);

        PartDefinition crus = thigh.addOrReplaceChild(baseName + "crus", CubeListBuilder.create().texOffs(hind? 152 : 148, hind? 29 : 0).addBox(crusOfs, crusOfs, crusOfs, crusThick, crusLength, crusThick), PartPose.offset(crusPosX, crusPosY, crusPosZ));

        // foot variables
        float footPosX = 0;
        float footPosY = crusLength + (crusOfs / 2f);
        float footPosZ = 0;

        int footWidth = crusThick + 2/* + (skeleton? 2 : 0)*/;
        int footHeight = 4;
        int footLength = (int) (baseLength * (hind? 0.67f : 0.34f));

        float footOfsX = -(footWidth / 2f);
        float footOfsY = -(footHeight / 2f);
        float footOfsZ = footLength * -0.75f;

        PartDefinition foot = crus.addOrReplaceChild(baseName + "foot", CubeListBuilder.create().texOffs(hind? 180 : 210, hind? 29 : 0).addBox(footOfsX, footOfsY, footOfsZ, footWidth, footHeight, footLength), PartPose.offset(footPosX, footPosY, footPosZ));

        // toe variables
        int toeWidth = footWidth;
        int toeHeight = footHeight;
        int toeLength = (int) (baseLength * (hind? 0.27f : 0.33f));

        float toePosX = 0;
        float toePosY = 0;
        float toePosZ = footOfsZ - (footOfsY / 2f);

        float toeOfsX = -(toeWidth / 2f);
        float toeOfsY = -(toeHeight / 2f);
        float toeOfsZ = -toeLength;

        foot.addOrReplaceChild(baseName + "toe", CubeListBuilder.create().texOffs(hind? 215 : 176, hind? 29 : 0).addBox(toeOfsX, toeOfsY, toeOfsZ, toeWidth, toeHeight, toeLength), PartPose.offset(toePosX, toePosY, toePosZ));
    }

    @Override
    public void prepareMobModel(TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pPartialTick)
    {
        boolean middleScales = dragon.breed.showMiddleTailScales();
        tailScaleMiddle.visible = middleScales;
        tailScaleRight.visible = tailScaleLeft.visible = !middleScales;

        dragon.getAnimator().setPartialTicks(pPartialTick);
    }

    @Override
    public void setupAnim(TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        size = dragon.getScale();

        DragonAnimator animator = dragon.getAnimator();
        animator.setLook(pNetHeadYaw, pHeadPitch);
        animator.setMovement(pLimbSwing, pLimbSwingAmount * size);
        dragon.getAnimator().animate(this);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vertices, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        ps.pushPose();
        ps.translate(offsetX, offsetY, offsetZ);
        ps.mulPose(Vector3f.XN.rotationDegrees(pitch));

        body.render(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        renderHead(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        for (ModelPartProxy proxy : neckProxy) proxy.render(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        for (ModelPartProxy proxy : tailProxy) proxy.render(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        renderWings(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        renderLegs(ps, vertices, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);

        ps.popPose();
    }

    protected void renderHead(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        float headScale = 1.4f / (size + 0.5f);
        ((ModelPartAccess)(Object) head).setRenderScale(headScale, headScale, headScale);
        head.render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    private static final Matrix4f inverseX = Matrix4f.createScaleMatrix(-1, 1, 1);
    private static final Matrix3f inverseNormal = new Matrix3f(inverseX);

    protected void renderWings(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        ps.pushPose();
        ps.scale(1.1f, 1.1f, 1.1f);

        wingArm.render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
//        wingArm.render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
        ps.popPose();
    }

    protected void renderLegs(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        thighProxy[0].render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
        thighProxy[1].render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
//        for (int i = 0; i < thighProxy.length; i++)
//        {
//            thighProxy[i].render(ps, vertices, packedLight, packedOverlay, pRed, pGreen, pBlue, pAlpha);
//        }
    }
}