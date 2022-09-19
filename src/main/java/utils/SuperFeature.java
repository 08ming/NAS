package utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName SuperFeature
 * Description
 * Author Ka1HuangZhe
 * Date  10/10/2020
 *
 *
 *
 */
public class SuperFeature {
    /*List<Long> hashValue;
    int total_size = 12;
    int group_size = 3;
    public SuperFeature(){
        hashValue = new ArrayList<>();
    }

    public SuperFeature(List<Long> hashValue){
        this.hashValue = hashValue;
        this.total_size = this.hashValue.size();
    }*/
    public static List<List<String>> sfs = new ArrayList<>();
    /**
     * get Super-features SF1,SF2...
     * @param group_size sfs的数量
     * @param total_size hash值的所有数量
     * @return sfs
     */
    public static List<String> getSfs(List<Long> hashValue,int total_size, int group_size) throws NoSuchAlgorithmException {

        assert hashValue.size() == 12;
        List<List<Long>> groupSorted = new ArrayList<>();
        List<Long> tmp = new ArrayList<>();
        for(int i = 0 ; i < total_size; i++){
            tmp.add(hashValue.get(i));
            if(  (i + 1) % group_size == 0){
                tmp.sort((Long::compareTo));
                groupSorted.add(tmp);
                tmp = new ArrayList<>();
            }
        }

        if(!tmp.isEmpty())
            tmp.clear();

        // extract the maximum hash value from group
        List<List<Long>> rs = new ArrayList<>();
        int per_count = total_size/group_size;
        for(int i = 0; i < group_size; i++){
            for(int j = 0; j < per_count; j++){
                Long tmp_value = groupSorted.get(j).get(i);
                tmp.add(tmp_value);
            }
            rs.add(tmp);
            tmp = new ArrayList<>();
        }

        List<String> sfs = new ArrayList<>();
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (List<Long> r : rs) {
            for (Long aLong : r) {
                md.update(("" + aLong).getBytes());
            }
            byte[] hash = md.digest();

            // recording to the cypher text, we can get a hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                if ((0xff & b) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & b)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & b));
                }
            }
            sfs.add(hexString.toString());
            md.reset();
        }
        return sfs;
    }

    public static List<String> getSfsNSF(List<Long> hashValue){
        List<String> result = new ArrayList<>();
        TTTD td = new TTTD();
        long tmp_rabin_value = 0;

        for(int i = 0; i < hashValue.size(); i ++){
            byte[] str_to_bytes = hashValue.get(i).toString().getBytes();
            for (byte str_to_byte : str_to_bytes) {
                tmp_rabin_value = td.updateHash(str_to_byte);
            }
            if( (i + 1) % 4 == 0){
                result.add(tmp_rabin_value + "");
                td = new TTTD();
            }
        }
        return result;
    }

    public static List<String> getSfs(List<Long> hashValue, int group_size) throws NoSuchAlgorithmException {

        assert hashValue != null && hashValue.size() == 12;

        List<List<Long>> groupSorted = new ArrayList<>();
        List<Long> tmp = new ArrayList<>();
        int total_size = hashValue.size();
        for(int i = 0 ; i < total_size; i++){
            tmp.add(hashValue.get(i));
            if(  (i + 1) % group_size == 0){
                tmp.sort((Long::compareTo));
                groupSorted.add(tmp);
                tmp = new ArrayList<>();
            }
        }

        if(!tmp.isEmpty())
            tmp.clear();

        // extract the maximum hash value from group
        List<List<Long>> rs = new ArrayList<>();
        int per_count = total_size/group_size;
        for(int i = 0; i < group_size; i++){
            for(int j = 0; j < per_count; j++){
                Long tmp_value = groupSorted.get(j).get(i);
                tmp.add(tmp_value);
            }
            rs.add(tmp);
            tmp = new ArrayList<>();
        }

        List<String> sfs = new ArrayList<>();
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (List<Long> r : rs) {
            for (Long aLong : r) {
                md.update(("" + aLong).getBytes());
            }
            byte[] hash = md.digest();

            // recording to the cypher text, we can get a hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                if ((0xff & b) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & b)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & b));
                }
            }
            sfs.add(hexString.toString());
            md.reset();
        }
        assert sfs.size() == 3;
        return sfs;
    }

    public static List<String> getSfs(List<Long> hashValue) throws NoSuchAlgorithmException {

        if(hashValue == null){
            return null;
        }
        int total_size = hashValue.size();
        int group_size = hashValue.size() / 4;
        List<List<Long>> groupSorted = new ArrayList<>();
        List<Long> tmp = new ArrayList<>();
        for(int i = 0 ; i < total_size; i++){
            tmp.add(hashValue.get(i));
            if(  (i + 1) % group_size == 0){
                tmp.sort((Long::compareTo));
                groupSorted.add(tmp);
                tmp = new ArrayList<>();
            }
        }
        if(!tmp.isEmpty())
            tmp.clear();

        // extract the maximum hash value from group
        List<List<Long>> rs = new ArrayList<>();
        int per_count = total_size/group_size;
        for(int i = 0; i < group_size; i++){
            for(int j = 0; j < per_count; j++){
                Long tmp_value = groupSorted.get(j).get(i);
                tmp.add(tmp_value);
            }
            rs.add(tmp);
            tmp = new ArrayList<>();
        }

        List<String> sfs = new ArrayList<>();
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (List<Long> r : rs) {
            for (Long aLong : r) {
                md.update(("" + aLong).getBytes());
            }
            byte[] hash = md.digest();

            // recording to the cypher text, we can get a hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                if ((0xff & b) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & b)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & b));
                }
            }
            sfs.add(hexString.toString());
            md.reset();
        }
        return sfs;
    }



}
