package utils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 *  主要工作: 对文件进行CDC分块，并记录块的信息：
 * 	Date: 11/24/2020 10:05  叶旭明
 *	与TTTD类的不同之处在于根据字节流生成块，进行了适合优化算法的微调
 * 	chunk的哈希值作为其文件名，写入到磁盘上
 */
public class TTTTChunk {
	Long p = 0L, l = 0L, backupBreak = 0L;//  p: current position      l: previous boundary position        backupBreak : boundary's swap space
	int sliding_window_size = 1000;
	StringBuilder current_for_rabin = new StringBuilder();
	InputStream io;

	InputStream fio;
	int Tmin = 512;
	List<Long> block_all_hash;
	int Tmax = 1024;
	int Ddash = 270, D = 540;
	int sub_block_count;
	public static TTTTChunk instance;
	byte[] data;
	int average_size;
	// NSF的专属变量
	private static List<Integer> m;
	private static List<Integer> a;
	private final static Integer L = 12;

	// Odess
	int mask;
	List<Integer> linear_m;
	List<Integer> linear_a;
	// TODO  record each chunk's rabin hash and serial number   day 2020/10/11
	RabinHashFunction rabinHashFunction = new RabinHashFunction();
	public Map<Integer, Chunk> breakpointlist; // contains each chunk's boundary   Integer: serial number      Long: boundary position
	String filepath;

	public void Init() {
		this.p = 0L;
		this.l = 0L;
		this.filepath = "";
		this.data = null;
		this.backupBreak = 0L;
		current_for_rabin.delete(0, current_for_rabin.length());
		this.block_all_hash = new ArrayList<>();
		this.breakpointlist = new HashMap<>();
		rabinHashFunction = new RabinHashFunction();
	}
	private TTTTChunk() {
		this.Init();
		Random r = new Random();
		m = new ArrayList<>();
		a = new ArrayList<>();
		for (int i = 0; i < L; i++) {
			m.add(r.nextInt(20) + 1);
			a.add(r.nextInt(20) + 1);
		}

		// Odess
		mask = 0b1111100;
		linear_m = new ArrayList<>();
		linear_a = new ArrayList<>();
		Random random = new Random(System.currentTimeMillis());
		for(int i = 0; i < L; i ++){
			linear_m.add(random.nextInt(999) + 1);
		}
		for(int i = 0; i < L; i ++){
			linear_a.add(random.nextInt(999) + 1);
		}
		PropertyConfigurator.configure("log4j2.xml");
		BasicConfigurator.configure();
	}

	public static TTTTChunk getInstance(){
		if(instance == null){
			synchronized (TTTTChunk.class){
				if(instance == null){
					instance = new TTTTChunk();
				}
			}
		}
		return instance;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
		this.io = new ByteArrayInputStream(data);
		this.fio = new ByteArrayInputStream(data);
	}

	public void setSub_block_count(int sub_block_count) {
		this.sub_block_count = sub_block_count;
	}

	public List<Long> getBlock_all_hash(){
		return this.block_all_hash;
	}

	public void setBlock_all_hash(List<Long> hash){
		this.block_all_hash = hash;
	}

	public int getTmin() {
		return Tmin;
	}

	public void setTmin(int tmin) {
		Tmin = tmin;
	}

	public int getTmax() {
		return Tmax;
	}

	public void setTmax(int tmax) {
		Tmax = tmax;
	}

	public int getDdash() {
		return Ddash;
	}

	public void setDdash(int ddash) {
		Ddash = ddash;
	}

	public int getD() {
		return D;
	}

	public void setD(int d) {
		D = d;
	}

	public int getAverage_size() {
		return average_size;
	}

	public void setAverage_size(int average_size) {
		this.average_size = average_size;
		this.Tmax = average_size * 2;
		this.Tmin = average_size / 2;
	}

	public void setBreakpointlist(Map<Integer, Chunk> breakpointlist) {
		this.breakpointlist = breakpointlist;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) throws FileNotFoundException {
		this.filepath = filepath;
		io = new FileInputStream(new File(filepath));
		fio = new FileInputStream(filepath);
	}

	public void setDefaultFilepath(String filepath){
		this.filepath = filepath;
	}


	public int getSliding_window_size() {
		return sliding_window_size;
	}

	public void setSliding_window_size(int sliding_window_size) {
		this.sliding_window_size = sliding_window_size;
	}


