package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.widget.Widget;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Implement this to let them synchronize data between server and client
 * see also: {@link Interactable}
 */
public interface ISyncedWidget {

    @SideOnly(Side.CLIENT)
    void readServerData(int id, PacketBuffer buf);

    void readClientData(int id, PacketBuffer buf);

    /**
     * Called each tick on server. Use it to detect and sync changes
     */
    default void onServerTick() {
    }

    /**
     * Sends the written data to {@link #readClientData(int, PacketBuffer)}
     *
     * @param id         helper to determine the type
     * @param bufBuilder data builder
     */
    @SideOnly(Side.CLIENT)
    default void syncToServer(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        getWindow().getContext().sendClientPacket(id, this, getWindow(), bufBuilder);
    }

    /**
     * Sends the written data to {@link #readServerData(int, PacketBuffer)}
     *
     * @param id         helper to determine the type
     * @param bufBuilder data builder
     */
    default void syncToClient(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        getWindow().getContext().sendServerPacket(id, this, getWindow(), bufBuilder);
    }

    ModularWindow getWindow();
}
