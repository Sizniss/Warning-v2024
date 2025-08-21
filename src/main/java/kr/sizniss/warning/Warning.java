package kr.sizniss.warning;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Warning extends JavaPlugin {

    public static Warning plugin;
    public static Files files;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        files = new Files(); // 파일 객체 생성
        Bukkit.getPluginManager().registerEvents(new Events(), plugin); // 이벤트 등록
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("Warn"))
        {
            return new Commands().onCommand(sender, command, label, args);
        }
        return false;
    }
}
