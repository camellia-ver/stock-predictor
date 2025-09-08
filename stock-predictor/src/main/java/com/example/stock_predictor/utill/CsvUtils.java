package com.example.stock_predictor.utill;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvUtils {

    public static boolean fileExists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    public static CSVReader openCsvReader(String filePath) throws IOException {
        return new CSVReader(new FileReader(filePath));
    }

    public static void skipHeader(CSVReader reader) throws IOException, CsvValidationException {
        reader.readNext();
    }
}
