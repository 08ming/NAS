package naswithcard;

import utils.Chunk;
import utils.Properties;
import utils.TTTTChunk;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class MyList extends ArrayList<Integer>{ // 保证存入的数据没有重复的
    @Override
    public boolean add(Integer integer) {
        if(this.contains(integer)){
            return false;
        }
        return super.add(integer);
    }
}

public class OptFinesse {
    // 每次需要重置的变量
    String _file_path;
    public Map<Integer, Chunk> _breakpointlist; // 所有的块的信息
    //private List<Integer> _coarse_grain_sign; // 需要使用大粒度finesse的块  (由于已经知道需要用细粒度处理的块以及冗余块，则该信息可以由前面两者推算出来，故不作处理)
    private List<Integer> _redundancy_sign; // 记录冗余的数据块
    private List<Integer> _fine_grit_sign; // 需要使用小粒度finesse的块
    private boolean _almost_non_duplicate;
    private String _metadata_file_path;//元数据文件存放的路径

    private List<Double> DCE = new ArrayList<>(); // DCE
    private int sub_chunk_count = 12;// 一个块再分为N个子块
    private String chunk_hash_file_path; // 记录了所有块的哈希值的文件路径，用来判断是否为冗余数据
    private int average_size = 1024;// 块的平均大小
    private int average_size_fine = 128;// 细粒度分解时块的平均大小
    private final String diff_container = Paths.get(Properties.RESULT_DIR, "diff.txt").toString();
    private int sfs_count = 3;// 最终得到的sfs的数量
    private File chunk_hash_file;// 存放每个块的哈希值的文件
    BufferedOutputStream BOS = new BufferedOutputStream(new FileOutputStream(diff_container, true), 10240);

    public OptFinesse() throws FileNotFoundException {
    }

    public void clear() throws IOException {
        BOS.flush();
        BOS.close();
    }

    public void Init(String file_path) throws FileNotFoundException, NoSuchAlgorithmException {// 初始化
        this._almost_non_duplicate = false;
        this._file_path = file_path;
        TTTTChunk td = TTTTChunk.getInstance();
        td.Init();
        td.setSub_block_count(sub_chunk_count);
        td.setAverage_size(average_size);
        td.setFilepath(file_path);
        td.recordChunksPosition("finesse");//对文件进行扫描，得到boundaries
        this._breakpointlist = td.getBreakpointlist();
        //this._coarse_grain_sign = new MyList();
        this._redundancy_sign = new MyList();
        this._fine_grit_sign = new MyList();
        File f = new File(file_path);
        this._metadata_file_path = Paths.get(Properties.METADATA_DIR, f.getName() + "_metadata").toString();
    }

