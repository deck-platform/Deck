package edge.droid.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileUtils {

    public static void saveFile(MultipartFile file, String path) throws IOException {
        file.transferTo(new File(path));
    }

    public static List<String> generateFileContentByDir(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory()) {
            log.error("[generateFileContentByDir] dir={} is not a dir", dir);
            return new ArrayList<>();
        }
        File[] fileList = dirFile.listFiles();
        if (null == fileList || fileList.length == 0) {
            log.info("[generateFileContentByDir] dir={} has not sub file", dir);
            return new ArrayList<>();
        }
        return Arrays.stream(fileList)
                .map(file -> FileUtils.fileToBase64(file.getAbsolutePath()))
                .collect(Collectors.toList());
    }

    public static boolean createDirs(String pathName) {
        Path path = Paths.get(pathName);
        try {
            if (!Files.exists(path)){
                Files.createDirectories(path);
            } else {
                log.info("[createDirs] path={} exists!", pathName);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String fileToBase64(String filePath){
        if (null == filePath || filePath.equals("")) {
            log.error("[fileToByte] filePath error");
        }
        String fileContent="";
        try {
            byte[] fileByte = toByteArray(filePath);
            fileContent = byteToBase64(fileByte);
        }catch (Exception e){
            log.error("[fileToBase64] error, filePath={}", filePath, e);
        }
        return fileContent;
    }

    public static byte[] toByteArray(String filePath){
        // todo change format
        ByteArrayOutputStream bos = null;
        BufferedInputStream in = null;
        try {
            File f = new File(filePath);
            if (f.exists()) {
                in = new BufferedInputStream(new FileInputStream(f));
                bos = new ByteArrayOutputStream((int) f.length());

                int buf_size = 1024;
                byte[] buffer = new byte[buf_size];
                int len = 0;
                while (-1 != (len = in.read(buffer, 0, buf_size))) {
                    bos.write(buffer, 0, len);
                }
            }

        } catch (IOException e) {
            log.error("[toByteArray] Exception", e);
        } finally {
            try {
                in.close();
                bos.close();
            } catch (IOException e) {
                log.error("[toByteArray] Exception",e);
            }
        }
        return bos.toByteArray();
    }


    public static String byteToBase64(byte[] b) {
        String str = "";
        if (null != b) {
            Base64 base64 = new Base64();
            str = base64.encodeToString(b);
        }
        return str;
    }

    public static void writeByteArrayToFile(String path, String content) throws Exception {
        File file = new File(path);
        // todo check overwrite
        if (file.exists()) {
            log.info("[writeByteArrayToFile] path={} exists!", path);
            return;
        } else {
            boolean createResult = file.createNewFile();
            if (!createResult) {
                log.error("[writeByteArrayToFile] path={} create error", path);
                return;
            }
        }
        FileOutputStream os = new FileOutputStream(path);
        Base64 base64 = new Base64();
        byte[] bytes = base64.decode(content);
        os.write(bytes);
        os.close();
    }
}