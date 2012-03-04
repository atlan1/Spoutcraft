package net.minecraft.src;

import com.pclewis.mcpatcher.mod.Colorizer; //Spout
import net.minecraft.src.BlockFluid;
import net.minecraft.src.EntityFX;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

public class EntityRainFX extends EntityFX {

	public EntityRainFX(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6, 0.0D, 0.0D, 0.0D);
		this.motionX *= 0.30000001192092896D;
		this.motionY = (double)((float)Math.random() * 0.2F + 0.1F);
		this.motionZ *= 0.30000001192092896D;
		//Spout HD end
		if (Colorizer.computeWaterColor(this.worldObj.getWorldChunkManager(), this.posX, this.posY, this.posZ)) {
			this.particleRed = Colorizer.waterColor[0];
			this.particleGreen = Colorizer.waterColor[1];
			this.particleBlue = Colorizer.waterColor[2];
		}
		else {
			this.particleRed = 0.2F;
			this.particleGreen = 0.3F;
			this.particleBlue = 1.0F;
		}
		//Spout HD start

		this.setParticleTextureIndex(19 + this.rand.nextInt(4));
		this.setSize(0.01F, 0.01F);
		this.particleGravity = 0.06F;
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
	}

	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7) {
		super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= (double)this.particleGravity;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;
		if (this.particleMaxAge-- <= 0) {
			this.setEntityDead();
		}

		if (this.onGround) {
			if (Math.random() < 0.5D) {
				this.setEntityDead();
			}

			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		Material var1 = this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
		if (var1.isLiquid() || var1.isSolid()) {
			double var2 = (double)((float)(MathHelper.floor_double(this.posY) + 1) - BlockFluid.getFluidHeightPercent(this.worldObj.getBlockMetadata(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))));
			if (this.posY < var2) {
				this.setEntityDead();
			}
		}

	}
}
