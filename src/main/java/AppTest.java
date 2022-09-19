
import utils.Properties;

import java.io.*;
import java.util.Objects;

/**
 * ClassName apptest
 * Description
 * Author Ka1HuangZhe
 * Date  10/12/2020
 */
public class AppTest {
    public static void main(String[] args) {
        clear_our_method_finesse();
    }
    public static void clear_nsf() throws IOException {
        File f1 = new File(Properties.NSF_SF_PATH);
        f1.deleteOnExit();
        deleteFile(new File(Properties.NSF_RESULT_DIR));
        deleteFile(new File(Properties.NSF_METADATA_DIR));
    }

    public static void clear_finesse() throws IOException {
        File f1 = new File(Properties.PROTOTYPE_SF_PATH);
        f1.deleteOnExit();
        deleteFile(new File(Properties.PROTOTYPE_METADATA_DIR));
        deleteFile(new File(Properties.PROTOTYPE_RESULT_DIR));
    }

    public static void clear_our_method_nsf(){
        File f1 = new File(Properties.N_CHUNK_HASH_FILE_PATH);
        File f2 = new File(Properties.N_FINE_GRIT_SF_PATH);
        File f3 = new File(Properties.N_COARSE_GRAIN_SF_PATH);

        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        deleteFile(new File(Properties.N_RESULT_DIR));
        deleteFile(new File(Properties.N_METADATA_DIR));
    }

    public static void clear_our_method_finesse(){
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
}