    public void processFinesse(String model_path,String script_path, String grained_model_path) throws IOException {
        this._breakpointlist.forEach((index, chunk) -> {
            if(index != 0 && !this._redundancy_sign.contains(index)){
                if (this._almost_non_duplicate || this._fine_grit_sign.contains(index)){ // 细粒度的finesse处理
                    try {
                        processFineGritSign(chunk, average_size_fine, grained_model_path, script_path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    // 粗粒度的finesse处理
                    processCoarseGrainSign(chunk, model_path, script_path);
                }
            }
        });
    }

    /**
     * 检测到冗余块的位置信息
     */
    public void findRedundancyChunkPosition() throws IOException {
        for(Map.Entry<Integer,Chunk> entry : this._breakpointlist.entrySet()){
            if(entry.getKey() == 0){ // 跳过第0个，因为第0个为空
                continue;
            }
            int flag_redundancy = contains(entry.getValue()); // 如果是冗余块则返回其对应的哈希值在哪一行，否在返回-1
            if(flag_redundancy != -1){// 冗余块
                // 1. TODO 冗余块，写入元数据之冗余块
                this._redundancy_sign.add(entry.getKey());
                write_string_to_file(this._metadata_file_path, entry.getKey() + " " + flag_redundancy);
            }
        }

        for (Integer integer : this._redundancy_sign) {
            if(integer -1 < 1){ //若左边的块为空
                continue;
            }
            // 标记冗余块左边的块
            if(  !this._redundancy_sign.contains(integer - 1)  )
                this._fine_grit_sign.add( integer -1 );
            //标记冗余块右边的块
            //首先判断右边是否有块，且右边的块是否为冗余块
            if(integer + 1 < this._breakpointlist.size() && !this._redundancy_sign.contains(integer + 1)){
                this._fine_grit_sign.add(integer + 1);
            }
        }
        if((this._redundancy_sign.size() + this._fine_grit_sign.size()) * (1.0) / this._breakpointlist.size() < 0.01){// 如果相似内容少于1%，则全部用细粒度进行处理
            this._almost_non_duplicate = true;
        }
    }
    /**
     * 处理细粒度的块
     */
    public void processFineGritSign(Chunk c, int average_size, String model_path,String script_path) throws IOException, NoSuchAlgorithmException {
        RandomAccessFile r = new RandomAccessFile(this._file_path,"r");
        r.seek(c.getOffset());
        byte[] data = new byte[c.getSize().intValue()];
        int read_flag = r.read(data);
        r.close();
        if(read_flag == -1){
            System.out.println("error");
        }
        TTTTChunk tttd = TTTTChunk.getInstance();
        tttd.Init();
        tttd.setSub_block_count(sub_chunk_count);//设置得到多少个feature值
        tttd.setData(data);
        tttd.setAverage_size(average_size);
        tttd.setDefaultFilepath(c.getParent_file_path());
        tttd.recordChunksPosition("finesse");
        tttd.getBreakpointlist().forEach((index,chunk)->{
            if(index != 0){
                try {
                    contains(chunk.getChunk_data(), model_path, script_path); // model 为0 表示细粒度
                    String metadata = chunk.toString();
                    write_string_to_file(this._metadata_file_path,metadata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 处理粗粒度的块
     */
    public void processCoarseGrainSign(Chunk chunk, String model_path, String script_path){
        try {
            contains(chunk.getChunk_data(), model_path, script_path); // model 为1 表示粗粒度
            String metadata = chunk.toString();
            write_string_to_file(this._metadata_file_path,metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否存在相似块
     * @return 若存在则返回其文件所在信息，否则返回-1
     */
    public void contains(byte[] data, String model_path, String script_path) throws IOException {
        //
        Process proc;
        try {
            Files.write(Paths.get(Properties.METADATA_DIR, "cash.txt"), data,StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            String sb = script_path + " --model " + model_path + " --data " + Paths.get(Properties.METADATA_DIR, "cash.txt") + " --dir_name " + Properties.RESULT_DIR;
            proc = Runtime.getRuntime().exec(sb);// 执行py文件
            //用输入输出流来截取结果
            proc.waitFor();
            Files.delete(Paths.get(Properties.METADATA_DIR, "cash.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private byte[] readFile(String fp) throws IOException {
        Path path = Paths.get(fp);
        return Files.readAllBytes(path);
    }

    private void write_string_to_file(String fp, String data) throws IOException {
        File f = new File(fp);
        if(f.getParentFile().exists() && !f.exists()){
            f.createNewFile();
        }else if(!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f,true))){
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否冗余块，若是，如果文件不存在，则新建文件写入内容
     * 若不是冗余块，写入文件
     * @param c
     * @return
     */
    public int contains(Chunk c){
        try {
            BufferedReader br = new BufferedReader(new FileReader(chunk_hash_file_path));
            String line = null;
            int line_count = 0;
            String current_file = "";
            while (null != (line = br.readLine())){
                String[] content = line.split(",");
                if(!content[0].equals(current_file))
                    line_count = 0;
                line_count ++;

                // TODO
                if(content[3].equals(c.getHashvalue()+"")){
                    this._fine_grit_sign.remove(new Integer(line_count));
                    br.close();
                    return line_count;
                }
            }
            br.close();
            Files.write(Paths.get(this.chunk_hash_file_path), (c + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private void saveFile(Chunk chunk, String fp) {
        File f = new File(fp);
        if(f.exists()){
            //如果该文件存在，则直接返回
            Random r = new Random(System.currentTimeMillis());
            f = new File(fp + r.nextInt());
        }
        try (FileOutputStream out = new FileOutputStream(f);
             RandomAccessFile raf = new RandomAccessFile(this._file_path, "r")){
            raf.seek(chunk.getOffset());
            byte[] data = new byte[chunk.getSize().intValue()];
            raf.read(data);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChunk_hash_file_path(String chunk_hash_file_path) throws IOException {
        this.chunk_hash_file_path = chunk_hash_file_path;
        this.chunk_hash_file = new File(this.chunk_hash_file_path);
        if (!chunk_hash_file.exists()) {
            Files.createDirectories(Paths.get(chunk_hash_file_path).getParent());
            Files.createFile(Paths.get(chunk_hash_file_path));
        }
    }

    public void setSub_chunk_count(int sub_chunk_count) {
        this.sub_chunk_count = sub_chunk_count;
    }

    public void setAverage_size(int average_size) {
        this.average_size = average_size;
    }

    public void setAverage_size_fine(int average_size_fine) {
        this.average_size_fine = average_size_fine;
    }

    public void setSfs_count(int sfs_count) {
        this.sfs_count = sfs_count;
    }
}
