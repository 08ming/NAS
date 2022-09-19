package deduplicator;

import org.apache.log4j.Logger;
import utils.MyFileUtil;
import utils.Properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static utils.MyFileUtil.getFiles;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException {
        // 配置文件夹
        try {
            String prefix = args[2];
            Properties.Reset(prefix);
        }catch (Exception e){
            System.out.println("没有配置文件夹前缀，默认为jar路径下的result_*文件夹");
        }
        try {
            if(Files.exists(Paths.get(Properties.PROTOTYPE_RESULT_DIR)))
                MyFileUtil.deleteDirectoryStream(Paths.get(Properties.PROTOTYPE_RESULT_DIR));
            if(Files.exists(Paths.get(Properties.PROTOTYPE_SF_PATH)))
                MyFileUtil.deleteDirectoryStream(Paths.get(Properties.PROTOTYPE_SF_PATH));
            if(Files.exists(Paths.get(Properties.PROTOTYPE_METADATA_DIR)))
                MyFileUtil.deleteDirectoryStream(Paths.get(Properties.PROTOTYPE_METADATA_DIR));
            Files.createDirectories(Paths.get(Properties.PROTOTYPE_RESULT_DIR));
            Files.createDirectories(Paths.get(Properties.PROTOTYPE_SF_PATH).getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //clear(); // 清楚上次运行产生的所有数据内容
        File folder_f = new File(args[0]);
        ResemblanceDetection rd = null;
        try {
            rd =new ResemblanceDetection(args[3]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 配置参数
        List<String> files = getFiles(folder_f);
        assert rd != null;
        rd.setAverage_size(Integer.parseInt(args[1])); // 平均大小
        rd.setSub_chunk_count(12); // 每个块有多少个子块
        rd.setSfs_count(3); // 最终得到SF的个数

        long start = System.currentTimeMillis();
        System.out.println("========================= Start Prototype Process =========================");
        System.out.println("=========================    current settings     =========================");
        System.out.println(Arrays.toString(args));
        for (String file : files) {
            rd.Init(file);
            rd.process();
        }
        System.out.println("========================== End Prototype Process ==========================");
        long end = System.currentTimeMillis();
        System.out.println("===========================================================================");
        System.out.printf("========Process time :  %s%n", end - start);
        long before_size = MyFileUtil.getFileSize(folder_f);
        long after_size = MyFileUtil.getFileSize(new File(Properties.PROTOTYPE_RESULT_DIR));
        System.out.printf("========DCR  :  %s%n", before_size * (1.0) / after_size);
        System.out.printf("========DCE  :  %s%n", rd.getDCE());
        long metadata_size = MyFileUtil.getFileSize(new File(Properties.PROTOTYPE_METADATA_DIR));
        System.out.printf("========Meta size  :  %s%n", metadata_size);
        System.out.printf("========DCR2  :  %s%n", before_size * (1.0) / (after_size + metadata_size));
        System.out.println("==========================================================================");
    }
}
