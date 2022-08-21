package com.mc.spring.actual.combat.utils;

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author macheng
 * @date 2022/6/28 9:56
 */
public class FileUtils {


    public static void writeString(String content, String fileName) throws IOException {


        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new FileException("FILE %s DELETE FAILED",fileName);
            }
        }
        if (file.createNewFile()) {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try (fileWriter; bufferedWriter) {
                bufferedWriter.write(content);
                bufferedWriter.flush();
            }
        }


    }
    public static void writeString(List<String> contents, String fileName) throws IOException {


        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new FileException("FILE %s DELETE FAILED",fileName);
            }
        }
        if (file.createNewFile()) {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            try (fileWriter; bufferedWriter) {
                for (String content : contents) {
                    bufferedWriter.write(content);
                }
                bufferedWriter.flush();
            }
        }


    }
    static class FileException extends RuntimeException {
        public FileException(String message,String param) {
            super(message.formatted(param));
        }

        public FileException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public static List<String> getFileString(String fileName, Predicate<String> filter , Function<String, String> map) throws IOException {
        // list store return sql
        ArrayList<@Nullable String> list = Lists.newArrayList();
        // add widonw path
        if (File.separator.equals("\\")) {
            fileName = FileUtils.class.getClassLoader().getResource("").getPath() + fileName;
        }
        File file = new File(fileName);
        // check file exists
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            System.exit(-1);
        }
        // read file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            // ignore empty line and comment line
           if (filter.test(line)){
               list.add(map.apply(line));
           }

        }
        return list;

    }

    public static List<String> getFileString(String fileName) throws IOException {
        // list store return sql
        ArrayList<@Nullable String> list = Lists.newArrayList();
        // add widonw path
        if (File.separator.equals("\\")) {
            fileName = FileUtils.class.getClassLoader().getResource("").getPath() + fileName;
        }
        File file = new File(fileName);
        // check file exists
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            System.exit(-1);
        }
        // read file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            // ignore empty line and comment line
                list.add(line);

        }
        return list;

    }

    public static String getFileSingleString(String fileName) throws IOException {
        // list store return sql
        StringBuilder sb=new StringBuilder();
        // add widonw path
        if (File.separator.equals("\\")) {
            fileName = FileUtils.class.getClassLoader().getResource("").getPath() + fileName;
        }
        File file = new File(fileName);
        // check file exists
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            System.exit(-1);
        }
        // read file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            // ignore empty line and comment line
            sb.append(line);

        }
        return sb.toString();

    }
}
