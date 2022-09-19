package naswithcard;

import utils.MyFileUtil;
import utils.Properties;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassName Main
 * Description
 * Author Ymkal
 * Date  11/26/2020
 *
 * 优化方法的启动主函数
 *
 * 1.每次运行之后， 可以运行clear 函数，删除数据内容，压缩之后的文件夹是的配置为RESULT_DIR, 在utils包下的Properties类中设置
 *
 * 0. 数据源路径
 * 1. 平均块长大小
 * 2. 细粒度平均块长大小
 * 3. 处理文件头缀
 * 4. 粗粒度文件路径
 * 5. 脚本路径 （加上 python 命令）
 * 6. 细粒度模型路径
 * meta ： 元数据和上述一致
 */
public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        try {
            String prefix = args[3];
            Properties.Reset(prefix);
        }catch (Exception e){
            System.out.println("没有配置文件夹前缀，默认为jar路径下的result_*文件夹");
        }
        Files.write(Paths.get(args[3], "time.out"), "0".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        File folder_f = new File(args[0]); // 数据源路径， 其他的配置均在Properties中

        OptFinesse bc = new OptFinesse(); // 配置BigChunk
        bc.setChunk_hash_file_path(Properties.CHUNK_HASH_FILE_PATH);
        bc.setAverage_size(Integer.parseInt(args[1]));
        bc.setAverage_size_fine(Integer.parseInt(args[2]));

        List<String> files = getFiles(folder_f);// 获取文件夹下的所有文件
        long start = System.currentTimeMillis();
        System.out.println("========================= Start Optimize Process =========================");
        for (String file : files) { // 对文件进行处理
            bc.Init(file);
            bc.findRedundancyChunkPosition();
            bc.processFinesse(args[4], args[5], args[6]);
        }
        bc.clear();
        System.out.println("========================== End Optimize Process ==========================");
        long end = System.currentTimeMillis();
        System.out.println("==========================================================================");
        System.out.printf("========Process time :  %s%n", end - start);
        String time_str = new String(Files.readAllBytes(Paths.get(args[3], "time.out")));
        System.out.printf("========Process time - load time :  %s%n", end - start - Float.parseFloat(time_str));
        Files.deleteIfExists(Paths.get(args[3], "time.out"));
        long before_size = MyFileUtil.getFileSize(folder_f);
        long after_size = MyFileUtil.getFileSize(new File(Properties.RESULT_DIR));
        System.out.printf("========DCR  :  %s%n", before_size * (1.0) / after_size);
        //System.out.printf("========DCE  :  %s%n", bc.getDCE());(先注释掉)
        long metadata_size = MyFileUtil.getFileSize(new File(Properties.METADATA_DIR));
        System.out.printf("========Meta size  :  %s%n", metadata_size);
        System.out.printf("========DCR2  :  %s%n", before_size * (1.0) / (after_size + metadata_size));
        System.out.println("==========================================================================");
    }

    public static List<String> getFiles(File f) {
        List<String> path = new ArrayList<>();
        if (f.isDirectory()) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                path.addAll(getFiles(file));
            }
        } else {
            path.add(f.getPath());
        }
        return path;
    }

    // 清除所有数据
    public static void clear() throws IOException {
        File f1 = new File(Properties.CHUNK_HASH_FILE_PATH);
        File f2 = new File(Properties.FINE_GRIT_SF_PATH);
        File f3 = new File(Properties.COARSE_GRAIN_SF_PATH);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        deleteFile(new File(Properties.RESULT_DIR));
        deleteFile(new File(Properties.METADATA_DIR));
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

}
