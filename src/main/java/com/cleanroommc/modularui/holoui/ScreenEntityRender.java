package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector2d;

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
        var holoPos = entity.getPositionVector();
        var pos = player.getPositionVector().add(0, player.getEyeHeight(), 0);

        var plane = entity.getPlane3D();
        var planeRot = plane.getRotation();

        // get the difference in the player look vector to the difference of the player's eye position and holo position
        var diff = holoPos.subtract(pos);
        double s1 = looking.x / diff.x, s2 = looking.y / diff.y, s3 = looking.z / diff.z;

        var diffH = diff.subtract(0, diff.y, 0);
        var diffV = diff.subtract(diff.x, 0, diff.z)
                .add(0, 0, diffH.length());

        // get relative vector for looking
        // 0, 0, 0 means the player is looking exactly at the center of the plane
        var lookRel = looking.subtract(diff.normalize());

        // calculate the angle of diff for horizontal and vertical
        double a3 = Math.atan(diffH.z / diffH.x);
//        double l4 = Math.sqrt(lookRel.z * lookRel.z + lookRel.x * lookRel.x);
        double a4 = Math.atan(diffV.y / diffV.z);

        // rotate look vec separately to not induce rotation error
        // rotate the look relative vector to match the rotation of the plane
        var lookRelH = lookRel
                .subtract(0, lookRel.y, 0); // remove the y component

        double l3 = lookRelH.length(); // get the length as is

        // normalize and rotate
        lookRelH = lookRelH.normalize()
                .rotateYaw((float) (planeRot.y - a3))
                .normalize();

        var lookRelV = lookRel
                .subtract(lookRel.x, 0, lookRel.z) // remove x and z
                .add(0, 0, l3) // add back to x as total length
                .rotatePitch((float) (planeRot.x - a4)) // pitch handles y and z
                .normalize();

        // get the angle of the look relative vector, and the hypotenuse is 1 since it should be normalized
        double aH = Math.asin(lookRelH.z); // horizontal, idk if this needs x or z
        // might need to offset angle maybe based on x
//        aH -= Math.PI / 2;
        double aV = Math.asin(lookRelV.y); // vertical
//        aV -= Math.PI / 2;

        // using the angle, now find the horizontal and vertical lengths
        double lH = Math.sin(aH) * diff.length();
        double lV = Math.cos(aV) * diff.length();
//        lV += diff.y; // move pos up/down based on diff
        // handle left-right movement somehow

        // convert from block distance to pixel distance
        lH *= 16 * plane.getScale();
        lV *= 16 * plane.getScale();

        // shift values so that 0, 0 is top left
        lH += plane.getWidth() / 2;
        lV += plane.getHeight() / 2;

        // compare to the size of the plane
        if (// lH > 0 && lH < plane.getWidth() &&
            lV > 0 && lV < plane.getHeight()) {
            // we are within the plane
            return new Vec3i(lH, lV, 0);
        }

//        double l1 = Math.sqrt(looking.x * looking.x + looking.y * looking.y);
//        double l2 = Math.sqrt(looking.z * looking.z + looking.y * looking.y);
//        double x = Math.acos(looking.x / l1);
//        double y = Math.asin(looking.z / l2);

        // horizontals only
//        var diffH = new Vec3d(diff.x, 0, diff.z);
//        var normalH = diffH.normalize();
//        var lookingH = new Vec3d(looking.x, 0, looking.z);

        // calculate offset to left/right of screen
//        double offsetH = Math.atan((plane.getWidth() / 16 / 2) / diffH.length());
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
