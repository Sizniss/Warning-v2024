package kr.sizniss.warning;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static kr.sizniss.warning.Warning.plugin;

public class Files {

    public static JsonObject config;
    public static JsonObject data;


    public static String toString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    public static String getServerTitle() {
        return config.get("ServerTitle").getAsString();
    }
    public static void setServerTitle(String serverTitle) {
        config.addProperty("ServerTitle", serverTitle);
    }

    public static boolean getWarnBroadcast() { return config.get("WarnBroadcast").getAsBoolean(); }
    public static void setWarnBroadcast(boolean warnBroadcast) { config.addProperty("WarnBroadcast", warnBroadcast); }

    public static int getWarnCount() { return config.get("WarnCount").getAsInt(); }
    public static void setWarnCount(int warnCount) { config.addProperty("WarnCount", warnCount); }

    public static int getExpirePeriod() { return config.get("ExpirePeriod").getAsInt(); }
    public static void setExpirePeriod(int expirePeriod) { config.addProperty("ExpirePeriod", expirePeriod); }


    public static JsonArray getWarnList(OfflinePlayer player) {
        // return data.get(player.getUniqueId().toString()).getAsJsonArray();
        return data.get(player.getUniqueId().toString()) == null ? null : data.get(player.getUniqueId().toString()).getAsJsonArray();
    }
    public static void setWarnList(OfflinePlayer player, JsonArray jsonArray) {
        data.add(player.getUniqueId().toString(), jsonArray);
    }

    public static JsonObject getWarn(OfflinePlayer player, int number) {
        return getWarnList(player).get(number).getAsJsonObject();
    }
    public static void setWarn(OfflinePlayer player, int number, JsonObject jsonObject) {
        getWarnList(player).set(number, jsonObject);
    }
    public static void addWarn(OfflinePlayer player, Date date, OfflinePlayer executor, String reason) {
        JsonObject jsonObject = new JsonObject();

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        String executorStr = executor.getUniqueId().toString();
        String reasonStr = reason;
        jsonObject.addProperty("Date", dateStr);
        jsonObject.addProperty("Executor", executorStr);
        jsonObject.addProperty("Reason", reasonStr);

        JsonArray warnList = getWarnList(player);
        if (warnList != null) {
            getWarnList(player).add(jsonObject);
        } else {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(jsonObject);
            setWarnList(player, jsonArray);
        }

        // 로그 기록
        plugin.getLogger().info(player.getName() + " is warned by " + executor.getName() + ". [Date: " + dateStr + ", Reason: " + reasonStr + "]");
    }
    public static void removeWarn(OfflinePlayer player, int number) {
        getWarnList(player).remove(number);

        // 로그 기록
        plugin.getLogger().info("Remove " + player.getName() + "'s warning number " + number);

        if (getTotalWarnCount(player) == 0) { // 경고가 없을 경우
            data.remove(player.getUniqueId().toString());
        }
    }

    public static Date getWarnDate(OfflinePlayer player, int number) {
        Date date;
        try {
            String str = getWarn(player, number).get("Date").getAsString();
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch(ParseException e) {
            date = null;
        }
        return date;
    }
    public static void setWarnDate(OfflinePlayer player, int number, Date date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        getWarn(player, number).addProperty("Date", dateStr);
    }

    public static OfflinePlayer getWarnExecutor(OfflinePlayer player, int number) {
        for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getUniqueId().equals(UUID.fromString(getWarn(player, number).get("Executor").getAsString()))) {
                return offlinePlayer;
            }
        }
        return Bukkit.getOfflinePlayer(getWarn(player, number).get("Executor").getAsString()); // 주의! 속도 저하를 일으킬 수 있음
    }
    public static void setWarnExecutor(OfflinePlayer player, int number, Player executor) {
        getWarn(player, number).addProperty("Executor", executor.getUniqueId().toString());
    }

    public static String getWarnReason(OfflinePlayer player, int number) {
        return getWarn(player, number).get("Reason").getAsString();
    }
    public static void setWarnReason(OfflinePlayer player, int number, String reason) {
        getWarn(player, number).addProperty("Reason", reason);
    }

    public static int getWarnCount(OfflinePlayer player) {
        if (getWarnList(player) != null) { // 플레이어 경고 데이터가 있을 경우
            int totalWarnCount = getTotalWarnCount(player);
            int pastWarnCount = 0;

            for (int i = 0; i < getWarnList(player).size(); i++) {
                if (isInvaliedWarn(player, i)) { // 경고가 유효 기간이 지난 경우
                    pastWarnCount++;
                }
            }

            return totalWarnCount - pastWarnCount;
        } else { // 플레이어 경고 데이터가 없을 경우
            return 0;
        }
    }
    public static int getTotalWarnCount(OfflinePlayer player) {
        return getWarnList(player) != null ? getWarnList(player).size() : 0;
    }


    public static boolean isInvaliedWarn(OfflinePlayer player, int number) {
        Calendar cal = Calendar.getInstance();
        Date warnDate = Files.getWarnDate(player, number);
        Date currentDate = new Date();
        int expirePeriod = Files.getExpirePeriod();

        cal.setTime(warnDate);
        cal.add(Calendar.DATE, expirePeriod);
        Date expireDate = cal.getTime();
        if (currentDate.after(expireDate)) { return true; }
        else { return false; }
    }


    public Files() {
        loadConfig(); // 콘피그 불러오기
        loadData(); // 데이터 불러오기
    }


    // 콘피그 불러오기 함수
    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "config.json");
        if (!file.exists()) {
            plugin.saveResource("config.json", false);
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            JsonParser parser = new JsonParser();
            config = parser.parse(reader).getAsJsonObject();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    // 콘피그 저장 함수
    public void saveConfig() {
        File file = new File(plugin.getDataFolder(), "config.json");
        if (!file.exists()) {
            plugin.saveResource("config.json", false);
        }
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, writer);
            writer.append(System.lineSeparator());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 데이터 불러오기 함수
    public void loadData() {
        File file = new File(plugin.getDataFolder(), "data.json");
        if (!file.exists()) {
            plugin.saveResource("data.json", false);
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            JsonParser parser = new JsonParser();
            data = parser.parse(reader).getAsJsonObject();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    // 데이터 저장 함수
    public void saveData() {
        File file = new File(plugin.getDataFolder(), "data.json");
        if (!file.exists()) {
            plugin.saveResource("data.json", false);
        }
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
            writer.append(System.lineSeparator());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
