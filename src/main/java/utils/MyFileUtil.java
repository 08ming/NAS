package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * ClassName MyFileUtil
 * Description
 * Author Ymkal
 * Date  1/17/2021
 */
public class MyFileUtil {
    public static long getFileSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                size += getFileSize(file);
            }
        } else {
            size += f.length();
        }
        return size;
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

    public static void copyDir(String old_dir, String new_dir){
        File old_dir_f = new File(old_dir);
        File[] old_files_f = old_dir_f.listFiles();
        assert old_files_f != null;
        for (File file : old_files_f) {
            Path new_file_fp = Paths.get(new_dir,file.getName());
            try {
                Files.copy(file.toPath(),new_file_fp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
