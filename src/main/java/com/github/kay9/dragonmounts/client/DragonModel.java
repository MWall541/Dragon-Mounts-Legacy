package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * Generic model for all winged tetrapod dragons.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@SuppressWarnings("UnnecessaryLocalVariable")
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
    public final ModelPart jaw;
    public final ModelPart body;
    public final ModelPart back;

    // [0][]: right fore  [1][]: right hind  [2][]: left fore  [3][]: left hind
    // [][0]: thigh       [][1]: crus        [][2]: foot       [][3]: toe
    public final ModelPart[][] legs = new ModelPart[4][4];

    // [0]: right  [1]: left
    public final ModelPart[] wingArms;
    public final ModelPart[] wingForearms;

    // [][0]: finger 1  [][1]: finger 2  [][2]: finger 3  [][3]: finger 4
    public final ModelPart[][] wingFingers = new ModelPart[2][4];


    // model attributes
    public final ModelPartProxy[] neckProxy = new ModelPartProxy[VERTS_NECK];
    public final ModelPartProxy[] tailProxy = new ModelPartProxy[VERTS_TAIL];

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
        this.tailHornRight = getNullableChild(tail, "right_tail_spike");
        this.tailHornLeft = getNullableChild(tail, "left_tail_spike");

        var rightWingArm = root.getChild("right_wing_arm");
        var leftWingArm = root.getChild("left_wing_arm");
        var rightWingForearm = rightWingArm.getChild("right_wing_forearm");
        var leftWingForearm = leftWingArm.getChild("left_wing_forearm");

        this.wingArms = new ModelPart[] {rightWingArm, leftWingArm};
        this.wingForearms = new ModelPart[] {rightWingForearm, leftWingForearm};

        for (int i = 1; i < 5; i++)
        {
            wingFingers[0][i - 1] = rightWingForearm.getChild("right_wing_finger_" + i);
            wingFingers[1][i - 1] = leftWingForearm.getChild("left_wing_finger_" + i);
        }

        for (int i = 0; i < legs.length; i++)
        {
            var right = i < 2;
            var dirName = right? "right_" : "left_";
            var type = i % 2 == 0? "fore_" : "hind_";
            var parts = new String[]{"thigh", "crus", "foot", "toe"};
            var parent = root;
            for (int j = 0; j < parts.length; j++)
                parent = legs[i][j] = parent.getChild(dirName + type + parts[j]);
        }

        // initialize model proxies
        for (int i = 0; i < neckProxy.length; i++) neckProxy[i] = new ModelPartProxy(neck);
        for (int i = 0; i < tailProxy.length; i++) tailProxy[i] = new ModelPartProxy(tail);

        if (tailHornRight != null)
            //noinspection ConstantConditions
            tailHornRight.visible = tailHornLeft.visible = false;
    }


    public static LayerDefinition createBodyLayer(Properties properties)
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        buildBody(root);
        buildNeck(root);
        buildHead(root);
        buildTail(root, properties);
        buildWings(root);
        buildLegs(root, properties);

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

    private static void buildTail(PartDefinition root, Properties properties)
    {
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(152, 88).addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE), PartPose.ZERO);
        CubeListBuilder tailSpikeCube = CubeListBuilder.create().texOffs(0, 0).addBox(-1, -8, -3, 2, 4, 6);
        if (properties.middleTailScales())
            tail.addOrReplaceChild("middle_tail_scale", tailSpikeCube, PartPose.ZERO);
        else
        {
            tail.addOrReplaceChild("left_tail_scale", tailSpikeCube, PartPose.rotation(0, 0, 0.785398f));
            tail.addOrReplaceChild("right_tail_scale", tailSpikeCube, PartPose.rotation(0, 0, -0.785398f));
        }

        if (properties.tailHorns()) addTailSpikes(tail);
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

        tail.addOrReplaceChild("right_tail_spike",
                CubeListBuilder.create().texOffs(0, 117).addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX, hornPosY, hornPosZ, hornRotX, hornRotY, hornRotZ));
        tail.addOrReplaceChild("left_tail_spike",
                CubeListBuilder.create().texOffs(0, 117).mirror().addBox(hornOfs, hornOfs, hornOfs, hornThick, hornThick, hornLength),
                PartPose.offsetAndRotation(hornPosX * -1, hornPosY, hornPosZ, hornRotX, hornRotY * -1, hornRotZ));
    }

    private static void buildWings(PartDefinition root)
    {
        buildWing(root,false); // right wing
        buildWing(root, true); // left wing
    }

    private static void buildWing(PartDefinition root, boolean mirror)
    {
        var direction = mirror? "left_" : "right_";

        var wingArmCube = CubeListBuilder.create().mirror(mirror);
        centerMirroredBox(wingArmCube.texOffs(0, 152), mirror, -28, -3, -3, 28, 6, 6); // bone
        centerMirroredBox(wingArmCube.texOffs(116, 232), mirror, -28, 0, 2, 28, 0, 24); // skin

        var foreArmCube = centerMirroredBox(CubeListBuilder.create().mirror(mirror).texOffs(0, 164), mirror, -48, -2, -2, 48, 4, 4); // bone

        var shortSkinCube = CubeListBuilder.create().mirror(mirror);
        centerMirroredBox(shortSkinCube.texOffs(0, 172), mirror, -70, -1, -1, 70, 2, 2); // bone
        centerMirroredBox(shortSkinCube.texOffs(-49, 176), mirror, -70, 0, 1, 70, 0, 48); // skin
        var shortSkinPos = mirrorXPos(-47, 0, 0, mirror);

        var lastFingerCube = CubeListBuilder.create().mirror(mirror);
        centerMirroredBox(lastFingerCube.texOffs(0, 172), mirror, -70, -1, -1, 70, 2, 2); // bone
        centerMirroredBox(lastFingerCube.texOffs(-32, 224), mirror, -70, 0, 1, 70, 0, 32); // shortskin

        var arm = root.addOrReplaceChild(direction + "wing_arm", wingArmCube, mirrorXPos(-10, 5, 4, mirror));
        var foreArm = arm.addOrReplaceChild(direction + "wing_forearm", foreArmCube, mirrorXPos(-28, 0, 0, mirror));
        for (int j = 1; j < 4; j++) foreArm.addOrReplaceChild(direction + "wing_finger_" + j, shortSkinCube, shortSkinPos);
        foreArm.addOrReplaceChild(direction + "wing_finger_4", lastFingerCube, shortSkinPos);
    }

    private static void buildLegs(PartDefinition root, Properties properties)
    {
        buildLeg(root, false, properties.thinLegs(), false); // front right
        buildLeg(root, true, properties.thinLegs(), false); // back right
        buildLeg(root, false, properties.thinLegs(), true); // front left
        buildLeg(root, true, properties.thinLegs(), true); // back left
    }

    private static void buildLeg(PartDefinition root, boolean hind, boolean thin, boolean mirror)
    {
        float baseLength = 26;
        var baseName = (mirror? "left_" : "right_") + (hind? "hind_" : "fore_");

        // thigh variables
        float thighPosX = -11;
        float thighPosY = 18;
        float thighPosZ = 4;

        int thighThick = 9 - (thin? 2 : 0);
        int thighLength = (int) (baseLength * (hind? 0.9f : 0.77f));

        if (hind)
        {
            thighThick++;
            thighPosY -= 5;
        }

        float thighOfs = -(thighThick / 2f);

        PartDefinition thigh = root.addOrReplaceChild(baseName + "thigh", CubeListBuilder.create().texOffs(112, hind? 29 : 0).addBox(thighOfs, thighOfs, thighOfs, thighThick, thighLength, thighThick), mirrorXPos(thighPosX, thighPosY, thighPosZ, mirror));

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

        PartDefinition crus = thigh.addOrReplaceChild(baseName + "crus",
                CubeListBuilder.create().texOffs(hind? 152 : 148, hind? 29 : 0).addBox(crusOfs, crusOfs, crusOfs, crusThick, crusLength, crusThick),
                mirrorXPos(crusPosX, crusPosY, crusPosZ, mirror));

        // foot variables
        float footPosX = 0;
        float footPosY = crusLength + (crusOfs / 2f);
        float footPosZ = 0;

        int footWidth = crusThick + 2 + (thin? 2 : 0);
        int footHeight = 4;
        int footLength = (int) (baseLength * (hind? 0.67f : 0.34f));

        float footOfsX = -(footWidth / 2f);
        float footOfsY = -(footHeight / 2f);
        float footOfsZ = footLength * -0.75f;

        PartDefinition foot = crus.addOrReplaceChild(baseName + "foot",
                CubeListBuilder.create().texOffs(hind? 180 : 210, hind? 29 : 0).addBox(footOfsX, footOfsY, footOfsZ, footWidth, footHeight, footLength),
                mirrorXPos(footPosX, footPosY, footPosZ, mirror));

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

        foot.addOrReplaceChild(baseName + "toe",
                CubeListBuilder.create().texOffs(hind? 215 : 176, hind? 29 : 0).addBox(toeOfsX, toeOfsY, toeOfsZ, toeWidth, toeHeight, toeLength),
                mirrorXPos(toePosX, toePosY, toePosZ, mirror));
    }

    @Override
    public void prepareMobModel(TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pPartialTick)
    {
        size = Math.min(dragon.getAgeScale(), 1);
        dragon.getAnimator().setPartialTicks(pPartialTick);
    }

    @Override
    public void setupAnim(TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        DragonAnimator animator = dragon.getAnimator();
        animator.setLook(pNetHeadYaw, pHeadPitch);
        animator.setMovement(pLimbSwing, pLimbSwingAmount * dragon.getAgeScale());
        dragon.getAnimator().animate(this);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vertices, int pPackedLight, int pPackedOverlay, int pColor)
    {
        body.render(ps, vertices, pPackedLight, pPackedOverlay, pColor);
        renderHead(ps, vertices, pPackedLight, pPackedOverlay, pColor);
        for (ModelPartProxy proxy : neckProxy)
            proxy.render(ps, vertices, pPackedLight, pPackedOverlay, pColor);
        for (ModelPartProxy proxy : tailProxy)
            proxy.render(ps, vertices, pPackedLight, pPackedOverlay, pColor);
        renderWings(ps, vertices, pPackedLight, pPackedOverlay, pColor);
        renderLegs(ps, vertices, pPackedLight, pPackedOverlay, pColor);
    }

    protected void renderHead(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, int pColor)
    {
        float headScale = 1.4f / (size + 0.4f);
        //noinspection DataFlowIssue
        ((ModelPartAccess) (Object) head).setRenderScale(headScale, headScale, headScale);
        head.render(ps, vertices, packedLight, packedOverlay, pColor);
    }

    public void renderWings(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, int pColor)
    {
        ps.pushPose();
        ps.scale(1.1f, 1.1f, 1.1f);
        wingArms[0].render(ps, vertices, packedLight, packedOverlay, pColor);
        wingArms[1].render(ps, vertices, packedLight, packedOverlay, pColor);
        ps.popPose();
    }

    protected void renderLegs(PoseStack ps, VertexConsumer vertices, int packedLight, int packedOverlay, int pColor)
    {
        for (ModelPart[] leg : legs)
            leg[0].render(ps, vertices, packedLight, packedOverlay, pColor);
    }

    private static CubeListBuilder centerMirroredBox(CubeListBuilder builder, boolean mirror, float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ)
    {
        if (mirror) pOriginX = 0;
        return builder.addBox(pOriginX, pOriginY, pOriginZ, pDimensionX, pDimensionY, pDimensionZ);
    }

    private static PartPose mirrorXPos(float x, float y, float z, boolean mirror)
    {
        if (mirror) x = -x;
        return PartPose.offset(x, y, z);
    }

    /**
     * Hacky workaround for getting model parts that may or may not exist.
     */
    @Nullable
    private static ModelPart getNullableChild(ModelPart parent, String child)
    {
        try
        {
            return parent.getChild(child);
        }
        catch (NoSuchElementException ignore)
        {
            return null;
        }
    }

    public record Properties(boolean middleTailScales, boolean tailHorns, boolean thinLegs)
    {
        public static final Properties STANDARD = new Properties(true, false, false);

        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(func -> func.group(
                Codec.BOOL.optionalFieldOf("middle_tail_scales", true).forGetter(Properties::middleTailScales),
                Codec.BOOL.optionalFieldOf("tail_horns", false).forGetter(Properties::tailHorns),
                Codec.BOOL.optionalFieldOf("thin_legs", false).forGetter(Properties::thinLegs)
        ).apply(func, Properties::new));
    }
}