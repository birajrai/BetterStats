package io.github.birajrai.betterstats;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.birajrai.betterstats.commands.DiscordLinkCommand;
import io.github.birajrai.betterstats.commands.DiscordUnLinkCommand;
import io.github.birajrai.betterstats.commands.DownloadCommand;
import io.github.birajrai.betterstats.commands.GiveMedalCommand;
import io.github.birajrai.betterstats.commands.MedalCommand;
import io.github.birajrai.betterstats.commands.MedalsCommand;
import io.github.birajrai.betterstats.commands.MergeCommand;
import io.github.birajrai.betterstats.commands.PlayerMedalsCommand;
import io.github.birajrai.betterstats.commands.PlayerStatsCommand;
import io.github.birajrai.betterstats.commands.TagsCommand;
import io.github.birajrai.betterstats.commands.UpdateAllCommand;
import io.github.birajrai.betterstats.commands.UploadAllCommand;
import io.github.birajrai.betterstats.commands.UploadCommand;
import io.github.birajrai.betterstats.discord.LinkManager;
import io.github.birajrai.betterstats.listeners.BlockListeners;
import io.github.birajrai.betterstats.listeners.EntityListeners;
import io.github.birajrai.betterstats.listeners.ListenersController;
import io.github.birajrai.betterstats.listeners.MessageListener;
import io.github.birajrai.betterstats.listeners.PlayerListeners;
import io.github.birajrai.betterstats.mongoDB.DataBase;
import io.github.birajrai.betterstats.mongoDB.DataBaseManager;
import io.github.birajrai.betterstats.server.ServerManager;
import io.github.birajrai.betterstats.listeners.DiscordChatListener;
import io.github.birajrai.betterstats.listeners.DiscordLinkListener;

import com.google.common.collect.Sets;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class Main extends JavaPlugin {

    private boolean initialized = false;
    private boolean loadError = false;

    public static JDA jda = null;
    public static final Set<GatewayIntent> api = Sets.immutableEnumSet(EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_BANS,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.DIRECT_MESSAGES
    ));

    public static Server currentServer;

    private Logger log;
    private ListenersController controller;
    private DataBaseManager mongoDB;
    private ServerManager serverMan;
    private LinkManager linkMan;

    @Override
    public void onLoad() {

        log = this.getServer().getLogger();
        try {
            loadConfig();

            Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.SEVERE);  //TO NOT HAVE LOGS ON CONSOLE

            currentServer = getServer();
            serverMan = new ServerManager(new DataBase(getConfig()), log, this);
            mongoDB = serverMan.getDataBaseManager();
            DiscordLinkCommand discordLinkCommand = new DiscordLinkCommand(mongoDB, serverMan, linkMan);
            linkMan = new LinkManager(mongoDB, serverMan, discordLinkCommand);
            controller = new ListenersController(mongoDB, serverMan);

            Thread initThread = new Thread(this::init, "[MineStats] - Initialization Discord Bot");
            initThread.setUncaughtExceptionHandler((t, e) -> {
                getLogger().severe("[MineStats] - failed to load Discord functions properly: " + e.getMessage());
            });
            initThread.start();

            initialized = true;

        } catch(Exception e) {
            log.log(Level.INFO, "[MineStats] - Error on enable MongoDB.", e);
            loadError = true;
        }
    }

    @Override
    public void onEnable() {
        if(!initialized) onLoad();

        if(!loadError) {

            saveDefaultConfig();

            PluginManager pm = getServer().getPluginManager();

            getCommand("upload").setExecutor(new UploadCommand(mongoDB));

            getCommand("uploadall").setExecutor(new UploadAllCommand(serverMan));

            getCommand("updateall").setExecutor(new UpdateAllCommand(mongoDB, serverMan));

            getCommand("download").setExecutor(new DownloadCommand(mongoDB, serverMan));

            getCommand("merge").setExecutor(new MergeCommand(mongoDB, serverMan));

            getCommand("givemedal").setExecutor(new GiveMedalCommand(mongoDB, serverMan));

            getCommand("medal").setExecutor(new MedalCommand());

            getCommand("medals").setExecutor(new MedalsCommand(mongoDB, serverMan));

            getCommand("playermedals").setExecutor(new PlayerMedalsCommand(mongoDB, serverMan));

            getCommand("stats").setExecutor(new PlayerStatsCommand(mongoDB, serverMan));

            getCommand("tag").setExecutor(new TagsCommand(this.getServer(), mongoDB, serverMan));

            getCommand("link").setExecutor(new DiscordLinkCommand(mongoDB, serverMan, linkMan));

            getCommand("unlink").setExecutor(new DiscordUnLinkCommand(mongoDB, serverMan, linkMan));


            pm.registerEvents(new BlockListeners(controller), this);
            pm.registerEvents(new PlayerListeners(controller), this);
            pm.registerEvents(new EntityListeners(controller), this);
            pm.registerEvents(new MessageListener(), this);

        }
    }

    public void init() {
        try {
            jda = JDABuilder.create(api)
                    .setToken(getConfig().getString("token"))
                    .addEventListeners(new DiscordChatListener(mongoDB))
                    .addEventListeners(new DiscordLinkListener(linkMan))
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .build();

            jda.awaitReady();
        } catch (LoginException e) {
            log.log(Level.SEVERE, "[MineStats] - The Discord bot Token is incorrect.", e);
        } catch(InterruptedException e) {
            log.log(Level.SEVERE, "[MineStats] - An error occurred while connecting.", e);
        }
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        if(!loadError) {

            if (jda != null) {
                jda.getEventManager().getRegisteredListeners().forEach(listener -> jda.getEventManager().unregister(listener));
                jda.shutdownNow();
            }

            saveConfig();
            serverMan.logOutAllPlayers();
            serverMan.uploadAll();
        }
    }

    public static JDA getJda() {
        return jda;
    }

}
