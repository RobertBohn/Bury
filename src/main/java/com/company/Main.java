package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length==1) {
            display(args[0]);
        } else if (args.length==3) {
            bury(args[0], args[1], args[2]);
            display(args[1]);
        } else {
            System.out.println("Usage: in out text");
        }
    }

    private static void bury(String inputFileName, String outputFileName, String text) throws IOException {
        File file = new File(inputFileName);
        long size = file.length();
        if (size < (text.length()+4   +1024)*8) {
            throw new InvalidParameterException("Invalid file size.");
        }

        byte[] result = new byte[(int)size];
        InputStream stream = new FileInputStream(file);
        if (stream.read(result) != size) {
            throw new IOException("Error reading file.");
        }

        saveInt(result,text.length());

        int mod = (int)size / ((text.length()+4)*8);
        for (int i = 0; i<text.length()*8; i++) {
            setBit(result, (i*mod)+32 + (1024*4), 7, getBit(text.getBytes(), i/8, i%8));
        }

        Path path = Paths.get(outputFileName);
        Files.write(path, result);
    }

    private static void display(String fileName) throws IOException {
        File file = new File(fileName);
        long size = file.length();

        byte[] fileBuffer = new byte[(int)size];
        InputStream stream = new FileInputStream(file);
        if (stream.read(fileBuffer) != size) {
            throw new IOException("Error reading file.");
        }

        byte[] textBuffer = new byte[getInt(fileBuffer)];

        int mod = (int)size / ((textBuffer.length+4)*8);
        for (int i=0; i<textBuffer.length*8; i++) {
            setBit(textBuffer, i/8, i%8, getBit(fileBuffer, (i*mod)+32 + (1024*4), 7));
        }

        System.out.println(new String(textBuffer));
    }

    private static int getBit(byte[] buffer, int off, int bit) {
        return (buffer[off] & (128 >> bit)) > 0 ? 1 : 0;
    }

    private static void setBit(byte[] buffer, int off, int bit, int value) {
        if (value == 1) {
            buffer[off] = (byte)(buffer[off] | (128 >> (bit % 8)));
        } else {
            buffer[off] = (byte)(buffer[off] & ~(128 >> (bit % 8)));
        }
    }

    private static void saveInt(byte[] buffer, int value) {
        byte[] number = new byte[4];
        for (int i=0; i<4; i++) {
            number[i] = (byte)(value % 256);
            value = value/256;
        }
        for (int i = 0; i<number.length*8; i++) {
            setBit(buffer, i + (1024*4), 7, getBit(number, i/8, i%8));
        }
    }

    private static int getInt(byte[] buffer) {
        byte[] number = new byte[4];
        for (int i=0; i<number.length*8; i++) {
            setBit(number, i/8, i%8, getBit(buffer, i + (1024*4), 7));
        }
        int result = 0;
        for (int i=3; i>=0; i--) {
            result = (result * 256) + (number[i] & 0xff);
        }
        return result;
    }
}
