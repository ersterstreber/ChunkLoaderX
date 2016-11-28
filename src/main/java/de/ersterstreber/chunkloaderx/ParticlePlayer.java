package de.ersterstreber.chunkloaderx;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_11_R1.EnumParticle;
import net.minecraft.server.v1_11_R1.PacketPlayOutWorldParticles;

public class ParticlePlayer {

	private int duration;
	
	private int i = 0;
	
	public ParticlePlayer(int d) {
		duration = d;
	}
	
	public void playParticles(final Player p, final Chunk chunk) {
		
		if (!Main.getInstance().showParticles()) return;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (i >= duration * 20) {
					cancel();
					return;
				}
				
				int beginX = chunk.getX() * 16;
				int beginZ = chunk.getZ() * 16;
				
				for (int y = p.getLocation().getBlockY() ; y < p.getLocation().getBlockY() + 3 ; y++) {
				
					for (int z = beginZ ; z < (beginZ + 17) ; z = z + 16) {
						for (int x = beginX ; x < (beginX + 16) ; x++) {
							PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME, true, x, y, z, 0, 0, 0, 0, 1, null);
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);				
						}
					}
				
					for (int x = beginX ; x < (beginX + 17) ; x = x + 16) {
						for (int z = beginZ ; z < (beginZ + 16) ; z++) {
							PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME, true, x, y, z, 0, 0, 0, 0, 1, null);
							((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);				
						}
					}
				}
				i += 10;
			}
		}.runTaskTimerAsynchronously(Main.getInstance(), 0, 10);
	}
	
}
