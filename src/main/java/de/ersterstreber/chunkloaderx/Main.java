package de.ersterstreber.chunkloaderx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.ersterstreber.chunkloaderx.commands.KMCCommand;
import de.ersterstreber.chunkloaderx.config.Config;
import de.ersterstreber.chunkloaderx.listeners.AsyncPlayerChatListener;
import de.ersterstreber.chunkloaderx.listeners.BlockBreakListener;
import de.ersterstreber.chunkloaderx.listeners.BlockPlaceListener;
import de.ersterstreber.chunkloaderx.listeners.ChunkUnloadListener;
import de.ersterstreber.chunkloaderx.listeners.ExplosionListener;
import de.ersterstreber.chunkloaderx.listeners.PlayerInteractListener;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private static Main instance;
	
	private File chunkFile;
	private YamlConfiguration chunkConfig;
	
	private File chunkLoaderFile;
	private YamlConfiguration chunkLoaderConfig;
	
	private boolean usingVault;
	private double price;
	private boolean usingChunkLoader;
	private int maximalChunks;
	private int loadedTimeCommand;
	private int loadedTimeChunkLoader;
	private boolean dropOnBreak;
	private boolean showParticles;
	
	private List<Chunk> waitingChunks;
	
	private Map<Player, ChunkLoader> renaming;
	
    private Economy economy;
	
	@Override
	public void onEnable() {
		instance = this;
		
		copyConfig();
		
		initAttributes();
		
		setupEconomy();
		
		initFiles();
		
		registerListeners();
		
		getCommand("chunkloader").setExecutor(new KMCCommand());
	}
	
	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new ChunkUnloadListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
		Bukkit.getPluginManager().registerEvents(new ExplosionListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(), this);
	}
	
	private void initAttributes() {
		
		Config cfg = new Config();
		
		usingVault = cfg.usingVault();
		price = cfg.getPrice();
		usingChunkLoader = cfg.usingChunkLoader();
		maximalChunks = cfg.getMaximalChunks();
		loadedTimeCommand = cfg.getLoadedTimeCommand();
		loadedTimeChunkLoader = cfg.getLoadedTimeChunkLoader();
		dropOnBreak = cfg.dropOnBreak();
		showParticles = cfg.showParticles();
		
		waitingChunks = new ArrayList<Chunk>();
		renaming = new HashMap<Player, ChunkLoader>();
	}
	
	private void initFiles() {
		chunkFile = new File("plugins/ChunkLoaderX/chunks.yml");
		if (!chunkFile.exists()) {
			try {
				chunkFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		chunkConfig = YamlConfiguration.loadConfiguration(chunkFile);
		chunkConfig.addDefault("chunks", new ArrayList<String>());
		chunkConfig.options().copyDefaults(true);
		saveChunkConfig();
	
		chunkLoaderFile = new File("plugins/ChunkLoaderX/chunkloader.yml");
		if (!chunkLoaderFile.exists()) {
			try {
				chunkLoaderFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		chunkLoaderConfig = YamlConfiguration.loadConfiguration(chunkLoaderFile);
		chunkLoaderConfig.addDefault("chunkloaders", new ArrayList<String>());
		chunkLoaderConfig.options().copyDefaults(true);
		saveChunkLoaderConfig();
		
		getLogger().log(Level.INFO, "Using economy: " + usingVault());
	}
	
	public List<Chunk> getWaitingChunks() {
		return waitingChunks;
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public YamlConfiguration getChunkConfig() {
		return chunkConfig;
	}
	
	public void saveChunkConfig() {
		try {
			chunkConfig.save(chunkFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public YamlConfiguration getChunkLoaderConfig() {
		return chunkLoaderConfig;
	}
	
	public void saveChunkLoaderConfig() {
		try {
			chunkLoaderConfig.save(chunkLoaderFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Map<Player, ChunkLoader> getRenaming() {
		return renaming;
	}

    private void setupEconomy() {
    	try {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        	if (economyProvider != null) {
        		economy = economyProvider.getProvider();
        		usingVault = true;
        	} else {
        		getLogger().warning("There is no Vault-supported economy-plugin installed, you won't be able to use the economy feature!");
        		usingVault = false;
        	}
    	} catch (NoClassDefFoundError e) {
    		getLogger().warning("There is no Vault-supported economy-plugin installed, you won't be able to use the economy feature!");
    		usingVault = false;
    	}
    }
    
    public boolean usingVault() {
    	return usingVault;
    }
    
    public double getPrice() {
    	return price;
    }
    
    public boolean usingChunkLoader() {
    	return usingChunkLoader;
    }
    
    public int getMaximalChunksPerUser() {
    	return maximalChunks;
    }
    
    public boolean infiniteLoadedTimeCommand() {
    	return loadedTimeCommand == -1;
    }
    
    public int getLoadedTimeCommand() {
    	return loadedTimeCommand;
    }
    
    public boolean infiniteLoadedTimeChunkLoader() {
    	return loadedTimeChunkLoader == -1;
    }
    
    public int getLoadedTimeChunkLoader() {
    	return loadedTimeChunkLoader;
    }
    
    public boolean dropOnBreak() {
    	return dropOnBreak;
    }
    
    public boolean showParticles() {
    	return showParticles;
    }
    
    public Economy getEconomy() {
    	return economy;
    }
    
    private void copyConfig() {
    	File dir = new File("plugins/ChunkLoaderX");
    	if (!dir.isDirectory()) {
    		dir.mkdirs();
    	}
    	
    	if (new File("plugins/ChunkLoaderX/config.yml").exists()) return;
    	
    	InputStream inputStream = null;
    	OutputStream outputStream = null;

    	try {
    		inputStream = getResource("config.yml");

    		outputStream = new FileOutputStream(new File("plugins/ChunkLoaderX/config.yml"));

    		int read = 0;
    		byte[] bytes = new byte[1024];

    		while ((read = inputStream.read(bytes)) != -1) {
    			outputStream.write(bytes, 0, read);
    		}

    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		
    		if (inputStream != null) {
    			try {
    				inputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    		if (outputStream != null) {
    			try {
    				outputStream.flush();
    				outputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
}