package net.minecraft.src;

import com.pclewis.mcpatcher.mod.Colorizer;//Spout HD
import java.util.Random;

public class BiomeGenSwamp extends BiomeGenBase {
	protected BiomeGenSwamp(int par1) {
		super(par1);
		this.biomeDecorator.treesPerChunk = 2;
		this.biomeDecorator.flowersPerChunk = -999;
		this.biomeDecorator.deadBushPerChunk = 1;
		this.biomeDecorator.mushroomsPerChunk = 8;
		this.biomeDecorator.reedsPerChunk = 10;
		this.biomeDecorator.clayPerChunk = 1;
		this.biomeDecorator.waterlilyPerChunk = 4;
		this.waterColorMultiplier = 14745518;
	}

	public WorldGenerator getRandomWorldGenForTrees(Random par1Random) {
		return this.worldGenSwamp;
	}

	public int getBiomeGrassColor() {
		double var1 = (double)this.getFloatTemperature();
		double var3 = (double)this.getFloatRainfall();
		return Colorizer.colorizeBiome(((ColorizerGrass.getGrassColor(var1, var3) & 16711422) + 5115470) / 2, Colorizer.COLOR_MAP_SWAMP_GRASS, var1, var3);
	}

	public int getBiomeFoliageColor() {
		double var1 = (double)this.getFloatTemperature();
		double var3 = (double)this.getFloatRainfall();
		return Colorizer.colorizeBiome(((ColorizerFoliage.getFoliageColor(var1, var3) & 16711422) + 5115470) / 2, Colorizer.COLOR_MAP_SWAMP_FOLIAGE, var1, var3);
	}
}
