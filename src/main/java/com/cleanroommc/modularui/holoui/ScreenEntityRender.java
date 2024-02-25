package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class ScreenEntityRender extends Render<HoloScreenEntity> {

    public ScreenEntityRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull HoloScreenEntity entity) {
        return null;
    }

    @Override
    public void doRender(@NotNull HoloScreenEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GuiScreenWrapper screenWrapper = entity.getWrapper();
        if (screenWrapper == null) return;

        Plane3D plane3D = entity.getPlane3D();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (entity.getOrientation() == ScreenOrientation.TO_PLAYER) {
            plane3D.transform(player.getPositionVector(), entity.getPositionVector(), player.getLookVec());
        } else {
            plane3D.transform();
        }
        var mouse = calculateMousePos(player, entity, player.getLookVec());
        screenWrapper.drawScreen(mouse.getX(), mouse.getY(), partialTicks);
        GlStateManager.popMatrix();
    }

    private Vec3i calculateMousePos(EntityPlayer player, HoloScreenEntity entity, Vec3d looking) {
        var entpos = entity.getPositionVector();
        var pos = player.getPositionVector();

        var plane = entity.getPlane3D();
        var planeRot = plane.getRotation();
        var diff = entpos.subtract(pos);

        double l1 = Math.sqrt(looking.x * looking.x + looking.y * looking.y);
        double l2 = Math.sqrt(looking.z * looking.z + looking.y * looking.y);
        double x = Math.acos(looking.x / l1);
        double y = Math.asin(looking.z / l2);

        ModularUI.LOGGER.warn(String.format("looking x {%s}; y {%s}", Math.toDegrees(x), Math.toDegrees(y)));
        // horizontals only
//        var diffH = new Vec3d(diff.x, 0, diff.z);
//        var normalH = diffH.normalize();
//        var lookingH = new Vec3d(looking.x, 0, looking.z);

        // calculate offset to left/right of screen
//        double offsetH = Math.atan((plane.getWidth() / 16 / 2) / diffH.length());
//        var lookRel = lookingH.subtract(normalH).normalize();
//        double lookRH = Math.atan(lookRel.z / lookRel.x) - (Math.PI / 4);

        // check if looking vector is within screen
        // diff is the vector from pos to entity
        // i need to check if the look vector is within the bounds of the screen
        // which means i need to rotate diff or something to line up with the edges of the plane
        // i would be adding width to z assuming the plane is to the right from 0, 0
        // height would be added to y as well
        // however the plane could be at any point, so a rotation would be nicer
        // i only need the rotation from the above example, which i could use
        // i need the length of diff over the width of the plane
        // this /should/ be looking at the plane
        // now to actually calculate the mouse pos
        return new Vec3i(0, 0, 0);
    }
}
