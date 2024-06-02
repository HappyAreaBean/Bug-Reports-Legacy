package com.leon.bugreport.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.leon.bugreport.BugReportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Objects;

import static com.leon.bugreport.BugReportManager.*;
import static org.bukkit.Bukkit.getServer;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    public static void sendPluginMessage(Player player) {

        FileConfiguration configuration = plugin.getConfig();
        String server = config.getString("serverName", "MyServer");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("Bugreport");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF(player.getName() + " " + server);
            msgout.writeShort(12);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        player.sendPluginMessage(BugReportPlugin.getPlugin(), "BungeeCord", out.toByteArray());

        System.out.println("Message sent.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("Bugreport")) {
            try {
                short len = in.readShort();
                byte[] msgbytes = new byte[len];
                in.readFully(msgbytes);

                DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
                String somedata = msgin.readUTF();
                short somenumber = msgin.readShort();
                System.out.println(somedata + " " + somenumber);

                String[] dataParts = somedata.split(" ");
                if (dataParts.length >= 2) {
                    String playerName = dataParts[0];
                    String serverName = dataParts[1];

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("bugreport.admin")) {
                            onlinePlayer.sendMessage(pluginColor + pluginTitle + " " + Objects.requireNonNullElse(endingPluginTitleColor, ChatColor.GREEN) + "New Report submitted by " + playerName + " from " + serverName + "!");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
