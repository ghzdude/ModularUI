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

        // get the difference of the player's eye position and holo position
        double a3 = calculateHorizontalAngle(holoPos);
        var posR = pos.rotateYaw((float) a3);
        var holoR = holoPos.rotateYaw((float) a3);
        var diff = holoPos.subtract(pos);
        var diff2 = holoR.subtractReverse(posR);

        // rotate diff based on plane rotation
        double a1 = calculateHorizontalAngle(diff);

        double a2 = calculateHorizontalAngle(looking);


        // rotate to make x zero
        var diffRot = diff.rotateYaw((float) -a1);
        var lookRot = diffRot.rotateYaw((float) a2);

        // the x, y of look rot should be the mouse pos if scaled by the length of diffRot
        double sf = (diffRot.z / lookRot.z);
        double mX = (lookRot.x * sf) * -16;
        mX += plane.getWidth() / 2;
        double mY = plane.getHeight() / 2;

        return new Vec3i(mX, mY, 0);
    }

    private double calculateHorizontalAngle(Vec3d vec) {
        // x is opposite, z is adjacent, theta = atan(x/z)
        double a3 = Math.atan(vec.x / vec.z);
        if (vec.z < 0) {
            a3 *= -1;
            a3 += vec.x < 0 ? Math.PI : -Math.PI;
        }
        return a3;
    }
}
