import utils.SuperFeature;
import org.junit.Test;
import utils.Chunk;
import utils.MyFileUtil;
import utils.Properties;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * ClassName AppTest
 * Description
 * Author Ymkal
 * Date  11/24/2020
 */

public class AppTest {
    @Test
    public void test_delete_dirs() throws IOException {
        MyFileUtil.deleteDirectoryStream(Paths.get("D:\\uscDailyWorkSpace\\论文\\唐佳\\FinesseOptimizeV5\\DevNASSourceCode\\result_finesse\\result_files"));

    }

    @Test
    public void test_random_int(){
        List<Integer> linear_m = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0; i < 12; i ++){
            linear_m.add(random.nextInt(999) + 1);

        }
        System.out.println(linear_m);
    }

    @Test
    public void buffer_write() throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("buffer_write.txt", true), 8);
        byte[] data = "a".getBytes();
        bos.write(data);
        bos.close();
    }

    @Test
    public void test_map(){
        Map<String, Integer> k = new HashMap<>();
        k.put("hash1", 1);
        k.put("hash1", 2);
        for(Map.Entry<String, Integer> entry : k.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    @Test
    public void for_each(){
        List<Integer> l = new ArrayList<Integer>(){{add(1);add(2);add(3);}};
        int f = 0;
        for(Integer i : l){
            i = 4;
            System.out.println(i);
            System.out.printf("index : %d%n", f);
        }

    }

    @Test
    public void clear_nsf() throws IOException {
        File f1 = new File(Properties.NSF_SF_PATH);
        f1.deleteOnExit();
        deleteFile(new File(Properties.NSF_RESULT_DIR));
        deleteFile(new File(Properties.NSF_METADATA_DIR));
    }

    @Test
    public void clear_finesse() throws IOException {
        File f1 = new File(Properties.PROTOTYPE_SF_PATH);
        f1.deleteOnExit();
        deleteFile(new File(Properties.PROTOTYPE_METADATA_DIR));
        deleteFile(new File(Properties.PROTOTYPE_RESULT_DIR));
    }
    @Test
    public void clear_our_method_nsf(){
        File f1 = new File(Properties.N_CHUNK_HASH_FILE_PATH);
        File f2 = new File(Properties.N_FINE_GRIT_SF_PATH);
        File f3 = new File(Properties.N_COARSE_GRAIN_SF_PATH);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        deleteFile(new File(Properties.N_RESULT_DIR));
        deleteFile(new File(Properties.N_METADATA_DIR));
    }
    @Test
    public void clear_our_method_finesse(){
        File f1 = new File(Properties.CHUNK_HASH_FILE_PATH);
        File f2 = new File(Properties.FINE_GRIT_SF_PATH);
        File f3 = new File(Properties.COARSE_GRAIN_SF_PATH);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        deleteFile(new File(Properties.RESULT_DIR));
        deleteFile(new File(Properties.METADATA_DIR));
        deleteFile(new File(Properties.BLOCK_ALL_HASH_DIR));
    }

    @Test
    public void save_file() throws IOException {
        File f1 = new File(Properties.CHUNK_HASH_FILE_PATH);
        File f2 = new File(Properties.FINE_GRIT_SF_PATH);
        File f3 = new File(Properties.COARSE_GRAIN_SF_PATH);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        deleteFile(new File(Properties.RESULT_DIR));
        deleteFile(new File(Properties.METADATA_DIR));
        deleteFile(new File(Properties.BLOCK_ALL_HASH_DIR));
    }

    public static void deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return ;
        }

        if (dirFile.isFile()) {
            dirFile.delete();
        } else {
            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
    }

    @Test
    public void Test(){
        MyFileUtil.copyDir("old", "new");
    }

    @Test
    public void test_block_all_hash(){
        Chunk c = new Chunk();
        c.set_block_all_hash(new ArrayList<Long>(){
            {
                add(1L);
                add(2L);
                add(3L);
                add(4L);
                add(5L);
                add(6L);
                add(7L);
                add(8L);
                add(9L);
                add(10L);
                add(11L);
                add(12L);
                add(13L);
                add(14L);
            }
        });
        System.out.println(c.get_block_all_hash());
    }

    @Test
    public void test_rabin() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("a").append("b").append("c");
        StringBuilder sb2 = sb.deleteCharAt(0);
        sb2.deleteCharAt(0);
        sb.delete(0, sb.length());
        System.out.println(sb);
        System.out.println(sb2);
    }

    @Test
    public void test_create_file() throws IOException {
        InputStream in = new FileInputStream("tst.txt");
        int i = in.read();
        System.out.println(i);
    }

    @Test
    public void test_sfs(){
        List<Long > resource = new ArrayList<Long>(){
            {
                add(1L);
                add(2L);
                add(3L);
                add(4L);
                add(5L);
                add(6L);
                add(7L);
                add(8L);
                add(9L);
                add(10L);
                add(11L);
                add(12L);
                add(13L);
                add(14L);
            }
        };
        System.out.println(SuperFeature.getSfsNSF(resource));
    }

}
