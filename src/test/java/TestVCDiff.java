/*import com.davidehrmann.vcdiff.VCDiffDecoder;
import com.davidehrmann.vcdiff.VCDiffDecoderBuilder;
import com.davidehrmann.vcdiff.VCDiffEncoder;
import com.davidehrmann.vcdiff.VCDiffEncoderBuilder;
import com.davidehrmann.vcdiff.io.VCDiffOutputStream;*/
import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import java.io.*;

/**
 * ClassName TestVCDiff
 * Description
 * Author Ka1HuangZhe
 * Date  10/10/2020
 */
public class TestVCDiff {
    public static void main(String[] args) throws IOException, VcdiffEncodeException, VcdiffDecodeException {
        byte[] dictionary = "what's your name, This is the most valuable gift for me.I don't want to be a million man, I only want you. But you are not by my side...".getBytes();
        byte[] uncompressedData = "what's your name, This is the most valuable gift for me.I don't want to be a million man, I only want you. But you are not by my side...".getBytes();

        File resource = new File("resource.txt");
        OutputStream os = new FileOutputStream(resource);
        os.write(dictionary);
        os.flush();

        File targetFile = new File("target.txt");
        OutputStream os2 = new FileOutputStream(targetFile);
        os2.write(uncompressedData);
        os2.flush();

        File file = new File("c:\\test.txt");
        if(file.delete()){
            System.out.println(file.getName() + " 文件已被删除！");
        }else{
            System.out.println("文件删除失败！");
        }
        VcdiffEncoder.encode(resource,targetFile,new File("diff.txt"));
        VcdiffDecoder.decode(resource, new File("diff.txt"), new File("decode.txt"));
    }

    /**
     * use vcdiff get the diff file length
     * @param dictionary resource file
     * @param uncompressedData target file
     * @return diff length (target file length - diff file length)
     */
    public static int get_diff_length(byte[] dictionary, byte[] uncompressedData) throws IOException, VcdiffEncodeException, VcdiffDecodeException {
        File resource = new File("resource.txt");
        OutputStream os = new FileOutputStream(resource);
        os.write(dictionary);
        os.flush();

        File targetFile = new File("target.txt");
        OutputStream os2 = new FileOutputStream(targetFile);
        os2.write(uncompressedData);
        os2.flush();

        File diffFile = new File("diff.txt");
        VcdiffEncoder.encode(resource,targetFile,diffFile);
        int result =  (uncompressedData.length - diffFile.length()) > 0 ? (int) diffFile.length() :uncompressedData.length;

        // clear file
        removeFile(resource);
        removeFile(targetFile);
        removeFile(diffFile);

        return result;
    }

    private static void removeFile(File f){
        if(f.delete()){
        }else{
        }
    }

    public static int length(byte[] array){
        int count = 0;
        for (int i = array.length - 1; i >= 0; i--) {
            if(array[i] == 0){
                count ++;
            }else {
                break;
            }
        }
        return array.length - count;
    }
}
