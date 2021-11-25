package com.github.hhhzzzsss.hbot.processes.boxstructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.Logger;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

public class BoxStructure extends CoreProcess {
	public static enum Phase {
		WALLS,
		//WALLS_2,
		WINDOWS,
		HOLLOWING,
		HOLLOWING_2,
		LIGHTING,
		DECORATIONS,
		DECORATIONS_2,
		DONE,
	}
	public static enum NodeType {
		AIR,
		ROOM,
		HALL,
	}
	public static enum RoomType {
		NORMAL,
		ROOT,
		LIBRARY,
		VAULT,
	}
	public static enum Lighting {
		NONE,
		CORNER_ROD,
		CORNER_HANG,
		CORNER_POT,
		CORNER_LAMP,
		MID_EMBED,
		MID_ROD,
		MID_HANG,
		CENTER_ROD,
		CENTER_HANG,
		FLOOR_EMBED,
	}
	public static enum Decoration {
		NONE,
		CRAFT,
		BENCH,
		TABLE,
		SINK,
		POTTED_SHRUB,
		BIG_FLOWERPOT,
		SHELF,
		COUNTER,
		WINDOWSILL,
		FRIDGE,
		COOLER,
		BOOKSHELF,
		LECTERNS,
		GEMS,
		TREASURE_CHEST,
		IRON_DOORS,
	}
	
	public static enum CenterDecoration {
		NONE,
		LABEL,
	}
	
	public static final int[] WALLTOFACE = {0, 1, 4, 5};
	public static final int[] WALLTOAXIS = {0, 0, 2, 2};
	public static final String[] WALLDIR = {"west", "east", "north", "south"};
	public static final String[][] WALLDIRCONV = {{"west", "east", "north", "south"}, {"east", "west", "south", "north"}, {"north", "south", "east", "west"}, {"south", "north", "west", "east"}};
	public static final Integer[] WALLROTATION = {0, 8, 4, 12};
	public static final Pattern cardinalPattern = Pattern.compile("\\b(west|east|north|south)\\b");
	public static final Pattern rotationPattern = Pattern.compile("\\brotation=([0-9]+)\\b");
	//public static final String[][] DECIDX = new String[4][4] {{}}; // decoration index
	
	public final int MAX_EDGE_LENGTH = 5;
	
	public final HBot hbot;
	
	private int size; //length in nodes
	private Node[] grid;
	private Integer[] indexOrder;
	private int xdim;
	private int ydim;
	private int zdim;
	private int gridOriginX;
	private int gridOriginY;
	private int gridOriginZ;
	private int root;
	private int rootY;
	private Edge[] mst;
	
	Random rand;
	
	public Phase phase = Phase.WALLS;
	public int nodeIndex = 0;
	public String[] nodeCommands = new String[0];
	public int commandIndex = 0;
	
	public BoxStructure(HBot hbot, int x, int y, int z) {
		this(hbot, x, y, z, (long) (Math.random()*Integer.MAX_VALUE));
	}
	
	public BoxStructure(HBot hbot, int x, int y, int z, long seed) {
		this.hbot = hbot;
		
		size = 12;
		xdim = 2*size;
		ydim = 32;
		zdim = 2*size;
		
		gridOriginX = (Math.floorDiv(x, 16)*2-size+1)*8;
		gridOriginY = 0;
		gridOriginZ = (Math.floorDiv(z, 16)*2-size+1)*8;

		rootY = Math.floorDiv(y, 8);
		root = getIndex(size-1, rootY, size-1);
		
		grid = new Node[xdim*ydim*zdim];
		for (int i=0; i<xdim*ydim*zdim; i++) {
			grid[i] = new Node(i);
		}
		
		rand = new Random(seed);
		
		makeIndexOrder();
		populateGrid();
		findAdjacencies();
		findNeighbors();
		createMST(root);
		removeDisconnectedRooms();
		fillEdges();
		differentiateRooms1();
		addWindows();
		differentiateRooms2();
		assignNodeMaterials();
		addLighting();
		addRoomFeatures();
	}
	
	private void makeIndexOrder() {
		indexOrder = new Integer[xdim*ydim*zdim];
		for (int i=0; i<xdim*ydim*zdim; i++) {
			indexOrder[i] = i;
		}
		Arrays.sort(indexOrder, (Integer a, Integer b) -> {
			int[] aPos = decomposeIndex(a);
			
			int[] bPos = decomposeIndex(b);
			
			int da = Math.abs(2*aPos[0]+1-xdim) + Math.abs(2*aPos[1]+1-rootY*2) + Math.abs(2*aPos[2]+1-zdim);
			int db = Math.abs(2*bPos[0]+1-xdim) + Math.abs(2*bPos[1]+1-rootY*2) + Math.abs(2*bPos[2]+1-zdim);
			
			return da-db;
		});
	}
	
	private void populateGrid() {
		for (int i=0; i<xdim*ydim*zdim; i++) {
			if (rand.nextDouble() < 0.1) {
				grid[i].type = NodeType.ROOM;
			}
		}
		grid[root].type = NodeType.ROOM;
		grid[root+1].type = NodeType.ROOM;
		grid[root+xdim*ydim].type = NodeType.ROOM;
		grid[root+xdim*ydim+1].type = NodeType.ROOM;
	}
	
