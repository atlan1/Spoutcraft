package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class ChunkProviderFlat implements IChunkProvider {
	private World worldObj;
	private Random random;
	private final boolean useStructures;
	private MapGenVillage villageGen = new MapGenVillage(1);

	public ChunkProviderFlat(World par1World, long par2, boolean par4) {
		this.worldObj = par1World;
		this.useStructures = par4;
		this.random = new Random(par2);
	}

	private void generate(byte[] par1ArrayOfByte) {
		int var2 = par1ArrayOfByte.length / 256;

		for (int var3 = 0; var3 < 16; ++var3) {
			for (int var4 = 0; var4 < 16; ++var4) {
				for (int var5 = 0; var5 < var2; ++var5) {
					int var6 = 0;
					if (var5 == 0) {
						var6 = Block.bedrock.blockID;
					} else if (var5 <= 2) {
						var6 = Block.dirt.blockID;
					} else if (var5 == 3) {
						var6 = Block.grass.blockID;
					}

					par1ArrayOfByte[var3 << 11 | var4 << 7 | var5] = (byte)var6;
				}
			}
		}
	}

	public Chunk loadChunk(int par1, int par2) {
		return this.provideChunk(par1, par2);
	}

	public Chunk provideChunk(int par1, int par2) {
		byte[] var3 = new byte[32768];
		this.generate(var3);
		Chunk var4 = new Chunk(this.worldObj, var3, par1, par2);
		if (this.useStructures) {
			this.villageGen.generate(this, this.worldObj, par1, par2, var3);
		}

		BiomeGenBase[] var5 = this.worldObj.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, par1 * 16, par2 * 16, 16, 16);
		byte[] var6 = var4.getBiomeArray();

		for (int var7 = 0; var7 < var6.length; ++var7) {
			var6[var7] = (byte)var5[var7].biomeID;
		}

		var4.generateSkylightMap();
		return var4;
	}

	public boolean chunkExists(int par1, int par2) {
		return true;
	}

	public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {
		this.random.setSeed(this.worldObj.getSeed());
		long var4 = this.random.nextLong() / 2L * 2L + 1L;
		long var6 = this.random.nextLong() / 2L * 2L + 1L;
		this.random.setSeed((long)par2 * var4 + (long)par3 * var6 ^ this.worldObj.getSeed());
		if (this.useStructures) {
			this.villageGen.generateStructuresInChunk(this.worldObj, this.random, par2, par3);
		}
	}

	public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate) {
		return true;
	}

	public boolean unload100OldestChunks() {
		return false;
	}

	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return "FlatLevelSource";
	}

	public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
		BiomeGenBase var5 = this.worldObj.getBiomeGenForCoords(par2, par4);
		return var5 == null?null:var5.getSpawnableList(par1EnumCreatureType);
	}

	public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5) {
		return null;
	}
}
