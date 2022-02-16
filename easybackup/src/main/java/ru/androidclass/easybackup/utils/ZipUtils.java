package ru.androidclass.easybackup.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final String TAG = ZipUtils.class.getSimpleName();

    public static void zipFolder(File srcFolder, File destZipFile) throws IOException {
        try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
             ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

            addFolderToZip(srcFolder, srcFolder, zip);
        }
    }

    private static void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws IOException {

        if (srcFile.isDirectory()) {
            addFolderToZip(rootPath, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                String name = srcFile.getPath();
                name = name.replace(rootPath.getPath(), "");
                Log.d(TAG, "Zip " + srcFile + " to " + name);
                zip.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }

    private static void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws IOException {
        for (File fileName : srcFolder.listFiles()) {
            addFileToZip(rootPath, fileName, zip);
        }
    }

    public static void unZip(File from, String destPath) throws IOException {
        InputStream is;
        ZipInputStream zis;
        String filename;
        is = new FileInputStream(from);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();
            // Need to create directories if not exists, or
            // it will generate an Exception...
            if (ze.isDirectory()) {
                File fmd = new File(destPath + filename);
                fmd.mkdirs();
                continue;
            }
            FileOutputStream fout = new FileOutputStream(destPath + filename);
            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }
            fout.close();
            zis.closeEntry();
        }
        zis.close();
    }

    public static String md5(@NonNull String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] bytesOfMessage = str.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] theMD5digest = md.digest(bytesOfMessage);
        return bytesToHex(theMD5digest);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
