package utils;

/**
 * ClassName Properties
 * Description
 * Author Ymkal
 * Date  1/11/2021
 */
public class Properties {
    /*public final static String CHUNK_HASH_FILE_PATH = "/home/ubuntu/Tj/result_our_method/result_sfs/sfs.txt";
    public final static String FINE_GRIT_SF_PATH = "/home/ubuntu/Tj/result_our_method/result_sfs/fine_grit_sf_file.txt";
    public final static String COARSE_GRAIN_SF_PATH = "/home/ubuntu/Tj/result_our_method/result_sfs/coarse_grain_sf_file.txt";
    public final static String METADATA_DIR = "/home/ubuntu/Tj/result_our_method/meta";
    public final static String RESULT_DIR = "/home/ubuntu/Tj/result_our_method/result_files";
    public final static String PROTOTYPE_RESULT_DIR = "/home/ubuntu/Tj/result_finesse/result_files";
    public final static String PROTOTYPE_SF_PATH = "/home/ubuntu/Tj/result_finesse/result_sfs/sfs.txt";
    public final static String PROTOTYPE_METADATA_DIR = "/home/ubuntu/Tj/result_finesse/meta";*/
    // Opt Finesse
    public static String CHUNK_HASH_FILE_PATH = "result_our_method/result_sfs/sfs.txt";
    public static String FINE_GRIT_SF_PATH = "result_our_method/result_sfs/fine_grit_sf_file.txt";
    public static String COARSE_GRAIN_SF_PATH = "result_our_method/result_sfs/coarse_grain_sf_file.txt";
    public static String METADATA_DIR = "result_our_method/meta";
    public static String RESULT_DIR = "result_our_method/result_files";
    // Opt ResemblanceDetection
    public static String N_CHUNK_HASH_FILE_PATH = "n_result_our_method/result_sfs/sfs.txt";
    public static String N_FINE_GRIT_SF_PATH = "n_result_our_method/result_sfs/fine_grit_sf_file.txt";
    public static String N_COARSE_GRAIN_SF_PATH = "n_result_our_method/result_sfs/coarse_grain_sf_file.txt";
    public static String N_METADATA_DIR = "n_result_our_method/meta";
    public static String N_RESULT_DIR = "n_result_our_method/result_files";
    // Finesse
    public static String PROTOTYPE_RESULT_DIR = "result_files";
    public static String PROTOTYPE_SF_PATH = "result_sfs/sfs.txt";
    public static String PROTOTYPE_METADATA_DIR = "meta";
    // ResemblanceDetection
    public static String NSF_RESULT_DIR = "result_nsf/result_files";
    public static String NSF_SF_PATH = "result_nsf/result_sfs/sfs.txt";
    public static String NSF_METADATA_DIR = "result_nsf/meta";
    public static String BLOCK_ALL_HASH_DIR = "block_all_hash";
    public static void Reset(String prefix){
        if (!prefix.endsWith("/")){
            prefix += "/";
        }
        CHUNK_HASH_FILE_PATH = prefix + CHUNK_HASH_FILE_PATH;
        FINE_GRIT_SF_PATH = prefix + FINE_GRIT_SF_PATH;
        COARSE_GRAIN_SF_PATH = prefix + COARSE_GRAIN_SF_PATH;
        METADATA_DIR  = prefix + METADATA_DIR;
        RESULT_DIR = prefix + RESULT_DIR;

        N_CHUNK_HASH_FILE_PATH = prefix + N_CHUNK_HASH_FILE_PATH;
        N_FINE_GRIT_SF_PATH = prefix + N_FINE_GRIT_SF_PATH;
        N_COARSE_GRAIN_SF_PATH = prefix + N_COARSE_GRAIN_SF_PATH;
        N_METADATA_DIR  = prefix + N_METADATA_DIR;
        N_RESULT_DIR = prefix + N_RESULT_DIR;

        PROTOTYPE_RESULT_DIR = prefix + PROTOTYPE_RESULT_DIR;
        PROTOTYPE_SF_PATH  = prefix + PROTOTYPE_SF_PATH;
        PROTOTYPE_METADATA_DIR = prefix + PROTOTYPE_METADATA_DIR;

        NSF_RESULT_DIR  = prefix + NSF_RESULT_DIR;
        NSF_SF_PATH = prefix + NSF_SF_PATH;
        NSF_METADATA_DIR = prefix + NSF_METADATA_DIR;

        BLOCK_ALL_HASH_DIR = prefix + BLOCK_ALL_HASH_DIR;
    }
}
