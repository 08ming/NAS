package deduplicator;

import com.davidehrmann.vcdiff.VCDiffEncoder;
import com.davidehrmann.vcdiff.VCDiffEncoderBuilder;
import utils.Chunk;
import utils.Properties;
import utils.TTTTChunk;

import java.io.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ClassName Finesse
 * Description
 * Author Ymkal
 * Date  11/29/2020
 */

public class ResemblanceDetection {
    String _file_path;
    private String _metadata_file_path;

    private List<Double> DCE = new ArrayList<>(); // DCE
    private int sub_chunk_count = 12;// 一个块再分为N个子块
    private int average_size = 1024;
    private String diff_container = Paths.get(Properties.PROTOTYPE_RESULT_DIR, "diff.txt").toString();
    private int sfs_count = 3;// 最终得到的sfs的数量
    //private BufferedOutputStream bw;
    File sfs;
    String method;
    public ResemblanceDetection(String method) throws IOException {
        sfs = new File(Properties.PROTOTYPE_SF_PATH);
        this.method = method;
        if(!sfs.exists())
            sfs.createNewFile();
    }

    public void Init(String file_path) throws FileNotFoundException {// 初始化
        this._file_path = file_path;
        File f = new File(file_path);
        this._metadata_file_path = Paths.get(Properties.PROTOTYPE_METADATA_DIR, f.getName() + "_metadata").toString();
    }

    public void process() throws FileNotFoundException, NoSuchAlgorithmException {
        TTTTChunk tttd = TTTTChunk.getInstance();
        tttd.Init();
        tttd.setSub_block_count(sub_chunk_count);//设置得到多少个feature值 12
        tttd.setAverage_size(average_size);
        tttd.setFilepath(this._file_path);
        tttd.recordChunksPosition(this.method);
        tttd.getBreakpointlist().forEach((index,chunk)->{
            if(index != 0){
                try {
                    List<String> sfs = chunk.getSuper_features();
                    String contain_flag = contains(sfs); // model 为0 表示细粒度

                    StringBuilder sb = new StringBuilder();
                    for (String sf : sfs) {
                        sb.append(sf).append(",");
                    }
                    if(!"-1".equals(contain_flag)){// 含有相似块
                        //TODO 使用VCDIFF生成DIFF文件，并记录其信息
                        //1. 源文件
                        byte[] dictionary = readFile(Paths.get(Properties.PROTOTYPE_RESULT_DIR, contain_flag).toString());
                        //2. 目标文件
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
                        // 写入 Diff文件 压缩之后的文件，12345 123 diff:45
                        if(bytes.length > uncompressedData.length){ // 如果diff文件比源文件还要大，则不压缩
                            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(this.sfs, true));
                            bw.write((sb.toString() +" " + chunk + "\n").getBytes());// 写入Super-feature SFS
                            bw.flush();
                            bw.close();
                            // 保存数据
                            Chunk tmp = new Chunk();
                            tmp.setOffset(chunk.getOffset());
                            tmp.setSize(chunk.getSize());
                            saveFile(tmp,Paths.get(Properties.PROTOTYPE_RESULT_DIR, sb.toString()).toString());
                            String metadata = chunk.toString() + " " + sb.toString();
                            write_string_to_file(this._metadata_file_path,metadata);
                        }else {
                            DCE.add((double)bytes.length / uncompressedData.length);
                            try (OutputStream out = new FileOutputStream(diff_container, true)){
                                out.write(bytes);
//                                out.write("\r\n".getBytes()); // 加入换行符
                                out.flush();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            String metadata = chunk.toString() + " " + sb.toString();
                            write_string_to_file(this._metadata_file_path,metadata);
                        }
                    }else{
                        //TODO 保存整个块
                        // 1. 保存sfs和块的信息到文件中 finesse_sf_path
                        // 2. 保存块的数据 saveFile()
                        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(this.sfs, true));
                        bw.write((sb.toString() +" " + chunk + "\n").getBytes());
                        bw.flush();
                        bw.close();
                        saveFile(chunk,Paths.get(Properties.PROTOTYPE_RESULT_DIR, sb.toString()).toString());
                        String metadata = chunk.toString() + " " + sb.toString();
                        write_string_to_file(this._metadata_file_path,metadata);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     *
     * @return DCE, 即判断二者为相似块的准确度，也可称为命中率
     */
    public double getDCE(){
        double sum = 0;
        for (Double aDouble : this.DCE) {
            sum += aDouble;
        }
        return sum / DCE.size();
    }
    /**
     * 判断是否存在相似块
     * @param sfs Super_feature 值
     * @return 若存在则返回其文件所在信息，否则返回-1
     */
    public String contains(List<String> sfs){
        try (BufferedReader br = new BufferedReader(new FileReader(this.sfs))){
            // 判断文件是否存在
            String line = null;// sf1, sf2, sf3
            while ((line = br.readLine()) != null){
                String[] split = line.split(",");// split == sfs
                // TODO
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    sb.append(split[i]).append(",");
                }
                for (int i = 0; i < 3; i++) {
                    if(sfs.contains(split[i])){//  修改0.6
                        br.close();
                        return sb.toString();
                    }
                }
            }
            br.close();
            return -1+"";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1+"";
    }

    private byte[] readFile(String fp) throws IOException {
        File f = new File(fp);
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(fp));
        byte[] result = new byte[(int)f.length()];
        int flag = reader.read(result);
        assert flag != -1;
        reader.close();
        return result;
    }

    private void write_string_to_file(String fp, String data){
        File f = new File(fp);
        create_file(f);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fp,true));
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveFile(Chunk chunk, String fp) {
        File f = new File(fp);
        if(f.exists()){
            //如果该文件存在，则直接返回
            Random r = new Random(System.currentTimeMillis());
            f = new File(fp + r.nextInt());
        }
        //return;
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


    /**
     * 若文件不存在则创建文件
     * @param f 文件
     */
    private void create_file(File f) {
        if(f.exists())
            return;
        try {
            if(f.getParentFile().exists() && !f.exists()){
                f.createNewFile();
            }else if(!f.getParentFile().exists()){
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setSub_chunk_count(int sub_chunk_count) {
        this.sub_chunk_count = sub_chunk_count;
    }

    public void setAverage_size(int average_size) {
        this.average_size = average_size;
    }

    public void setSfs_count(int sfs_count) {
        this.sfs_count = sfs_count;
    }
}
