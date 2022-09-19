/**
 * ClassName TestBigChunkV4
 * Description
 * Author Ymkal
 * Date  11/24/2020
 */
public class
TestBigChunkV4 {
   /* @Test
    public void test_record_position() throws IOException {
        BigChunkV4 bc = BigChunkV4.getInstance();
        BigChunkV4.setChunk_hash_file_path("sfs.txt");
        //BigChunkV4.setFine_grit_sf_path("fine_grit_sf_file.txt");
        //BigChunkV4.setCoarse_grain_sf_path("coarse_grain_sf_file.txt");


        bc.Init("resource/files/test2.txt");
        bc.findRedundancyChunkPosition();

        System.out.println(bc.getFine_grit_sign());
        System.out.println(bc.getRedundancy_sign());
        System.out.println(bc.breakpointlist.size());
        bc.processFinesse();
    }

    @Test
    public void test_randomaccessfile() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("output.txt","r");
        raf.seek(6);
        byte[] data = new byte[8];
        raf.read(data);
        System.out.println(new String(data));
    }

    @Test
    public void test_diff() throws IOException {
        byte[] dictionary = "hello,what's your name? so what?".getBytes();
        byte[] uncompressedData = "hello,what's your name? so what?.".getBytes();
        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();

        VCDiffEncoder<OutputStream> encoder = VCDiffEncoderBuilder.builder()
                .withDictionary(dictionary)
                .buildSimple();

        encoder.encode(uncompressedData, compressedData);
        byte[] bytes = compressedData.toByteArray();
        System.out.println(bytes.length);
    }

    @Test
    public void test_jvdiff() throws VcdiffEncodeException, IOException, VcdiffDecodeException {
        byte[] dictionary = "hello,what's your name? so what?".getBytes();
        byte[] uncompressedData = "hello,what's your name? so what?. i sey you".getBytes();
        int diff_length = VCdiff.get_diff_length(dictionary, uncompressedData);
        System.out.println(uncompressedData.length);
        System.out.println(diff_length);
    }

    @Test
    public void test_finesse() throws IOException {
        Finesse f = Finesse.getInstance();
        f.Init("resource/out.txt");
        f.process();
    }

    @Test
    public void test_create_file() throws IOException {
        String fp = "test/t/ds.txt";
        File f = new File(fp);
        if(f.getParentFile().exists()){
            f.createNewFile();
        }else {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }

    }*/
}
