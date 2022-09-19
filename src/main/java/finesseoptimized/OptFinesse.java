package finesseoptimized;

import com.davidehrmann.vcdiff.VCDiffEncoder;
import com.davidehrmann.vcdiff.VCDiffEncoderBuilder;
import utils.SuperFeature;
import utils.Chunk;
import utils.Properties;
import utils.TTTTChunk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public void processFinesse() throws IOException {
        this._breakpointlist.forEach((index, chunk) -> {
            if(index != 0 && !this._redundancy_sign.contains(index)){
                if (this._almost_non_duplicate || this._fine_grit_sign.contains(index)){ // 细粒度的finesse处理
                    try {
                        processFineGritSign(chunk, average_size_fine);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    // 粗粒度的finesse处理
                    processCoarseGrainSign(chunk);
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

    public void processFineGritSign(Chunk c, int average_size) throws IOException, NoSuchAlgorithmException {
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
                    List<String> sfs = chunk.getSuper_features();
                    String contain_flag = contains(sfs, 0); // model 为0 表示细粒度
                    StringBuilder sb = new StringBuilder();
                    for (String sf : sfs) {
                        sb.append(sf).append(",");
                    }
                    if(!"-1".equals(contain_flag)){// 含有相似块
                        //TODO 使用VCDIFF生成DIFF文件，并记录其信息
                        //1. 源文件
                        byte[] dictionary = readFile(Paths.get(Properties.RESULT_DIR, contain_flag).toString());
                        //2. 目标文件
                        RandomAccessFile raf = new RandomAccessFile(this._file_path,"r");
                        raf.seek(c.getOffset() + chunk.getOffset());
                        byte[] uncompressedData = new byte[chunk.getSize().intValue()];
                        raf.read(uncompressedData);
                        raf.close();
                        //3. 生成Diff文件
                        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
                        VCDiffEncoder<OutputStream> encoder = VCDiffEncoderBuilder.builder()
                                .withDictionary(dictionary)
                                .buildSimple();
                        encoder.encode(uncompressedData, compressedData);
                        byte[] bytes = compressedData.toByteArray();
                        compressedData.close();
                        // 写入 Diff文件
                        if(bytes.length > uncompressedData.length){ // 如果diff文件比源文件还要大，则不压缩
                            chunk.setOffset(c.getOffset() + chunk.getOffset());
                            Files.write(Paths.get(Properties.FINE_GRIT_SF_PATH), (sb.toString() +" " + chunk + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                            Chunk tmp = new Chunk();
                            tmp.setOffset(c.getOffset() + chunk.getOffset());
                            tmp.setSize(chunk.getSize());
                            saveFile(tmp,Paths.get(Properties.RESULT_DIR, sb.toString()).toString());
                            String metadata = chunk.toString() + " " + sb.toString();
                            write_string_to_file(this._metadata_file_path,metadata);
                        }else {
                            BOS.write(bytes);
                            BOS.write("\r\n".getBytes()); // 加入换行符
                            DCE.add((double)bytes.length / uncompressedData.length);
                            String metadata = chunk.toString() + " " + sb.toString();
                            write_string_to_file(this._metadata_file_path,metadata);
                        }
                    }else{
                        //TODO 保存整个块
                        // 1. 保存sfs和块的信息到文件中 fine_grit_sf_path
                        // 2. 保存块的数据 saveFile()
                        chunk.setOffset(c.getOffset() + chunk.getOffset());
                        Files.write(Paths.get(Properties.FINE_GRIT_SF_PATH), (sb.toString() +" " + chunk + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        Chunk tmp = new Chunk();
                        tmp.setOffset(c.getOffset() + chunk.getOffset());
                        tmp.setSize(chunk.getSize());
                        saveFile(tmp,Paths.get(Properties.RESULT_DIR, sb.toString()).toString());
                        String metadata = chunk.toString() + " " + sb.toString();
                        write_string_to_file(this._metadata_file_path,metadata);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        // 如果这个块的相似内容多余30%， 那么将下一个块也作为相似块进行处理（细粒度处理）
        // return ((double) similar_chunk_count.get() / (double) chunk_size) >= 0.3;
    }

    /**
     * 处理粗粒度的块
     */
    public void processCoarseGrainSign(Chunk chunk){
        try {
            List<String> sfs = chunk.getSuper_features();
            String contain_flag = contains(sfs, 1); // model 为1 表示粗粒度
            StringBuilder sb = new StringBuilder();
            for (String sf : sfs) {
                sb.append(sf).append(",");
            }
            if(!"-1".equals(contain_flag)){// 含有相似块
                //TODO 使用VCDIFF生成DIFF文件，并记录其信息
                //1. 源文件
                byte[] dictionary = readFile(Paths.get(Properties.RESULT_DIR, contain_flag).toString());
                //2. 当前处理的文件
                RandomAccessFile raf = new RandomAccessFile(this._file_path,"r");
                raf.seek(chunk.getOffset());
                byte[] uncompressedData = new byte[chunk.getSize().intValue()];
                raf.read(uncompressedData);
                raf.close();
                //3. 生成Diff文件
                ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
                VCDiffEncoder<OutputStream> encoder = VCDiffEncoderBuilder.builder()
                        .withDictionary(dictionary)
                        .buildSimple();
                encoder.encode(uncompressedData, compressedData);
                byte[] bytes = compressedData.toByteArray();
                // 写入 Diff文件
                if(bytes.length > uncompressedData.length){ // 如果diff文件比当前块还要大，则不压缩
                    Files.write(Paths.get(Properties.COARSE_GRAIN_SF_PATH), (sb.toString() +" " + chunk + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    saveFile(chunk,Paths.get(Properties.RESULT_DIR, sb.toString()).toString());
                    String metadata = chunk.toString() + " " + sb.toString();
                    write_string_to_file(this._metadata_file_path,metadata);
                }else {
                    DCE.add((double)bytes.length / uncompressedData.length);
                    BOS.write(bytes);
                    BOS.write("\r\n".getBytes()); // 加入换行符
                    String metadata = chunk.toString() + " " + sb.toString();
                    write_string_to_file(this._metadata_file_path,metadata);
                }
            }else{
                //TODO 保存整个块
                // 1. 保存sfs和块的信息到文件中 fine_grit_sf_path
                // 2. 保存块的数据 saveFile()
                Files.write(Paths.get(Properties.COARSE_GRAIN_SF_PATH), (sb.toString() +" " + chunk + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                saveFile(chunk,Paths.get(Properties.RESULT_DIR, sb.toString()).toString());
                String metadata = chunk.toString() + " " + sb.toString();
                write_string_to_file(this._metadata_file_path,metadata);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getDCE(){
        double sum = 0;
        for (Double aDouble : DCE) {
            sum += aDouble;
        }
        return sum / DCE.size();
    }

    /**
     * 判断是否存在相似块
     * @param sfs Super_feature 值
     * @param model 模式：1  为粗粒度  其他为细粒度
     * @return 若存在则返回其文件所在信息，否则返回-1
     */
    public String contains(List<String> sfs, int model) throws IOException {
        String fp = Properties.FINE_GRIT_SF_PATH;
        if(model == 1){
            fp = Properties.COARSE_GRAIN_SF_PATH;
        }
        File fpFile = new File(fp);
        if(!fpFile.exists()){
            fpFile.createNewFile();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(fpFile));){
            String line = null;
            while ((line = br.readLine()) != null){
                String[] split = line.split(",");
                // TODO
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    sb.append(split[i]).append(",");
                }
                for (int i = 0; i < 3; i++) {
                    if(sfs.contains(split[i])){
                        br.close();
                        return sb.toString();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1+"";
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
                    //saveFile(c);
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
