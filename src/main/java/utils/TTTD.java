package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class TTTD {

	int p = 0, l = 0, backupBreak = 0;
	int sliding_window_size = 1000;
	StringBuilder current_for_rabin = new StringBuilder();
	InputStream io;
	int Tmin = 2141;
	int Tmax = 4096;
	int Ddash = 270, D = 540;
	RabinHashFunction rabinHashFunction = new RabinHashFunction();
	public List<Integer> breakpointlist;
	String filepath;

	public void Init(String filepath) {
		this.filepath = filepath;
		breakpointlist = new ArrayList<Integer>();
		p = 0;
		l = 0;
		backupBreak = 0;
		current_for_rabin = new StringBuilder();
	}

	public void record_chunks_position() {
		try {
			long hashvalue = 0;
			io = new FileInputStream(new File(filepath));
			int c = -1;
			while ((c = io.read()) != -1) {
				p++;
				hashvalue = updateHash(c);

				if (p - l < Tmin) {
					continue;
				}
				if (((hashvalue % Ddash) == Ddash - 1) && (hashvalue != 0)) {
					backupBreak = p;
				}
				if (hashvalue % D == D - 1) {
					addBreakpoint(p);
					backupBreak = 0;
					l = p;
					continue;
				}
				if (p - l < Tmax) {
					continue;
				}
				if (backupBreak != 0) {
					addBreakpoint(backupBreak);
					l = backupBreak;
					backupBreak = 0;
				} else {
					addBreakpoint(p);
					l = p;
					backupBreak = 0;
				}
				if(p != l)
					addBreakpoint(p);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addBreakpoint(int p2) {
		// System.out.println("add breakpoint at
		// -----"+breakpointlist.size()+"-------- "+p2);
		breakpointlist.add(p2);
	}

	public long updateHash(int c) {
		int current_size = current_for_rabin.length();
		if (current_size < sliding_window_size) {
			current_for_rabin.append(c);
		} else {
			current_for_rabin.deleteCharAt(0);
		}
		return rabinHashFunction.hash(current_for_rabin.toString());
	}

	public List<Integer> getBreakpointlist() {
		return breakpointlist;
	}

	public static void main(String[] args) {
		// int i=1,j=2,c=-123;
		TTTD tttd = new TTTD();
		tttd.Init("./files/2.pdf");
		tttd.record_chunks_position();
		for (int i = 1; i < tttd.breakpointlist.size(); i++) {
			System.err.println((tttd.breakpointlist.get(i)-tttd.breakpointlist.get(i-1)));
		}
		System.out.println(tttd.breakpointlist.size());
	}

}
