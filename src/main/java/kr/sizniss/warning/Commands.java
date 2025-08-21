package kr.sizniss.warning;

import kr.sizniss.warning.customevents.PlayerWarnCumulativeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Commands implements CommandExecutor {

    private String serverTitle = Files.getServerTitle();
    private boolean warnBroadcast = Files.getWarnBroadcast();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("warning.op")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("Reload")) {
                    Warning plugin = Warning.plugin;
                    Bukkit.getPluginManager().disablePlugin(plugin); // 플러그인 비활성화
                    Bukkit.getPluginManager().enablePlugin(plugin); // 플러그인 활성화
                    sender.sendMessage(serverTitle + " §f§l플러그인을 리로드하였습니다!");
                } else if (args[0].equalsIgnoreCase("Info")) {
                    if (args.length > 1) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                        if (player != null) {
                            sender.sendMessage("");
                            sender.sendMessage(" " + serverTitle);
                            sender.sendMessage(" §6§l" + player.getName() + "§f§l님의 경고 정보:");
                            if (Files.getWarnList(player) != null && Files.getWarnList(player).size() > 0) {
                                for(int i = 0; i < Files.getWarnList(player).size(); i++) {
                                    String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Files.getWarnDate(player, i));
                                    String executorStr = Files.getWarnExecutor(player, i).getName(); // 속도 저하의 주범
                                    String reasonStr = Files.getWarnReason(player, i);

                                    if (Files.isInvaliedWarn(player, i)) { // 경고의 유효 날짜가 지난 경우
                                        sender.sendMessage(" §f- §7" + i + "§7: §mDate: " + dateStr);
                                        sender.sendMessage("    §0. §7§mExecutor: " + executorStr);
                                        sender.sendMessage("    §0. §7§mReason: " + reasonStr);
                                    } else { // 경고의 유효 날짜가 지나지 않은 경우
                                        sender.sendMessage(" §f- §7" + i + "§7: Date: " + dateStr);
                                        sender.sendMessage("    §0. §7Executor: " + executorStr);
                                        sender.sendMessage("    §0. §7Reason: " + reasonStr);
                                    }
                                }
                            } else {
                                sender.sendMessage(" §f- []");
                            }
                        } else {
                            sender.sendMessage(serverTitle + " §f§l해당 플레이어(§6§l" + args[1] + "§f§l)를 찾을 수 없습니다!");
                        }
                    } else {
                        sender.sendMessage(serverTitle + " §f§l/Warn Info §e§l<Player>§7§l: §f플레이어의 경고 정보를 확인합니다.");
                    }
                } else if (args[0].equalsIgnoreCase("Add")) {
                    if (args.length > 1) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                        if (player != null) {
                            if (args.length > 2) {
                                Date date = new Date();
                                OfflinePlayer executor = sender.getName().equals("CONSOLE") ? player : (OfflinePlayer)sender;
                                String reason = "";
                                for(int i = 2; i < args.length; i++) {
                                    reason = reason + args[i];
                                    if (i + 1 < args.length) {
                                        reason = reason + " ";
                                    }
                                }

                                Files.addWarn(player, date, executor, reason);
                                Files files = Warning.files;
                                files.saveData(); // 데이터 저장
                                sender.sendMessage(serverTitle + " §f§l해당 플레이어(§6§l" + player.getName() + "§f§l)에게 경고를 추가하였습니다.");
                                if (warnBroadcast) {
                                    Bukkit.broadcastMessage(" ");
                                    Bukkit.broadcastMessage(" " + serverTitle);
                                    Bukkit.broadcastMessage(" §6§l" + player.getName() + "§f§l님은 경고를 받았습니다!");
                                    Bukkit.broadcastMessage(" §f- §7날짜: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
                                    Bukkit.broadcastMessage(" §f- §7경고자: " + executor.getName());
                                    Bukkit.broadcastMessage(" §f- §7사유: " + reason);
                                }

                                // 경고 수가 3회일 경우
                                if (Files.getWarnCount(player) == Files.getWarnCount()) {
                                    // PlayerWarnCumulativeEvent 이벤트 호출
                                    PlayerWarnCumulativeEvent event = new PlayerWarnCumulativeEvent(player);
                                    Bukkit.getServer().getPluginManager().callEvent(event);

                                    if (!event.isCancelled()) {
                                        if (player.isOnline()) {
                                            player.getPlayer().kickPlayer("§4§l경고 3회 누적으로 인한 정지");
                                        }
                                    }
                                }
                            } else {
                                sender.sendMessage(serverTitle + " §f§l/Warn Add §e§l<Player> <Reason>§7§l: §f플레이어에게 경고를 추가합니다.");
                            }
                        } else {
                            sender.sendMessage(serverTitle + " §f§l해당 플레이어(§6§l" + args[1] + "§f§l)를 찾을 수 없습니다!");
                        }
                    } else {
                        sender.sendMessage(serverTitle + " §f§l/Warn Add §e§l<Player> <Reason>§7§l: §f플레이어에게 경고를 추가합니다.");
                    }
                } else if (args[0].equalsIgnoreCase("Remove")) {
                    if (args.length > 1) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                        if (player != null) {
                            if (args.length > 2) {
                                int number = Integer.parseInt(args[2]);

                                Files.removeWarn(player, number);
                                Files files = Warning.files;
                                files.saveData(); // 데이터 저장
                                sender.sendMessage(serverTitle + " §f§l해당 플레이어(§6§l" + player.getName() + "§f§l)에게서 경고를 차감하였습니다.");
                            } else {
                                sender.sendMessage(serverTitle + " §f§l/Warn Remove §e§l<Player> <Number>§7§l: §f플레이어에게서 경고를 차감합니다.");
                            }
                        } else {
                            sender.sendMessage(serverTitle + " §f§l해당 플레이어(§6§l" + args[1] + "§f§l)를 찾을 수 없습니다!");
                        }
                    } else {
                        sender.sendMessage(serverTitle + " §f§l/Warn Remove §e§l<Player> <Number>§7§l: §f플레이어에게서 경고를 차감합니다.");
                    }
                }
            } else {
                sender.sendMessage("");
                sender.sendMessage(" " + serverTitle);
                sender.sendMessage(" §f§l/Warn Reload§7§l: §f플러그인을 리로드합니다.");
                sender.sendMessage(" §f§l/Warn Info §e§l<Player>§7§l: §f플레이어의 경고 정보를 확인합니다.");
                sender.sendMessage(" §f§l/Warn Add §e§l<Player> <Reason>§7§l: §f플레이어에게 경고를 추가합니다.");
                sender.sendMessage(" §f§l/Warn Remove §e§l<Player> <Number>§7§l: §f플레이어에게서 경고를 차감합니다.");
            }
        } else if (sender.hasPermission("warning.user")) {
            OfflinePlayer player = (OfflinePlayer)sender;

            if (player != null) {
                sender.sendMessage("");
                sender.sendMessage(" " + serverTitle);
                sender.sendMessage(" §6§l" + player.getName() + "§f§l님의 경고 정보:");
                if (Files.getWarnList(player) != null && Files.getWarnList(player).size() > 0) {
                    for (int i = 0; i < Files.getWarnList(player).size(); i++) {
                        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Files.getWarnDate(player, i));
                        String executorStr = Files.getWarnExecutor(player, i).getName();
                        String reasonStr = Files.getWarnReason(player, i);

                        if (Files.isInvaliedWarn(player, i)) { // 경고가 유효 기간이 지난 경우
                            sender.sendMessage(" §f- §7" + i + "§7: §mDate: " + dateStr);
                            sender.sendMessage("    §0. §7§mExecutor: " + executorStr);
                            sender.sendMessage("    §0. §7§mReason: " + reasonStr);
                        } else { // 경고가 유효 기간이 지나지 않은 경우
                            sender.sendMessage(" §f- §7" + i + "§7: Date: " + dateStr);
                            sender.sendMessage("    §0. §7Executor: " + executorStr);
                            sender.sendMessage("    §0. §7Reason: " + reasonStr);
                        }
                    }
                } else {
                    sender.sendMessage(" §f- []");
                }
            } else {
                sender.sendMessage(serverTitle + " §f§l플레이어가 아닙니다!");
            }

        }
        return false;
    }

}