	public void recordChunksPosition(String method) throws NoSuchAlgorithmException {
		try {
			breakpointlist.put(0,new Chunk(0L,0L,0L));
			long hashvalue = 0;
			int c = -1;
			while ((c = io.read()) != -1) {
				p++;
				hashvalue = updateHash(c);
				block_all_hash.add(hashvalue); // 记录每个块的所有Hash值
				if (p - l < Tmin) {// chunk_size = p -l  means that current size is less than Tmin
					continue;
				}
				if (((hashvalue % Ddash) == Ddash - 1) && (hashvalue != 0)) { // satisfy the condition of become a boundary     backupBreak : Boundary temporary storage area
					backupBreak = p;
				}
				if (hashvalue % D == D - 1) {// current hash value satisfy the condition , this position will be a boundary
					func(p, hashvalue, method);
					backupBreak = 0L;
					l = p;
					continue;
				}
				if (p - l < Tmax) {// current hash value doesn't  satisfy the condition and chunk size is less than Tmax
					continue;
				}
				if (backupBreak != 0) {// if current chunk size is greater than or equal to Tmax, record the position
					func(backupBreak,hashvalue, method);
					l = backupBreak;
					backupBreak = 0L;
				} else {// else the backupBreak is null，then the chunk size is Tmax
					func(p,hashvalue, method);
					l = p;
					backupBreak = 0L;
				}
			}
			if(!p.equals(l))
				func(p, hashvalue, method);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void func(Long p, Long hashvalue, String method) throws IOException, NoSuchAlgorithmException {
		Chunk pre_chunk = breakpointlist.get(	breakpointlist.size() -1 );
		Long offset = pre_chunk.offset + pre_chunk.size;
		Long size = p - offset;
		Chunk current_chunk;

		if(size < this.Tmin){ /// 该语句判断最后一个块是否达到了Tmin的要求，如果没有达到，则我们将最后一个块和倒数第二个块合并
			if(breakpointlist.size() < 2){
				// 如果大小为1， 则表明其为空值
				current_chunk = new Chunk(offset, hashvalue , size);
				current_chunk.setSuper_features(this.getSuperFeatures(method));

				current_chunk.setParent_file_path(this.filepath);
				byte[] chunk_data = new byte[Math.toIntExact(size)];
				fio.read(chunk_data);
				current_chunk.setChunk_data(chunk_data);
				addBreakpoint(current_chunk);
				block_all_hash = new ArrayList<>();
				return;
			}
			current_chunk = new Chunk(pre_chunk.getOffset(), hashvalue , size + pre_chunk.getSize());
			current_chunk.setSuper_features(pre_chunk.getSuper_features());// 该合并的块的12个finesse哈希值从倒数第二个块为准
			current_chunk.setParent_file_path(this.filepath);
			byte[] chunk_data = new byte[Math.toIntExact(size)];
			fio.read(chunk_data);
			current_chunk.setChunk_data(chunk_data);
			breakpointlist.put(breakpointlist.size() -1, current_chunk);
			block_all_hash = new ArrayList<>();
			return;
		}
		current_chunk = new Chunk(offset, hashvalue , size);
		current_chunk.setSuper_features(this.getSuperFeatures(method));
		current_chunk.setParent_file_path(this.filepath);
		byte[] chunk_data = new byte[Math.toIntExact(size)];
		fio.read(chunk_data);
		current_chunk.setChunk_data(chunk_data);
		addBreakpoint(current_chunk);
		block_all_hash = new ArrayList<>();
	}

	private void addBreakpoint(Chunk c) {
		int chunk_count = breakpointlist.size();
		breakpointlist.put(chunk_count,c);
	}


	/**
	 * 得到 finesse\SFS 需要的12个hash值
	 */
	public List<String> getSuperFeatures(String method) throws NoSuchAlgorithmException { // method: finesse、nsf、odess
		switch (method) {
			case "odess": {
				List<Long> result = new ArrayList<>();
				for (int i = 0; i < L; i++) {
					result.add(0L);
				}
				block_all_hash.forEach((hash) -> {
					if ((hash & mask) == 0) {
						for (int i = 0; i < L; i++) {
							Long transform = (linear_m.get(i) * hash + linear_a.get(i)) % (2 ^ 32);
							if (result.get(i) <= transform) {
								result.set(i, transform);
							}
						}
					}
				});
				return SuperFeature.getSfsNSF(result);
			}
			case "nsf": {
				while (block_all_hash.size() < sub_block_count) { // 空位补充0
					block_all_hash.add(0L);
				}
				List<Long> result = new ArrayList<>();
				for (int i = 0; i < L; i++) {
					result.add(0L);
				}
				block_all_hash.forEach((hash) -> {
					for (int i = 0; i < L; i++) {
						Long transform = (m.get(i) * hash + a.get(i)) % (Integer.MAX_VALUE);
						if (result.get(i) <= transform) {
							result.set(i, transform);
						}
					}
				});
				return SuperFeature.getSfsNSF(result);
			}
			case "finesse": {
				int size = block_all_hash.size();
				while (block_all_hash.size() < sub_block_count) { // 空位补充0
					block_all_hash.add(0L);
				}
				int each_size = size / sub_block_count;
				int last_size = each_size + size % sub_block_count;
				long max_value = 0L;
				List<Long> max_values = new ArrayList<>();

				for (int i = 0; i < size - last_size; i++) {
					if (max_value < block_all_hash.get(i)) {
						max_value = block_all_hash.get(i);
					}
					if ((i + 1) % each_size == 0) {// record chunk's info and 12 maximum hash value
						max_values.add(max_value);
						max_value = 0L;
					}
				}
				for (int i = size - 1; i >= size - last_size; i--) {
					if (max_value < block_all_hash.get(i)) {
						max_value = block_all_hash.get(i);
					}
				}
				max_values.add(max_value);
				return SuperFeature.getSfs(max_values, 3);
			}

		}
		return null;
	}

	private long updateHash(int c) {
		int current_size = current_for_rabin.length();
		if (current_size < sliding_window_size) {
			current_for_rabin.append(c);
		} else {
			//current_for_rabin = current_for_rabin.substring(current_size - sliding_window_size + 1);		// Otherwise the window slides to the right 1 byte
			current_for_rabin.deleteCharAt(0);
		}
		String current_for_rabin_to_str = current_for_rabin.toString();
		long hash = rabinHashFunction.hash(current_for_rabin_to_str);
		current_for_rabin_to_str = null;
		return hash;
	}


	public Map<Integer, Chunk> getBreakpointlist() {
		return breakpointlist;
	}

}


