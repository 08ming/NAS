package utils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Chunk{
	Long offset;
	Long hashvalue;
	Long size;
	String parent_file_path;
	List<String> super_features;
	List<Long> block_all_hash;
	byte[] chunk_data;

	public byte[] getChunk_data() {
		return chunk_data;
	}

	public void setChunk_data(byte[] chunk_data) {
		this.chunk_data = chunk_data;
	}

	public Chunk(Long offset, Long hashvalue, Long size) {
		this.offset = offset;
		this.hashvalue = hashvalue;
		this.size = size;
	}

	public Chunk() {
		this.offset = 0L;
		this.hashvalue = 0L;
		this.size = 0L;
		this.block_all_hash = new ArrayList<>();
	}

	@Override
	public String toString() {
		return parent_file_path + ","
				+ offset + ","
				+ size + ","
				+ hashvalue;
	}

	public void set_block_all_hash(List<Long > block_all_hash) {
		File f = new File(Paths.get(Properties.BLOCK_ALL_HASH_DIR, hashvalue.toString()).toString());
		try (FileWriter fw = new FileWriter(f)){
			for(Long hash : block_all_hash){
				fw.write(hash.toString()+"\n");
			}
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Long > get_block_all_hash(){
		List<Long > block_all_hash = new ArrayList<>();
		File f = new File(Paths.get(Properties.BLOCK_ALL_HASH_DIR, hashvalue.toString()).toString());
		if(f.exists()){
			try (BufferedReader br = new BufferedReader(new FileReader(f))){
				String line = null;
				while (null != (line = br.readLine())){
					block_all_hash.add(Long.parseLong(line));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("error");
		}
		return block_all_hash;
	}

	public List<String> getSuper_features() {
		return super_features;
	}

	public String getParent_file_path() {
		return parent_file_path;
	}

	public void setParent_file_path(String parent_file_path) {
		this.parent_file_path = parent_file_path;
	}

	public void setSuper_features(List<String> super_features) {
		this.super_features = super_features;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getHashvalue() {
		return hashvalue;
	}

	public void setHashvalue(Long hashvalue) {
		this.hashvalue = hashvalue;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
}