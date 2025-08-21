package kr.sizniss.warning.customevents;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerWarnCumulativeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private OfflinePlayer player;


    public PlayerWarnCumulativeEvent(OfflinePlayer player) {
        this.player = player;
    }


    public OfflinePlayer getPlayer() {
        return player;
    }

    public void setPlayer(OfflinePlayer player) {
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
