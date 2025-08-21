package kr.sizniss.warning;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Events implements Listener {

    @EventHandler
    private void PlayerLoginEvent(PlayerLoginEvent event) {
        OfflinePlayer player = (OfflinePlayer)event.getPlayer();

        if (Files.getWarnList(player) != null) { // 경고 데이터가 비어 있지 않을 경우
            if (Files.getWarnCount(player) >= Files.getWarnCount()) { // 경고 횟수가 3회 이상인 경우
                int warnNumber = 0;
                for (int i = 0; i < Files.getTotalWarnCount(player); i++) {
                    if (!Files.isInvaliedWarn(player, i)) {
                        warnNumber = i;
                        break;
                    }
                }

                Calendar cal = Calendar.getInstance();
                int expirePeriod = Files.getExpirePeriod();
                cal.setTime(Files.getWarnDate(player, Files.getTotalWarnCount(player) - Files.getWarnCount())); // 현재 시간 설정
                cal.add(Calendar.DATE, expirePeriod); // 경고 만기 기간 추가

                Date startDate = Files.getWarnDate(player, Files.getTotalWarnCount(player) - 1);
                Date endDate = cal.getTime();

                String[] reason = new String[Files.getWarnCount(player)];
                for(int i = 0; i < reason.length; i++) {
                    reason[i] = Files.getWarnReason(player, warnNumber + i);
                }

                String startDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate);
                String endDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate);
                String reasonStr = reason[0];

                for(int i = 1; i < reason.length; i++) {
                    // reasonStr = reasonStr + ", \n" + reason[i];
                    reasonStr = reasonStr + "\n" + reason[i];
                }

                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "§4§l경고 3회 누적으로 인한 정지 \n§f( " + startDateStr + " ~ " + endDateStr + " ) \n\n§f§l정지 사유 \n§f" + reasonStr);
            }
        }
    }

}