	private void findAdjacencies() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			int[] pos = decomposeIndex(node.index);
			if (pos[0] > 0) {
				node.adjacencies[0] = grid[getIndex(pos[0]-1, pos[1], pos[2])];
			}
			if (pos[0] < xdim-1) {
				node.adjacencies[1] = grid[getIndex(pos[0]+1, pos[1], pos[2])];
			}
			if (pos[1] > 0) {
				node.adjacencies[2] = grid[getIndex(pos[0], pos[1]-1, pos[2])];
			}
			if (pos[1] < ydim-1) {
				node.adjacencies[3] = grid[getIndex(pos[0], pos[1]+1, pos[2])];
			}
			if (pos[2] > 0) {
				node.adjacencies[4] = grid[getIndex(pos[0], pos[1], pos[2]-1)];
			}
			if (pos[2] < zdim-1) {
				node.adjacencies[5] = grid[getIndex(pos[0], pos[1], pos[2]+1)];
			}
		}
	}
	
	private void findNeighbors() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			node.edges = new ArrayList<>();
			int[] pos = decomposeIndex(node.index);
			int x, y, z;
			x = pos[0];
			y = pos[1];
			z = pos[2];
			x--;
			int bound = Math.max(pos[0]-MAX_EDGE_LENGTH, 0);
			while (x>=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 0, pos[0]-x));
					break;
				}
				x--;
			}
			x = pos[0];
			x++;
			bound = Math.min(pos[0]+MAX_EDGE_LENGTH, xdim-1);
			while (x<=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 0, x-pos[0]));
					break;
				}
				x++;
			}
			x = pos[0];
			y--;
			bound = Math.max(pos[1]-MAX_EDGE_LENGTH, 0);
			while (y>=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 1, (pos[1]-y)*(pos[1]-y)));
					break;
				}
				y--;
			}
			y = pos[1];
			y++;
			bound = Math.min(pos[1]+MAX_EDGE_LENGTH, ydim-1);
			while (y<=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 1, (y-pos[1])*(pos[1]-y)));
					break;
				}
				y++;
			}
			y = pos[1];
			z--;
			bound = Math.max(pos[2]-MAX_EDGE_LENGTH, 0);
			while (z>=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 2, pos[2]-z));
					break;
				}
				z--;
			}
			z = pos[2];
			z++;
			bound = Math.min(pos[2]+MAX_EDGE_LENGTH, zdim-1);
			while (z<=bound) {
				int idx = getIndex(x, y, z);
				if (grid[idx].type == NodeType.ROOM) {
					node.edges.add(new Edge(node.index, idx, 2, z-pos[2]));
					break;
				}
				z++;
			}
		}
	}
	
	private void createMST(int root) {
		mst = new Edge[xdim*ydim*zdim];
		PriorityQueue<Edge> pq = new PriorityQueue<>();
		int[] minCost = new int[xdim*ydim*zdim];
		for (int i=0; i<xdim*ydim*zdim; i++) {minCost[i] = Integer.MAX_VALUE;}
		pq.add(new Edge(-1, root, 0, 0));
		while (!pq.isEmpty()) {
			Node node = grid[pq.poll().b];
			grid[node.index].visited = true;
			for (Edge edge : node.edges) {
				if (!grid[edge.b].visited && edge.cost < minCost[edge.b]) {
					minCost[edge.b] = edge.cost;
					pq.add(edge);
					mst[edge.b] = edge;
				}
			}
		}
	}
	
	private void removeDisconnectedRooms() {
		for (int i=0; i<xdim*ydim*zdim; i++) {
			if (grid[i].type == NodeType.ROOM && !grid[i].visited) {
				grid[i].type = NodeType.AIR;
				grid[i].edges = null;
			}
		}
	}
	
	private void fillEdges() {
		for (Edge edge : mst) if (edge != null) {
			int[] pos1 = decomposeIndex(edge.a);
			int[] pos2 = decomposeIndex(edge.b);
			if (edge.axis == 0) {
				int xmin = Math.min(pos1[0], pos2[0]);
				int xmax = Math.max(pos1[0], pos2[0]);
				for (int x=xmin+1; x<xmax; x++) {
					int idx = getIndex(x, pos1[1], pos1[2]);
					grid[idx].type = NodeType.HALL;
					grid[idx].axes[0] = true;
				}
			}
			else if (edge.axis == 1) {
				int ymin = Math.min(pos1[1], pos2[1]);
				int ymax = Math.max(pos1[1], pos2[1]);
				for (int y=ymin+1; y<ymax; y++) {
					int idx = getIndex(pos1[0], y, pos1[2]);
					grid[idx].type = NodeType.HALL;
					grid[idx].axes[1] = true;
				}
			}
			else if (edge.axis == 2) {
				int zmin = Math.min(pos1[2], pos2[2]);
				int zmax = Math.max(pos1[2], pos2[2]);
				for (int z=zmin+1; z<zmax; z++) {
					int idx = getIndex(pos1[0], pos1[1], z);
					grid[idx].type = NodeType.HALL;
					grid[idx].axes[2] = true;
				}
			}
		}
	}
	
	private void differentiateRooms1() {
		grid[root].roomType = RoomType.ROOT;
		grid[root+1].roomType = RoomType.ROOT;
		grid[root+xdim*ydim].roomType = RoomType.ROOT;
		grid[root+xdim*ydim+1].roomType = RoomType.ROOT;
	}
	
	private void addWindows() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			boolean[] viableWalls = new boolean[6];
			int numViableWalls = 0;
			viableWalls[0] = node.adjacencies[0] == null || node.adjacencies[0].type == NodeType.AIR || (node.adjacencies[0].type == NodeType.HALL && !node.adjacencies[0].axes[0]);
			viableWalls[1] = node.adjacencies[1] == null || node.adjacencies[1].type == NodeType.AIR || (node.adjacencies[1].type == NodeType.HALL && !node.adjacencies[1].axes[0]);
			viableWalls[2] = node.adjacencies[2] == null || node.adjacencies[2].type == NodeType.AIR || (node.adjacencies[2].type == NodeType.HALL && !node.adjacencies[2].axes[1]);
			if (node.roomType != RoomType.ROOT) viableWalls[3] = node.adjacencies[3] == null || node.adjacencies[3].type == NodeType.AIR || (node.adjacencies[3].type == NodeType.HALL && !node.adjacencies[3].axes[1]);
			viableWalls[4] = node.adjacencies[4] == null || node.adjacencies[4].type == NodeType.AIR || (node.adjacencies[4].type == NodeType.HALL && !node.adjacencies[4].axes[2]);
			viableWalls[5] = node.adjacencies[5] == null || node.adjacencies[5].type == NodeType.AIR || (node.adjacencies[5].type == NodeType.HALL && !node.adjacencies[5].axes[2]);
			for (int i=0; i<6; i++) if(viableWalls[i]) numViableWalls++;
			for (int i=0; i<6; i++) if (viableWalls[i]) {
				if (rand.nextInt(numViableWalls+1) == 0) {
					node.windows[i] = true;
				}
			}
		}
	}
	
	private void differentiateRooms2() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			if (node.roomType == RoomType.ROOT) {
				continue;
			}
			
			ArrayList<RoomType> possibleRoomTypes = new ArrayList<>();
			int walls = 0;
			int nonWindowWalls = 0;
			int numAdjacentRooms = 0;
			int numAdjacentHalls = 0;
			for (int i=0; i<4; i++) {
				Node adj = node.adjacencies[WALLTOFACE[i]];
				if ( adj != null && (adj.type == NodeType.ROOM) ) {
					numAdjacentRooms++;
					continue;
				}
				else if ( adj != null && (adj.type == NodeType.HALL && adj.axes[WALLTOAXIS[i]]) ) {
					numAdjacentHalls++;
					continue;
				}
				walls++;
				if (!node.windows[WALLTOFACE[i]]) {
					nonWindowWalls++;
				}
			}
			boolean openFloor = node.adjacencies[2] != null && (node.adjacencies[2].type == NodeType.ROOM || (node.adjacencies[2].type == NodeType.HALL && node.adjacencies[2].axes[1]));
			boolean openCeiling = node.adjacencies[3] != null && (node.adjacencies[3].type == NodeType.ROOM || (node.adjacencies[3].type == NodeType.HALL && node.adjacencies[3].axes[1]));
			
			if (nonWindowWalls >= 1 && walls >= 2) {
				possibleRoomTypes.add(RoomType.LIBRARY);
			}
			
			if (numAdjacentRooms == 0 && walls >= 2 && !openFloor && !openCeiling) {
				possibleRoomTypes.add(RoomType.VAULT);
				possibleRoomTypes.add(RoomType.VAULT);
				possibleRoomTypes.add(RoomType.VAULT);
				for (int i=0; i<6; i++) {
					node.windows[i] = false;
				}
			}
			
			if (possibleRoomTypes.size() > 0 && rand.nextInt(4+possibleRoomTypes.size()) >= 4) {
				node.roomType = possibleRoomTypes.get(rand.nextInt(possibleRoomTypes.size()));
			}
		}
	}
	
	private void assignNodeMaterials() {
		for (Node node : grid) {
			if (node.type == NodeType.HALL) {
				node.material = "light_gray_concrete";
				node.windowMaterial = "light_gray_stained_glass";
			}
			else if (node.type == NodeType.ROOM) {
				if (node.roomType == RoomType.NORMAL) {
					node.material = "white_concrete";
					node.edgeMaterial = "gray_concrete";
					node.windowMaterial = "white_stained_glass";
				}
				else if (node.roomType == RoomType.ROOT) {
					node.material = "light_blue_concrete";
					node.edgeMaterial = "blue_concrete";
					node.windowMaterial = "blue_stained_glass";
				}
				else if (node.roomType == RoomType.LIBRARY) {
					node.material = "oak_planks";
					node.edgeMaterial = "stone_bricks";
					node.windowMaterial = "glass";
					node.lightMaterial = "glowstone";
				}
				else if (node.roomType == RoomType.VAULT) {
					node.material = "iron_block";
					node.edgeMaterial = "gold_block";
					node.lightMaterial = "beacon";
				}
			}
		}
	}
	
	private void addLighting() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			if (node.roomType == RoomType.ROOT) {
				node.lighting = Lighting.MID_EMBED;
				continue;
			}
			
			ArrayList<Lighting> possibleLighting = new ArrayList<>();
			boolean cornercond4 = node.adjacencies[4] == null || node.adjacencies[4].type != NodeType.ROOM;
			boolean cornercond5 = node.adjacencies[5] == null || node.adjacencies[5].type != NodeType.ROOM;
			boolean cornercond0 = node.adjacencies[0] == null || node.adjacencies[0].type != NodeType.ROOM;
			boolean cornercond1 = node.adjacencies[1] == null || node.adjacencies[1].type != NodeType.ROOM;
			if (cornercond4 && cornercond5 && cornercond0 && cornercond1) {
				possibleLighting.add(Lighting.CORNER_ROD);
				possibleLighting.add(Lighting.CORNER_HANG);
				possibleLighting.add(Lighting.CORNER_POT);
				possibleLighting.add(Lighting.CORNER_LAMP);
			}
			boolean midcond = !node.windows[3];
			if (midcond) {
				possibleLighting.add(Lighting.MID_EMBED);
				possibleLighting.add(Lighting.MID_ROD);
				possibleLighting.add(Lighting.MID_HANG);
			}
			boolean centercond = node.adjacencies[3] == null || node.adjacencies[3].type == NodeType.AIR || (node.adjacencies[3].type == NodeType.HALL && !node.adjacencies[3].axes[1]);
			if (centercond && midcond) {
				possibleLighting.add(Lighting.CENTER_ROD);
				possibleLighting.add(Lighting.CENTER_HANG);
			}

			if (possibleLighting.size() == 0) {
				node.windows[2] = false;
				node.lighting = Lighting.FLOOR_EMBED;
			}
			else {
				node.lighting = possibleLighting.get(rand.nextInt(possibleLighting.size()));
			}
		}
	}
	
	private void addRoomFeatures() {
		for (Node node : grid) if (node.type == NodeType.ROOM) {
			if (node.roomType == RoomType.ROOT) {
				addFeatures_ROOT(node);
			}
			else if (node.roomType == RoomType.NORMAL) {
				addFeatures_NORMAL(node);
			}
			else if (node.roomType == RoomType.LIBRARY) {
				addFeatures_LIBRARY(node);
			}
			else if (node.roomType == RoomType.VAULT) {
				addFeatures_VAULT(node);
			}
		}
	}
	
	private void addFeatures_ROOT(Node node) {
		node.centerDecoration = CenterDecoration.LABEL;
	}
	
	private void addFeatures_NORMAL(Node node) {
		for (int i=0; i<4; i++) {
			ArrayList<Decoration> possibleDecorations = new ArrayList<>();
			
			Node adj = node.adjacencies[WALLTOFACE[i]];
			if ( adj != null && (adj.type == NodeType.ROOM || (adj.type == NodeType.HALL && adj.axes[WALLTOAXIS[i]])) ) continue;

			possibleDecorations.add(Decoration.NONE);
			possibleDecorations.add(Decoration.CRAFT);
			possibleDecorations.add(Decoration.BENCH);
			possibleDecorations.add(Decoration.TABLE);
			possibleDecorations.add(Decoration.SINK);
			possibleDecorations.add(Decoration.POTTED_SHRUB);
			possibleDecorations.add(Decoration.BIG_FLOWERPOT);
			possibleDecorations.add(Decoration.SHELF);
			possibleDecorations.add(Decoration.COUNTER);
			if (node.windows[WALLTOFACE[i]]) {
				possibleDecorations.add(Decoration.WINDOWSILL);
				possibleDecorations.add(Decoration.WINDOWSILL);
				possibleDecorations.add(Decoration.WINDOWSILL);
				possibleDecorations.add(Decoration.BIG_FLOWERPOT);
			}
			else {
				possibleDecorations.add(Decoration.FRIDGE);
				possibleDecorations.add(Decoration.COOLER);
			}
			node.decorations[i] = possibleDecorations.get(rand.nextInt(possibleDecorations.size()));
		}
	}
	
	private void addFeatures_LIBRARY(Node node) {
		ArrayList<Integer> possibleWalls = new ArrayList<>();
		for (int i=0; i<4; i++) {
			Node adj = node.adjacencies[WALLTOFACE[i]];
			if ( adj != null && (adj.type == NodeType.ROOM || (adj.type == NodeType.HALL && adj.axes[WALLTOAXIS[i]])) ) continue;
			
			node.decorations[i] = Decoration.LECTERNS;
			if (!node.windows[WALLTOFACE[i]]) possibleWalls.add(i);
		}
		
		int bookshelfWall = possibleWalls.get(rand.nextInt(possibleWalls.size()));
		node.decorations[bookshelfWall] = Decoration.BOOKSHELF;
	}
	
	private void addFeatures_VAULT(Node node) {
		ArrayList<Integer> possibleWalls = new ArrayList<>();
		for (int i=0; i<4; i++) {
			Node adj = node.adjacencies[WALLTOFACE[i]];
			if ( adj != null && adj.type == NodeType.HALL && adj.axes[WALLTOAXIS[i]] ) {
				node.decorations[i] = Decoration.IRON_DOORS;
			}
			else {
				node.decorations[i] = Decoration.GEMS;
				possibleWalls.add(i);
			}
		}
		int chestWall = possibleWalls.get(rand.nextInt(possibleWalls.size()));
		node.decorations[chestWall] = Decoration.TREASURE_CHEST;
	}
	
	private int getIndex(int x, int y, int z) {
		return z*xdim*ydim + y*xdim + x;
	}
	
	private int[] decomposeIndex(int index) {
		int x = index % xdim;
		index /= xdim;
		int y = index % ydim;
		index /= ydim;
		int z = index;
		return new int[] {x, y, z};
	}

	@Override
	public void onSequence() {
		while (commandIndex == nodeCommands.length && phase != Phase.DONE) {
			if (nodeIndex == xdim*ydim*zdim) {
				Phase prevPhase = phase;
				phase = Phase.values()[phase.ordinal() + 1];
				nodeIndex = 0;
				hbot.sendChat("&7Finished phase &3" + prevPhase + " &7of Box Structure");
				if (phase == Phase.DONE) {
					hbot.sendChat("&7Finished building Box Structure");
				}
			}
			nodeCommands = grid[indexOrder[nodeIndex++]].getCommands(phase);
			commandIndex = 0;
		}
		if (phase != Phase.DONE) {
			hbot.getCommandCore().run(nodeCommands[commandIndex++]);
		}
		else {
			this.stop();
		}
	}
	
	private class Node {
		public int index;
		public NodeType type;
		public RoomType roomType = RoomType.NORMAL;
		public int originX;
		public int originY;
		public int originZ;
		public Node[] adjacencies = new Node[6];
		public ArrayList<Edge> edges;
		public boolean visited = false;
		public boolean[] axes = new boolean[3];
		public boolean[] windows = new boolean[6];
		public Lighting lighting = Lighting.NONE;
		public Decoration[] decorations = {Decoration.NONE, Decoration.NONE, Decoration.NONE, Decoration.NONE};
		public CenterDecoration centerDecoration;
		
		public String material;
		public String edgeMaterial;
		public String windowMaterial;
		public String lightMaterial = "sea_lantern";
		
		public Node(int index) {
			this.index = index;
			this.type = NodeType.AIR;
			
			int[] pos = decomposeIndex(index);
			originX = gridOriginX + pos[0]*8;
			originY = gridOriginY + pos[1]*8;
			originZ = gridOriginZ + pos[2]*8;
		}
		
		public String[] getCommands(Phase phase) {
			if (phase == Phase.WALLS) {
				return getCommands_WALLS();
			}
			/*else if (phase == Phase.WALLS_2) {
				return getCommands_WALLS_2();
			}*/
			else if (phase == Phase.WINDOWS) {
				return getCommands_WINDOWS();
			}
			else if (phase == Phase.HOLLOWING) {
				return getCommands_HOLLOWING();
			}
			else if (phase == Phase.HOLLOWING_2) {
				return getCommands_HOLLOWING_2();
			}
			else if (phase == Phase.LIGHTING) {
				return getCommands_LIGHTING();
			}
			else if (phase == Phase.DECORATIONS) {
				return getCommands_DECORATIONS();
			}
			else if (phase == Phase.DECORATIONS_2) {
				return getCommands_DECORATIONS_2();
			}
			else {
				return new String[0];
			}
		}
		
		private String[] getCommands_WALLS() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				//commands.add(fillString(0, 0, 0, 7, 7, 7, edgeMaterial, "outline"));

				commands.add(fillString(0, 0, 0, 0, 7, 0, edgeMaterial));
				commands.add(fillString(0, 0, 7, 0, 7, 7, edgeMaterial));
				commands.add(fillString(7, 0, 0, 7, 7, 0, edgeMaterial));
				commands.add(fillString(7, 0, 7, 7, 7, 7, edgeMaterial));
				commands.add(fillString(1, 0, 0, 6, 0, 0, edgeMaterial));
				commands.add(fillString(1, 0, 7, 6, 0, 7, edgeMaterial));
				commands.add(fillString(0, 0, 1, 0, 0, 6, edgeMaterial));
				commands.add(fillString(7, 0, 1, 7, 0, 6, edgeMaterial));
				commands.add(fillString(1, 7, 0, 6, 7, 0, edgeMaterial));
				commands.add(fillString(1, 7, 7, 6, 7, 7, edgeMaterial));
				commands.add(fillString(0, 7, 1, 0, 7, 6, edgeMaterial));
				commands.add(fillString(7, 7, 1, 7, 7, 6, edgeMaterial));
				commands.add(fillString(1, 0, 1, 6, 0, 6, material));
				commands.add(fillString(1, 7, 1, 6, 7, 6, material));
				commands.add(fillString(1, 1, 0, 6, 6, 0, material));
				commands.add(fillString(1, 1, 7, 6, 6, 7, material));
				commands.add(fillString(0, 1, 1, 0, 6, 6, material));
				commands.add(fillString(7, 1, 1, 7, 6, 6, material));
			}
			else if (type == NodeType.HALL) {
				if (axes[0]) {
					commands.add(fillString(0, 0, 2, 7, 4, 5, material, "outline"));
				}
				if (axes[1]) {
					commands.add(fillString(2, 0, 2, 5, 7, 5, material, "outline"));
					if (axes[0] || axes[2]) commands.add(fillString(1, 0, 1, 6, 4, 6, material, "outline"));
				}
				if (axes[2]) {
					commands.add(fillString(2, 0, 0, 5, 4, 7, material, "outline"));
				}
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_WALLS_2() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				/*boolean cond1 = adjacencies[1] != null && adjacencies[1].type == NodeType.ROOM;
				boolean cond5 = adjacencies[5] != null && adjacencies[5].type == NodeType.ROOM;
				boolean cond15 = cond1 && cond5 && adjacencies[1].adjacencies[5].type == NodeType.ROOM;
				if (cond1) {
					commands.add(fillString(7, 0, 1, 8, 0, 6, material));
					commands.add(fillString(7, 7, 1, 8, 7, 6, material));
					commands.add(fillString(7, 1, 0, 8, 6, 0, material));
					commands.add(fillString(7, 1, 7, 8, 6, 7, material));
				}
				if (cond5) {
					commands.add(fillString(1, 0, 7, 6, 0, 8, material));
					commands.add(fillString(1, 7, 7, 6, 7, 8, material));
					commands.add(fillString(0, 1, 7, 0, 6, 8, material));
					commands.add(fillString(7, 1, 7, 7, 6, 8, material));
				}
				if (cond15) commands.add(fillString(7, 0, 7, 8, 7, 8, material));*/
				commands.add(fillString(1, 0, 1, 6, 0, 6, material));
				commands.add(fillString(1, 7, 1, 6, 7, 6, material));
				commands.add(fillString(1, 1, 0, 6, 6, 0, material));
				commands.add(fillString(1, 1, 7, 6, 6, 7, material));
				commands.add(fillString(0, 1, 1, 0, 6, 6, material));
				commands.add(fillString(7, 1, 1, 7, 6, 6, material));
			}
			else if (type == NodeType.HALL) {
				return new String[0];
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_WINDOWS() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				if (windows[0]) commands.add(fillString(0, 2, 2, 0, 5, 5, windowMaterial));
				if (windows[1]) commands.add(fillString(7, 2, 2, 7, 5, 5, windowMaterial));
				if (windows[2]) commands.add(fillString(2, 0, 2, 5, 0, 5, windowMaterial));
				if (windows[3]) commands.add(fillString(2, 7, 2, 5, 7, 5, windowMaterial));
				if (windows[4]) commands.add(fillString(2, 2, 0, 5, 5, 0, windowMaterial));
				if (windows[5]) commands.add(fillString(2, 2, 7, 5, 5, 7, windowMaterial));
			}
			else if (type == NodeType.HALL) {
				if (axes[0]) {
					commands.add(fillString(0, 2, 2, 7, 2, 2, windowMaterial));
					commands.add(fillString(0, 2, 5, 7, 2, 5, windowMaterial));
				}
				if (axes[1]) {
					if (axes[0] || axes[2]) commands.add(fillString(1, 2, 1, 6, 2, 6, windowMaterial));
					else commands.add(fillString(2, 2, 2, 5, 2, 5, "sea_lantern"));
				}
				if (axes[2]) {
					commands.add(fillString(2, 2, 0, 2, 2, 7, windowMaterial));
					commands.add(fillString(5, 2, 0, 5, 2, 7, windowMaterial));
				}
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_HOLLOWING() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				commands.add(fillString(1, 1, 1, 6, 6, 6, "air"));
				if (adjacencies[3] != null && adjacencies[3].type == NodeType.ROOM) {
					commands.add(fillString(3, 7, 3, 4, 8, 4, "air"));
				}
				
				if (adjacencies[1] != null && adjacencies[5] != null && adjacencies[1].type == NodeType.ROOM && adjacencies[5].type == NodeType.ROOM && adjacencies[1].adjacencies[5].type == NodeType.ROOM) {
					commands.add(fillString(7, 1, 7, 8, 6, 8, "air"));
				}
			}
			else if (type == NodeType.HALL) {
				if (axes[0]) {
					commands.add(fillString(-1, 1, 3, 8, 3, 4, "air"));
				}
				if (axes[1]) {
					if (axes[0] || axes[2]) commands.add(fillString(2, 1, 2, 5, 3, 5, "air"));
					commands.add(fillString(3, -1, 3, 4, 8, 4, "air"));
				}
				if (axes[2]) {
					commands.add(fillString(3, 1, -1, 4, 3, 8, "air"));
				}
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_HOLLOWING_2() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				boolean cond4 = adjacencies[4] != null && adjacencies[4].type == NodeType.ROOM;
				boolean cond5 = adjacencies[5] != null && adjacencies[5].type == NodeType.ROOM;
				boolean cond0 = adjacencies[0] != null && adjacencies[0].type == NodeType.ROOM;
				boolean cond1 = adjacencies[1] != null && adjacencies[1].type == NodeType.ROOM;
				if (cond4) commands.add(fillString(1, 1, 0, 6, 6, 0, "air"));
				if (cond5) commands.add(fillString(1, 1, 7, 6, 6, 7, "air"));
				if (cond0) commands.add(fillString(0, 1, 1, 0, 6, 6, "air"));
				if (cond1) commands.add(fillString(7, 1, 1, 7, 6, 6, "air"));
			}
			else if (type == NodeType.HALL) {
				return new String[0];
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_LIGHTING() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				if (lighting == Lighting.CORNER_ROD) {
					commands.add(setblockString(1, 6, 1, "end_rod[facing=down]"));
					commands.add(setblockString(1, 6, 6, "end_rod[facing=down]"));
					commands.add(setblockString(6, 6, 1, "end_rod[facing=down]"));
					commands.add(setblockString(6, 6, 6, "end_rod[facing=down]"));
				}
				else if (lighting == Lighting.CORNER_HANG) {
					commands.add(setblockString(1, 6, 1, "end_rod[facing=down]"));
					commands.add(setblockString(1, 6, 6, "end_rod[facing=down]"));
					commands.add(setblockString(6, 6, 1, "end_rod[facing=down]"));
					commands.add(setblockString(6, 6, 6, "end_rod[facing=down]"));
					commands.add(setblockString(1, 5, 1, lightMaterial));
					commands.add(setblockString(1, 5, 6, lightMaterial));
					commands.add(setblockString(6, 5, 1, lightMaterial));
					commands.add(setblockString(6, 5, 6, lightMaterial));
				}
				else if (lighting == Lighting.CORNER_POT) {
					commands.add(setblockString(1, 1, 1, "cauldron"));
					commands.add(setblockString(1, 1, 6, "cauldron"));
					commands.add(setblockString(6, 1, 1, "cauldron"));
					commands.add(setblockString(6, 1, 6, "cauldron"));
					commands.add(setblockString(1, 2, 1, lightMaterial));
					commands.add(setblockString(1, 2, 6, lightMaterial));
					commands.add(setblockString(6, 2, 1, lightMaterial));
					commands.add(setblockString(6, 2, 6, lightMaterial));
				}
				else if (lighting == Lighting.CORNER_LAMP) {
					commands.add(setblockString(1, 1, 1, "oak_planks"));
					commands.add(setblockString(1, 1, 6, "oak_planks"));
					commands.add(setblockString(6, 1, 1, "oak_planks"));
					commands.add(setblockString(6, 1, 6, "oak_planks"));
					commands.add(setblockString(1, 2, 1, "end_rod"));
					commands.add(setblockString(1, 2, 6, "end_rod"));
					commands.add(setblockString(6, 2, 1, "end_rod"));
					commands.add(setblockString(6, 2, 6, "end_rod"));
					commands.add(setblockString(1, 3, 1, lightMaterial));
					commands.add(setblockString(1, 3, 6, lightMaterial));
					commands.add(setblockString(6, 3, 1, lightMaterial));
					commands.add(setblockString(6, 3, 6, lightMaterial));
				}
				else if (lighting == Lighting.MID_EMBED) {
					commands.add(setblockString(2, 7, 2, lightMaterial));
					commands.add(setblockString(2, 7, 5, lightMaterial));
					commands.add(setblockString(5, 7, 2, lightMaterial));
					commands.add(setblockString(5, 7, 5, lightMaterial));
				}
				else if (lighting == Lighting.MID_ROD) {
					commands.add(setblockString(2, 6, 2, "end_rod[facing=down]"));
					commands.add(setblockString(2, 6, 5, "end_rod[facing=down]"));
					commands.add(setblockString(5, 6, 2, "end_rod[facing=down]"));
					commands.add(setblockString(5, 6, 5, "end_rod[facing=down]"));
				}
				else if (lighting == Lighting.MID_HANG) {
					commands.add(setblockString(2, 6, 2, "end_rod[facing=down]"));
					commands.add(setblockString(2, 6, 5, "end_rod[facing=down]"));
					commands.add(setblockString(5, 6, 2, "end_rod[facing=down]"));
					commands.add(setblockString(5, 6, 5, "end_rod[facing=down]"));
					commands.add(setblockString(2, 5, 2, lightMaterial));
					commands.add(setblockString(2, 5, 5, lightMaterial));
					commands.add(setblockString(5, 5, 2, lightMaterial));
					commands.add(setblockString(5, 5, 5, lightMaterial));
				}
				else if (lighting == Lighting.CENTER_ROD) {
					commands.add(fillString(3, 6, 3, 4, 6, 4, "end_rod[facing=down]"));
				}
				else if (lighting == Lighting.CENTER_HANG) {
					commands.add(fillString(3, 6, 3, 4, 6, 4, "end_rod[facing=down]"));
					commands.add(fillString(3, 5, 3, 4, 5, 4, lightMaterial));
				}
				else if (lighting == Lighting.FLOOR_EMBED) {
					commands.add(setblockString(2, 0, 2, lightMaterial));
					commands.add(setblockString(2, 0, 5, lightMaterial));
					commands.add(setblockString(5, 0, 2, lightMaterial));
					commands.add(setblockString(5, 0, 5, lightMaterial));
				}
			}
			else if (type == NodeType.HALL) {
				if (axes[0]) {
					commands.add(fillString(0, 4, 3, 0, 4, 4, "sea_lantern"));
					commands.add(fillString(7, 4, 3, 7, 4, 4, "sea_lantern"));
				}
				if (axes[2]) {
					commands.add(fillString(3, 4, 0, 4, 4, 0, "sea_lantern"));
					commands.add(fillString(3, 4, 7, 4, 4, 7, "sea_lantern"));
				}
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_DECORATIONS() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				if (centerDecoration == CenterDecoration.LABEL) {
					addLABEL(commands);
				}
				
				for (int i=0; i<4; i++) {
					if (decorations[i] == Decoration.CRAFT) {
						addCRAFT(commands, i);
					}
					else if (decorations[i] == Decoration.BENCH) {
						addBENCH(commands, i);
					}
					else if (decorations[i] == Decoration.TABLE) {
						addTABLE(commands, i);
					}
					else if (decorations[i] == Decoration.SINK) {
						addSINK(commands, i);
					}
					else if (decorations[i] == Decoration.POTTED_SHRUB) {
						addPOTTED_SHRUB(commands, i);
					}
					else if (decorations[i] == Decoration.BIG_FLOWERPOT) {
						addBIG_FLOWERPOT(commands, i);
					}
					else if (decorations[i] == Decoration.SHELF) {
						addSHELF(commands, i);
					}
					else if (decorations[i] == Decoration.COUNTER) {
						addCOUNTER(commands, i);
					}
					else if (decorations[i] == Decoration.WINDOWSILL) {
						addWINDOWSILL(commands, i);
					}
					else if (decorations[i] == Decoration.FRIDGE) {
						addFRIDGE(commands, i);
					}
					else if (decorations[i] == Decoration.COOLER) {
						addCOOLER(commands, i);
					}
					else if (decorations[i] == Decoration.BOOKSHELF) {
						addBOOKSHELF(commands, i);
					}
					else if (decorations[i] == Decoration.LECTERNS) {
						addLECTERNS(commands, i);
					}
					else if (decorations[i] == Decoration.GEMS) {
						addGEMS(commands, i);
					}
					else if (decorations[i] == Decoration.TREASURE_CHEST) {
						addTREASURE_CHEST(commands, i);
					}
					else if (decorations[i] == Decoration.IRON_DOORS) {
						addIRON_DOORS(commands, i);
					}
				}
			}
			else if (type == NodeType.HALL) {
				return new String[0];
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}
		
		private String[] getCommands_DECORATIONS_2() {
			ArrayList<String> commands = new ArrayList<>();
			
			if (type == NodeType.AIR) {
				return new String[0];
			}
			else if (type == NodeType.ROOM) {
				for (int i=0; i<4; i++) {
					if (decorations[i] == Decoration.BIG_FLOWERPOT) {
						addBIG_FLOWERPOT_2(commands, i);
					}
					else if (decorations[i] == Decoration.FRIDGE) {
						addFRIDGE_2(commands, i);
					}
					else if (decorations[i] == Decoration.COOLER) {
						addCOOLER_2(commands, i);
					}
					else if (decorations[i] == Decoration.IRON_DOORS) {
						addIRON_DOORS_2(commands, i);
					}
				}
			}
			else if (type == NodeType.HALL) {
				return new String[0];
			}
			
			String[] arr = new String[commands.size()];
			commands.toArray(arr);
			return arr;
		}

		private void addLABEL(ArrayList<String> commands) {
			String labelSignBlock = "oak_sign[rotation=14]" + getSignData("§7§lBox §3§lStructure", "", "made by", "§b§lhhhzzzsss");
			commands.add(setblockStringR(rand.nextInt(4), 2, 1, 2, labelSignBlock));
		}
		
		private void addCRAFT(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 1, 4, "crafting_table"));
			commands.add(setblockStringR(wall, 1, 1, 3, "furnace[facing=east]"));
		}
		
		private void addBENCH(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 1, 5, "oak_trapdoor[facing=south,open=true]"));
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "oak_stairs[facing=west]"));
			commands.add(setblockStringR(wall, 1, 1, 2, "oak_trapdoor[facing=north,open=true]"));
		}
		
		private void addTABLE(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 1, 5, "oak_stairs[facing=south]"));
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "spruce_trapdoor[facing=east,half=top]"));
			commands.add(setblockStringR(wall, 1, 1, 2, "oak_stairs[facing=north]"));
			if (rand.nextInt(3) == 0) commands.add(setblockStringR(wall, 1, 2, 4, randomTableItem()));
			if (rand.nextInt(3) == 0) commands.add(setblockStringR(wall, 1, 2, 3, randomTableItem()));
		}
		
		private void addSINK(ArrayList<String> commands, int wall) {
			int r = rand.nextInt(3);
			if (r==0) {
				commands.add(fillStringR(wall, 1, 2, 4, 1, 2, 3, "tripwire_hook[facing=east]"));
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "cauldron[level=3]"));
			}
			else if (r==1) {
				commands.add(fillStringR(wall, 1, 2, 4, 1, 2, 3, "tripwire_hook[facing=east]"));
				commands.add(fillStringR(wall, 2, 1, 4, 2, 1, 3, "spruce_trapdoor[facing=east,open=true]"));
				commands.add(setblockStringR(wall, 1, 1, 5, "spruce_trapdoor[facing=south,open=true]"));
				commands.add(setblockStringR(wall, 1, 1, 2, "spruce_trapdoor[facing=north,open=true]"));
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "spruce_trapdoor[facing=east,half=top,open=true,waterlogged=true]"));
			}
			else {
				commands.add(fillStringR(wall, 1, 2, 4, 1, 2, 3, "tripwire_hook[facing=east]"));
				commands.add(fillStringR(wall, 2, 1, 4, 2, 1, 3, "spruce_trapdoor[facing=east,open=true]"));
				commands.add(setblockStringR(wall, 1, 1, 5, "spruce_trapdoor[facing=south,open=true]"));
				commands.add(setblockStringR(wall, 1, 1, 2, "spruce_trapdoor[facing=north,open=true]"));
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "smooth_quartz_slab[waterlogged=true]"));
			}
		}
		
		private void addPOTTED_SHRUB(ArrayList<String> commands, int wall) {
			int r = rand.nextInt(3);
			if (r==0) {
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "cauldron"));
			}
			else if (r==1) {
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "potted_dead_bush"));
			}
			else {
				commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "potted_bamboo"));
			}
			commands.add(fillStringR(wall, 1, 2, 4, 1, 2, 3, randomLeaves()+"[persistent=true]"));
		}
		
		private void addBIG_FLOWERPOT(ArrayList<String> commands, int wall) {
			String trapdoor = randomTrapdoor();
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "dirt"));
			commands.add(fillStringR(wall, 2, 1, 4, 2, 1, 3, trapdoor+"[facing=east,open=true]"));
			commands.add(setblockStringR(wall, 1, 1, 5, trapdoor+"[facing=south,open=true]"));
			commands.add(setblockStringR(wall, 1, 1, 2, trapdoor+"[facing=north,open=true]"));
		}
		
		private void addBIG_FLOWERPOT_2(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 2, 4, randomFlower()));
			commands.add(setblockStringR(wall, 1, 2, 3, randomFlower()));
		}
		
		private void addSHELF(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, randomTrapdoor()+"[facing=east,half=top]"));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 2, 4, randomTableItem()));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 2, 3, randomTableItem()));
		}
		
		private void addCOUNTER(ArrayList<String> commands, int wall) {
			String block = BlockIndex.COUNTER_BLOCKS[rand.nextInt(BlockIndex.COUNTER_BLOCKS.length)];
			commands.add(fillStringR(wall, 1, 1, 5, 1, 1, 2, block));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 5, randomTableItem()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 4, randomTableItem()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 3, randomTableItem()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 2, randomTableItem()));
		}
		
		private void addWINDOWSILL(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, 1, 1, 5, 1, 1, 2, randomWood()+"_stairs[facing=west,half=top]"));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 5, randomFlowerpot()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 4, randomFlowerpot()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 3, randomFlowerpot()));
			if (rand.nextInt(2) == 0) commands.add(setblockStringR(wall, 1, 2, 2, randomFlowerpot()));
		}
		
		private void addFRIDGE(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, 1, 1, 4, 1, 2, 3, "smooth_quartz"));
			commands.add(fillStringR(wall, 1, 3, 4, 1, 3, 3, "smooth_quartz_slab"));
			commands.add(setblockStringR(wall, 2, 1, 3, "iron_door[facing=east,half=lower]"));
			commands.add(setblockStringR(wall, 2, 2, 3, "iron_door[facing=east,half=upper]"));
		}
		
		private void addFRIDGE_2(ArrayList<String> commands, int wall) {
			ArrayList<ContainerItem> items = new ArrayList<>();
			for (int i=0; i<9; i++) {
				Item item = BlockIndex.FRIDGE_ITEMS[rand.nextInt(BlockIndex.FRIDGE_ITEMS.length)];
				items.add(new ContainerItem(i, item, 64));
			}
			commands.add(setblockStringR(wall, 1, 2, 3, "dropper[facing=east]"+getContainerData(items)));
			commands.add(setblockStringR(wall, 2, 2, 4, "stone_button[facing=east]"));
		}
		
		private void addCOOLER(ArrayList<String> commands, int wall) {
			ArrayList<ContainerItem> items = new ArrayList<>();
			items.add(new ContainerItem(0, "potion", 64, "{Potion:\"minecraft:water\"}"));
			commands.add(fillStringR(wall, 1, 2, 4, 1, 2, 3, "light_blue_stained_glass"));
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "dropper[facing=east]"+getContainerData(items)));
		}
		
		private void addCOOLER_2(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, 2, 2, 4, 2, 2, 3, "stone_button[facing=east]"));
		}
		
		private void addBOOKSHELF(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, 1, 1, 5, 1, 3, 2, "bookshelf"));
		}
		
		private void addLECTERNS(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 1, 4, randomLogExcerpt()));
			commands.add(setblockStringR(wall, 1, 1, 3, randomLogExcerpt()));
		}
		
		private void addGEMS(ArrayList<String> commands, int wall) {
			String gem = randomGem();
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 2, 4, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 2, 3, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 2, 1, 4, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 2, 1, 3, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 1, 5, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
			if (rand.nextInt(3) != 0) commands.add(setblockStringR(wall, 1, 1, 2, rand.nextInt(2) == 0 ? randomVaultHead() : gem));
		}
		
		private void addTREASURE_CHEST(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 1, 1, 4, "chest[facing=east,type=right]" + getTreasureChestData()));
			commands.add(setblockStringR(wall, 1, 1, 3, "chest[facing=east,type=left]" + getTreasureChestData()));
		}
		
		private void addIRON_DOORS(ArrayList<String> commands, int wall) {
			commands.add(setblockStringR(wall, 0, 1, 4, "iron_door[facing=east,half=lower,hinge=right]"));
			commands.add(setblockStringR(wall, 0, 1, 3, "iron_door[facing=east,half=lower,hinge=left]"));
			commands.add(setblockStringR(wall, 0, 2, 4, "iron_door[facing=east,half=upper,hinge=right]"));
			commands.add(setblockStringR(wall, 0, 2, 3, "iron_door[facing=east,half=upper,hinge=left]"));
			commands.add(fillStringR(wall, 0, 3, 4, 0, 3, 3, "iron_block"));
			commands.add(fillStringR(wall, 1, 1, 4, 1, 1, 3, "heavy_weighted_pressure_plate"));
		}
		
		private void addIRON_DOORS_2(ArrayList<String> commands, int wall) {
			commands.add(fillStringR(wall, -1, 3, 4, -1, 3, 3, "lever[face=wall,facing=west]"));
		}
		
		private String setblockString(int x, int y, int z, String block) {
			if (originY+y>255) y=255-originY;
			if (originY+y<0) y=0-originY;
			return String.format("/setblock %d %d %d %s replace", originX+x, originY+y, originZ+z, block);
		}
		
		private String setblockStringR(int rot, int x, int y, int z, String block) {
			if (rot == 1) {
				x = 7-x;
				z = 7-z;
			}
			else if (rot == 2) {
				int t = x;
				x = 7-z;
				z = t;
			}
			else if (rot==3) {
				int t = x;
				x = z;
				z = 7-t;
			}
			block = rotateBlock(block, rot);
			
			if (originY+y>255) y=255-originY;
			if (originY+y<0) y=0-originY;
			return String.format("/setblock %d %d %d %s replace", originX+x, originY+y, originZ+z, block);
		}
		
		private String fillString(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
			if (originY+y1>255) y1=255-originY;
			if (originY+y2>255) y2=255-originY;
			if (originY+y1<0) y1=0-originY;
			if (originY+y2<0) y2=0-originY;
			return String.format("/fill %d %d %d %d %d %d %s replace", originX+x1, originY+y1, originZ+z1, originX+x2, originY+y2, originZ+z2, block);
		}
		
		private String fillStringR(int rot, int x1, int y1, int z1, int x2, int y2, int z2, String block) {
			if (rot == 1) {
				x1 = 7-x1;
				z1 = 7-z1;
				x2 = 7-x2;
				z2 = 7-z2;
			}
			else if (rot == 2) {
				int t = x1;
				x1 = 7-z1;
				z1 = t;
				t = x2;
				x2 = 7-z2;
				z2 = t;
			}
			else if (rot==3) {
				int t = x1;
				x1 = z1;
				z1 = 7-t;
				t = x2;
				x2 = z2;
				z2 = 7-t;
			}
			block = rotateBlock(block, rot);
			
			if (originY+y1>255) y1=255-originY;
			if (originY+y2>255) y2=255-originY;
			if (originY+y1<0) y1=0-originY;
			if (originY+y2<0) y2=0-originY;
			return String.format("/fill %d %d %d %d %d %d %s replace", originX+x1, originY+y1, originZ+z1, originX+x2, originY+y2, originZ+z2, block);
		}
		
		private String fillString(int x1, int y1, int z1, int x2, int y2, int z2, String block, String type) {
			if (originY+y1>255) y1=255-originY;
			if (originY+y2>255) y2=255-originY;
			if (originY+y1<0) y1=0-originY;
			if (originY+y2<0) y2=0-originY;
			return String.format("/fill %d %d %d %d %d %d %s %s", originX+x1, originY+y1, originZ+z1, originX+x2, originY+y2, originZ+z2, block, type);
		}
		
		private String fillStringR(int rot, int x1, int y1, int z1, int x2, int y2, int z2, String block, String type) {
			if (rot == 1) {
				x1 = 7-x1;
				z1 = 7-z1;
				x2 = 7-x2;
				z2 = 7-z2;
			}
			else if (rot == 2) {
				int t = x1;
				x1 = 7-z1;
				z1 = t;
				t = x2;
				x2 = 7-z2;
				z2 = t;
			}
			else if (rot==3) {
				int t = x1;
				x1 = z1;
				z1 = 7-t;
				t = x2;
				x2 = z2;
				z2 = 7-t;
			}
			block = rotateBlock(block, rot);
			
			if (originY+y1>255) y1=255-originY;
			if (originY+y2>255) y2=255-originY;
			if (originY+y1<0) y1=0-originY;
			if (originY+y2<0) y2=0-originY;
			return String.format("/fill %d %d %d %d %d %d %s %s", originX+x1, originY+y1, originZ+z1, originX+x2, originY+y2, originZ+z2, block, type);
		}
		
		public String rotateBlock(String block, int rot) {
			Matcher m;
			StringBuffer sb;
			
			m = cardinalPattern.matcher(block);
			sb = new StringBuffer();
			while (m.find()) {
				for (int i=0; i<4; i++) {
					if (m.group(1).equals(WALLDIR[i])) {
						m.appendReplacement(sb, WALLDIRCONV[rot][i]);
						continue;
					}
				}
			}
			m.appendTail(sb);
			block = sb.toString();

			m = rotationPattern.matcher(block);
			sb = new StringBuffer();
			while (m.find()) {
				int newRotation = (Integer.parseInt(m.group(1))+WALLROTATION[rot]) % 16;
				m.appendReplacement(sb, "rotation="+newRotation);
			}
			m.appendTail(sb);
			block = sb.toString();
			
			return block;
		}
	}
	
	private class Edge implements Comparable<Edge> {
		public int a;
		public int b;
		public int axis; // axis of 0 is x, 1 is y, and 2 is z
		public int cost;
		public Edge(int a, int b, int axis, int cost) {
			this.a = a;
			this.b = b;
			this.axis = axis;
			this.cost = cost;
			if (cost < 0) {
			}
		}
		
		public int compareTo(Edge otherEdge) {
			return cost-otherEdge.cost;
		}
	}

	private String randomLeaves() {
		return BlockIndex.LEAVES[rand.nextInt(BlockIndex.LEAVES.length)];
	}
	
	private String randomWood() {
		return BlockIndex.WOOD[rand.nextInt(BlockIndex.WOOD.length)];
	}
	
	private String randomFlower() {
		return BlockIndex.FLOWERS[rand.nextInt(BlockIndex.FLOWERS.length)];
	}
	
	private String randomFlowerpot() {
		return BlockIndex.FLOWERPOTS[rand.nextInt(BlockIndex.FLOWERPOTS.length)];
	}
	
	private String randomTrapdoor() {
		return BlockIndex.TRAPDOORS[rand.nextInt(BlockIndex.TRAPDOORS.length)];
	}
	
	private String randomGem() {
		return BlockIndex.GEMS[rand.nextInt(BlockIndex.GEMS.length)];
	}
	
	private String randomTableItem() {
		int r = rand.nextInt(9);
		if (r<3) {
			return randomFlowerpot();
		}
		else if (r<7) {
			int rotation = rand.nextInt(16);
			String headName = BlockIndex.TABLE_HEAD_NAMES[rand.nextInt(BlockIndex.TABLE_HEAD_NAMES.length)];
			return String.format("player_head[rotation=%d]{SkullOwner:{Name:\"%s\"}}", rotation, headName);
		}
		else if (r<8) {
			int rotation = rand.nextInt(3) + 11;
			return String.format("oak_sign[rotation=%d]", rotation) + getSignData("§7§lBox §3§lStructure", "", "made by", "§b§lhhhzzzsss");
		}
		else {
			return String.format("turtle_egg[eggs=%d]", rand.nextInt(4)+1);
		}
	}

	private String randomLogExcerpt() {
		ArrayList<File> logs = new ArrayList<>();
		for (File file : Logger.logDir.listFiles()) if (file.getName().endsWith(".gz")) {
			logs.add(file);
		}
		if (logs.size() == 0) {
			return "lectern[facing=east,has_book=true]{Book:{id:\"minecraft:written_book\",Count:1b,tag:{title:\"§3Log §4Error\",author:\"§b§lhhhzzzsss\",pages:['{\"text\":\"Could not find any logs\"}']}}}";
		}
		try {
			BufferedReader reader = Logger.getLogReader( logs.get(rand.nextInt(logs.size())) );
			List<String> lines = new ArrayList<>();
			String line;
			String date = reader.readLine();
			while ((line = reader.readLine()) != null) {
				if (line.length() > 300) {
					line = line.substring(0, 300);
				}
				lines.add(line.replaceAll("^(\\[\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\] )?(\\[.*:.*\\] )?", ""));
			}
			int startLine = rand.nextInt(Math.max(1, lines.size()-100));
			lines = lines.subList(startLine, Math.min(lines.size(), startLine+100));
			return String.format("lectern[facing=east,has_book=true]{Book:%s}", getBookData(lines, "§3Log Excerpt from §6"+date, "§b§lhhhzzzsss"));
		}
		catch (IOException e) {
			e.printStackTrace();
			return String.format("lectern[facing=east,has_book=true]{Book:{id:\"minecraft:written_book\",Count:1b,tag:{title:\"§3Log §4Error\",author:\"§b§lhhhzzzsss\",pages:['{\"text\":\"Error reading logs: %s\"}']}}}", escape(e.getMessage()));
		}
	}
	
	private String randomVaultHead() {
		int rotation = rand.nextInt(16);
		String headName = BlockIndex.VAULT_HEAD_NAMES[rand.nextInt(BlockIndex.VAULT_HEAD_NAMES.length)];
		return String.format("player_head[rotation=%d]{SkullOwner:{Name:\"%s\"}}", rotation, headName);
	}
	
	private String getTreasureChestData() {
		ArrayList<String> items = new ArrayList<>();
		for (int i=0; i<27; i++) {
			//items.add(String.format("{Slot:%db,id:\"%s\",Count:%db,tag:%s}", i, "minecraft:barrier", 64, "{display:{Name:'{\"text\":\"Not Implemented Yet\",\"color\":\"red\",\"italic\":true}'},Enchantments:[{}]}"));
			int r = rand.nextInt(20);
			if (r < 5) {
				items.add(new ContainerItem(i, getItemSpawnEgg(), rand.nextInt(64)).toString());
			}
			else if (r<6) {
				String item = BlockIndex.LOOT_ITEMS[rand.nextInt(BlockIndex.LOOT_ITEMS.length)];
				items.add(item.substring(0, item.lastIndexOf('}')+1).replaceFirst("\\{", "{Slot:" + i + "b,"));
			}
		}
		return joinItemData(items);
	}
	
	private String getSignData(String text1, String text2, String text3, String text4) {
		return String.format("{Text1:'{\"text\":\"%s\"}',Text2:'{\"text\":\"%s\"}',Text3:'{\"text\":\"%s\"}',Text4:'{\"text\":\"%s\"}'}", text1, text2, text3, text4);
	}

	private String getContainerData(ArrayList<ContainerItem> items) {
		String[] itemStrings = new String[items.size()];
		for (int i=0; i<items.size(); i++) {
			itemStrings[i] = items.get(i).toString();
		}
		return String.format("{Items:[%s]}", String.join(",", itemStrings));
	}
	
	private String joinItemData(ArrayList<String> items) {
		return String.format("{Items:[%s]}", String.join(",", items));
	}
	
	private String getBookData(List<String> lines, String title, String author) {
		ArrayList<String> pageStrings = new ArrayList<>();
		StringBuilder pageBuilder = new StringBuilder();
		int pageLines = 0;
		for (int i=0; i<lines.size(); i++) {
			if (pageLines + lines.get(i).length()/19 + 1 > 14) {
				pageStrings.add(String.format("'{\"text\":\"%s\"}'", escape(pageBuilder.toString())));
				pageBuilder = new StringBuilder();
				pageLines = 0;
			}
			if (pageStrings.size() > 30) {
				break;
			}
			pageBuilder.append(lines.get(i) + "\n");
			pageLines += lines.get(i).length()/20 + 1;
		}
		pageStrings.add( String.format("'{\"text\":\"%s\"}'", escape(pageBuilder.toString())) );
		return String.format("{id:\"minecraft:written_book\",Count:1b,tag:{title:\"%s\",author:\"%s\",pages:[%s]}}", title, author, String.join(",", pageStrings));
	}
	
	private Item getItemSpawnEgg() {
		int itemIndex = rand.nextInt(BlockIndex.ITEMS.length-1)+1;
		String itemId = BlockIndex.ITEMS[itemIndex];
		String itemName = escape(BlockIndex.ITEM_DISPLAY_NAMES[itemIndex]);
		boolean glinting = rand.nextBoolean();
		String tag;
		if (glinting)
			tag = String.format("{Enchantments:[{}],display:{Name:'{\"text\":\"§6Glinting §3%s Item §2Spawn Egg\"}'},EntityTag:{id:\"minecraft:snowball\",NoGravity:1b,Item:{id:\"%s\",Count:1b,tag:{Enchantments:[{}]}}}}", itemName, itemId);
		else
			tag = String.format("{display:{Name:'{\"text\":\"§3%s Item §2Spawn Egg\"}'},EntityTag:{id:\"minecraft:snowball\",NoGravity:1b,Item:{id:\"%s\",Count:1b}}}", itemName, itemId);
		String spawnEgg = BlockIndex.SPAWN_EGGS[rand.nextInt(BlockIndex.SPAWN_EGGS.length)];
		return new Item(spawnEgg, tag);
	}
	
	public String escape(String str) {
		return str.replace("\\","\\\\\\\\").replace("\"", "\\\\\"").replace("'","\\\'");
	}
}
