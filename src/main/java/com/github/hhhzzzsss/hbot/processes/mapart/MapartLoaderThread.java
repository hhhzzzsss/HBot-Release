package com.github.hhhzzzsss.hbot.processes.mapart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.github.hhhzzzsss.hbot.util.DownloadUtils;
import com.github.hhhzzzsss.hbot.util.MapUtils;

import lombok.Getter;

public class MapartLoaderThread extends Thread {
	
	private URL url;
	@Getter private Exception exception;
	@Getter private BlockElevation[][] blocks = new BlockElevation[128][129];
	
	public MapartLoaderThread(String strUrl) throws IOException {
		url = new URL(strUrl);
		if (!url.getProtocol().startsWith("http")) {
			throw new IOException("Illegal protocol: must use http or https");
		}
	}
	
	public void run() {
		BufferedImage img;
		BufferedImage imgTransform;
		try {
			img = ImageIO.read(DownloadUtils.DownloadToOutputStream(url, 50*1024*1024));
		} catch (Exception e) {
			exception = e;
			return;
		}
		
		// Crop to make square
		int cropSize = Math.min(img.getWidth(), img.getHeight());
		imgTransform = Scalr.crop(img, (img.getWidth()-cropSize)/2, (img.getHeight()-cropSize)/2, cropSize, cropSize);
		img.flush();
		img = imgTransform;
		
		// Resize to 128x128
		imgTransform = Scalr.resize(img, 128);
		img.flush();
		img = imgTransform;
		
		// Fill transparency with white
		imgTransform = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgTransform.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 128, 128);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		img.flush();
		img = imgTransform;
		
		
		System.out.println(String.format("Resulting image size: %dpx x %dpx", img.getWidth(), img.getHeight()));
		
		// Noobline
		for (int x = 0; x < 128; x++) {
			blocks[x][0] = new BlockElevation("stone", 1); // The tone of the noobline doesn't matter
		}
		
		// Map colors
		for (int z = 0; z < 128; z++) for (int x = 0; x < 128; x++) {
			blocks[x][z+1] = getNearestMapColor(img.getRGB(x, z));
		}
		
		// Calcualate elevations
		/*
		 *tone=0 means the block is placed under the block north of it
		 *tone=1 means the block is placed at the same level as the block north of it
		 *tone=2 means the block is placed above the block north of it
		 */
		for (int x = 0; x < 128; x++) for (int z = 0; z < 128; z++) {
			int tone = blocks[x][z+1].tone;
			if (tone == 0) {
				blocks[x][z+1].elevation = 1;
			}
			else if (tone == 1) {
				blocks[x][z+1].elevation = blocks[x][z].elevation;
			}
			else {
				blocks[x][z+1].elevation = blocks[x][z].elevation + 1;
			}
			
			// If tone is 0 but the previous block is already at lowest elevation, shift everything up
			if (tone == 0 && blocks[x][z].elevation == 1) {
				for (int i = z; i >= 0; i--) {
					if (blocks[x][i].tone == 2) {
						if (blocks[x][i].elevation > blocks[x][i+1].elevation) blocks[x][i].elevation++;
						break;
					}
					else {
						blocks[x][i].elevation++;
					}
				}
			}
		}
	}
	
	private static BlockElevation getNearestMapColor(int rgb) {
		int r = (rgb & 0x00ff0000) >> 16;
		int g = (rgb & 0x0000ff00) >> 8;
		int b = (rgb & 0x000000ff);
		BlockElevation nearest = null;
		int minDistSq = Integer.MAX_VALUE;
		for (MapUtils.MapColor mc : MapUtils.getColors()) for (int tone=0; tone<3; tone++) {
			Color c = mc.getColors()[tone];
			int dr = r - c.getRed();
			int dg = g - c.getGreen();
			int db = b - c.getBlue();
			int distSq = dr*dr + dg*dg + db*db; 
			if (distSq < minDistSq) {
				nearest = new BlockElevation(mc.getBlock(), tone);
				minDistSq = distSq;
			}
		}
		
		return nearest;
	}
}
