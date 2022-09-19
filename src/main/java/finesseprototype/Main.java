package finesseprototype;
import utils.MyFileUtil;
import utils.Properties;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static utils.MyFileUtil.getFiles;

/**
 * ClassName Main
 * Description
 * Author Ymkal
 * Date  11/29/2020
 *
 *
 * 1.每次运行之后， 可以运行clear 函数，删除数据内容，压缩之后的文件夹是的配置为PROTOTYPE_RESULT_DIR, 在utils包下的Properties类中设置
 *  meta ： 元数据和上述一致
 *
 *  所有原型已用PROTOTYPE标明
 *
 */
public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        // 配置文件夹
        try {
            String prefix = args[2];
            Properties.Reset(prefix);
        }catch (Exception e){
            System.out.println("没有配置文件夹前缀，默认为jar路径下的result_*文件夹");
        }
        //clear(); // 清楚上次运行产生的所有数据内容
        File folder_f = new File(args[0]);
        Finesse f = null;
        try {
            f =new Finesse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 配置参数
        List<String> files = getFiles(folder_f);
        assert f != null;
        f.setAverage_size(Integer.parseInt(args[1])); // 平均大小
        f.setSub_chunk_count(12); // 每个块有多少个子块
        f.setSfs_count(3); // 最终得到SF的个数

        long start = System.currentTimeMillis();
        System.out.println("========================= Start Prototype Process =========================");
        for (String file : files) {
            f.Init(file);
            f.process();
        }
        System.out.println("========================== End Prototype Process ==========================");
        long end = System.currentTimeMillis();
        System.out.println("===========================================================================");
        System.out.printf("========Process time :  %s%n", end - start);
        long before_size = MyFileUtil.getFileSize(folder_f);
        long after_size = MyFileUtil.getFileSize(new File(Properties.PROTOTYPE_RESULT_DIR));
        System.out.printf("========DCR  :  %s%n", before_size * (1.0) / after_size);
        System.out.printf("========DCE  :  %s%n", f.getDCE());
        long metadata_size = MyFileUtil.getFileSize(new File(Properties.PROTOTYPE_METADATA_DIR));
        System.out.printf("========Meta size  :  %s%n", metadata_size);
        System.out.printf("========DCR2  :  %s%n", before_size * (1.0) / (after_size + metadata_size));
        System.out.println("==========================================================================");
    }
}
