/*
 * This file is part of Spoutcraft (http://www.spout.org/).
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.client.chunkcache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.getspout.commons.util.map.TIntPairObjectHashMap;
import org.spoutcraft.client.io.FileUtil;

public class HeightMap {
	private String worldName;
	private final static int INITIAL_CAPACITY = 500;
	private final TIntPairObjectHashMap<HeightChunk> cache = new TIntPairObjectHashMap<HeightChunk>(INITIAL_CAPACITY);
	private static final HashMap<String, HeightMap> heightMaps = new HashMap<String, HeightMap>();
	private static HeightMap lastMap = null; //Faster access to last height-map (which will be the case in most cases)
	private int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
	private boolean initBounds = false;
	private File file = null;
	private HeightChunk lastChunk = null; //Faster access to last accessed chunk

	private class HeightChunk {
		public short heightmap[] = new short[16 * 16];
		public final int x, z;
		public byte[] idmap = new byte[16 * 16];

		{
			for(int i = 0; i < 256; i++) {
				heightmap[i] = -1;
				idmap[i] = -1;
			}
		}
		
		public HeightChunk(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}

	public void clear() {
		cache.clear();
		initBounds = false;
	}

	public static HeightMap getHeightMap(String worldName) {
		return getHeightMap(worldName, getFile(worldName));
	}

	public static HeightMap getHeightMap(String worldName, File file) {
		if(lastMap != null && lastMap.getWorldName().equals(worldName)) {
			lastMap.file = file;
			return lastMap;
		}
		HeightMap ret = null;
		if(heightMaps.containsKey(worldName)) {
			ret = heightMaps.get(worldName);
			ret.file = file;
		} else {
			HeightMap map = new HeightMap(worldName);
			map.file = file;
			heightMaps.put(worldName, map);
			if(file.exists()) {
				map.load();
			}
			ret = map;
		}
		lastMap = ret;
		return ret;
	}

	private HeightMap(String worldName) {
		this.worldName = worldName;
	}

	/*
	 * Format of the file is this:
	 * worldName:String
	 * minX:int
	 * maxX:int
	 * minZ:int
	 * maxZ:int
	 * height-map:short[], where map[0] is at minX, minZ and map[last] is at maxX, maxZ
	 */

	public void load() {
		synchronized (cache) {
			clear();
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(file));
				
				StringBuilder builder = new StringBuilder();
				short size = in.readShort();
				for (int i = 0; i < size; i++) {
					builder.append(in.readChar());
				}
				String name = builder.toString();
				if(!name.equals(getWorldName())) {
					System.out.println("World names do not match: "+name+" [file] != "+getWorldName()+" [game]. Compensating...");
					//TODO compensate
				}
				minX = in.readInt();
				maxX = in.readInt();
				minZ = in.readInt();
				maxZ = in.readInt();
				initBounds = true;
				int x = minX;
				int z = minZ;
				try {
					while(true) {
						x = in.readInt();
						z = in.readInt();
						HeightChunk chunk = new HeightChunk(x, z);
						for(int i = 0; i < 256; i++) {
							chunk.heightmap[i] = in.readShort();
							chunk.idmap[i] = in.readByte();
						}
						addChunk(chunk);
					}
				} catch (EOFException e) {}
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				cache.clear();
				initBounds = false;
				System.out.println("Error while loading persistent copy of the heightmap. Clearing the cache.");
			}
		}
	}

	private void addChunk(HeightChunk chunk) {
		synchronized (cache) {
			int x = chunk.x;
			int z = chunk.z;
			cache.put(x, z, chunk);
			if(!initBounds) {
				minX = x; 
				maxX = x;
				minZ = z;
				maxZ = z;
				initBounds = true;
			} else {
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minZ = Math.min(minZ, z);
				maxZ = Math.max(maxZ, z);
			}	
		}
	}

	public void save() {
		synchronized (cache) {
			try {
				DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
				
				String name = getWorldName();
				out.writeShort(name.length());
				for (int i = 0; i < name.length(); i++) {
					out.writeChar(name.charAt(i));
				}
				
				out.writeInt(minX);
				out.writeInt(maxX);
				out.writeInt(minZ);
				out.writeInt(maxZ);
				for(HeightChunk chunk : cache.valueCollection()) {
					if(chunk == null) {
						continue;
					} else {
						out.writeInt(chunk.x);
						out.writeInt(chunk.z);
						for(int i = 0; i < 256; i++) {
							out.writeShort(chunk.heightmap[i]);
							out.writeByte(chunk.idmap[i]);
						}
					}
				}
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static File getFile(String worldName) {
		File folder = new File(FileUtil.getSpoutcraftDirectory(), "heightmap");
		if(!folder.isDirectory()) {
			folder.delete();
		}
		if(!folder.exists()){
			folder.mkdirs();
		}
		return new File(FileUtil.getSpoutcraftDirectory(), "heightmap/"+worldName+".hma");
	}

//	public boolean hasHeight(int x, int z) {
//		synchronized (cache) {
//			return cache.containsKey(x, z);			
//		}
//	}

	public short getHeight(int x, int z) {
		int cX = (x >> 4);
		int cZ = (z >> 4);
		x &= 0xF;
		z &= 0xF;
		if(lastChunk != null && lastChunk.x == cX && lastChunk.z == cZ) {
			return lastChunk.heightmap[z << 4 | x];
		}
		synchronized (cache) {
			if(cache.containsKey(cX, cZ)) {
				lastChunk = cache.get(cX, cZ);
				return lastChunk.heightmap[z << 4 | x];
			} else {
				return -1;
			}			
		}
	}
	
	public byte getBlockId(int x, int z) {
		int cX = (x >> 4);
		int cZ = (z >> 4);
		x &= 0xF;
		z &= 0xF;
		if(lastChunk != null && lastChunk.x == cX && lastChunk.z == cZ) {
			return lastChunk.idmap[z << 4 | x];
		}
		synchronized (cache) {
			if(cache.containsKey(cX, cZ)) {
				lastChunk = cache.get(cX, cZ);
				return lastChunk.idmap[z << 4 | x];
			} else {
				return -1;
			}			
		}
	}
	
	public void setHighestBlock(int x, int z, short height, byte id) {
		int cX = (x >> 4);
		int cZ = (z >> 4);
		x &= 0xF;
		z &= 0xF;
		synchronized (cache) {
			if(!(lastChunk != null && lastChunk.x == cX && lastChunk.z == cZ)) {
				if(cache.containsKey(cX, cZ)) {
					lastChunk = cache.get(cX, cZ);
				} else {
					HeightChunk chunk = new HeightChunk(cX, cZ);
					chunk.heightmap[z << 4 | x] = height;
					chunk.idmap [z << 4 | x] = id;
					lastChunk = chunk;
					addChunk(chunk);
					return;
				}	
			}
			lastChunk.heightmap[z << 4 | x] = height;
			lastChunk.idmap[z << 4 | x] = id;
		}
	}

	public String getWorldName() {
		return worldName;
	}

	public int getMinX() {
		return minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinZ() {
		return minZ;
	}

	public int getMaxZ() {
		return maxZ;
	}
}
