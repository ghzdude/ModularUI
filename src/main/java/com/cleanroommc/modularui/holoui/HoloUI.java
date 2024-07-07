package com.cleanroommc.modularui.holoui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class HoloUI {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double x, y, z;
        private Plane3D plane3D = new Plane3D();
        private ScreenOrientation orientation = ScreenOrientation.FIXED;

        public Builder at(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Builder at(BlockPos pos) {
            this.x = pos.getX() + 0.5D;
            this.y = pos.getY() + 0.5D;
            this.z = pos.getZ() + 0.5D;
            return this;
        }

        public Builder inFrontOf(EntityPlayer player, double distance, boolean fixed) {
            Vec3d look = player.getLookVec();
            this.orientation = fixed ? ScreenOrientation.FIXED : ScreenOrientation.TO_PLAYER;
            if (fixed) plane3D.setNormal((float) -look.x, 0, (float) -look.z);
            return at(player.posX + look.x * distance, player.posY + player.getEyeHeight() + look.y * distance, player.posZ + look.z * distance);
        }

        public Builder faceToPlayer() {
            this.orientation = ScreenOrientation.TO_PLAYER;
            return this;
        }

        public Builder faceTo(float x, float y, float z) {
            this.orientation = ScreenOrientation.FIXED;
            this.plane3D.setNormal(x, y, z);
            return this;
        }

        public Builder screenAnchor(float x, float y) {
            this.plane3D.setAnchor(x, y);
            return this;
        }

        public Builder virtualScreenSize(int width, int height) {
            this.plane3D.setSize(width, height);
            return this;
        }

        public Builder screenScale(float scale) {
            this.plane3D.setScale(scale);
            return this;
        }

        public Builder plane(Plane3D plane) {
            this.plane3D = plane;
            return this;
        }

        public void open(Consumer<HoloScreenEntity> entityConsumer, World world) {
//            JeiSettingsImpl jeiSettings = new JeiSettingsImpl();
//            jeiSettings.disableJei();
//            screen.getContext().setJeiSettings(jeiSettings);

//            wrapper.getScreen().getContext().isHoloScreen = true;
            HoloScreenEntity screen = new HoloScreenEntity(world, this.plane3D);
            screen.setPosition(this.x, this.y, this.z);
            screen.setOrientation(this.orientation);
            entityConsumer.accept(screen);
            screen.spawnInWorld();
//            holoScreenEntity.setPosition(this.x, this.y, this.z);
//            holoScreenEntity.setWrapper(wrapper);
//            holoScreenEntity.spawnInWorld();
//            holoScreenEntity.setOrientation(this.orientation);
        }

        public void reposition(EntityPlayer player, Collection<HoloScreenEntity> screens) {
            for (HoloScreenEntity screen : screens) {
                screen.setPosition(this.x, this.y, this.z);
                screen.setOrientation(this.orientation);
                if (player.world.isRemote) {
                    var vec = screen.getPositionVector().subtract(player.getPositionVector());
                    screen.getPlane3D().setNormal((float) -vec.x, 0, (float) -vec.z);
                }
            }
        }
    }
}
