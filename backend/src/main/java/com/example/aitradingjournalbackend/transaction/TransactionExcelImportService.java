package com.example.aitradingjournalbackend.transaction;

import com.example.aitradingjournalbackend.transaction.repo.TransactionRepository;
import com.example.aitradingjournalbackend.user.AppUser;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TransactionExcelImportService {

    private static final List<String> REQUIRED_COLUMNS = List.of(
        "position",
        "symbol",
        "type",
        "volume",
        "open time",
        "open price",
        "close time",
        "close price",
        "gross p/l"
    );

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[] {
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
    };

    private final TransactionRepository transactionRepository;

    @Transactional
    public int importExcel(MultipartFile file, AppUser user) {
        validateFile(file);

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Excel file does not contain any sheets");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = findHeaderRow(sheet);
            if (headerRow == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Excel file does not contain a recognizable header row"
                );
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> columns = readColumnIndexes(headerRow, formatter);
            ensureRequiredColumns(columns);

            List<Transaction> transactions = new ArrayList<>();
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }
                transactions.add(parseTransaction(row, columns, formatter, user, rowIndex + 1));
            }

            transactionRepository.saveAll(transactions);
            return transactions.size();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read Excel file", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Excel file is required");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only .xlsx files are supported");
        }
    }

    private Map<String, Integer> readColumnIndexes(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> columns = new HashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            String header = normalizeHeader(formatter.formatCellValue(headerRow.getCell(cellIndex)));
            if (StringUtils.hasText(header)) {
                columns.put(header, cellIndex);
            }
        }
        return columns;
    }

    private Row findHeaderRow(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        int maxRowToScan = Math.min(sheet.getLastRowNum(), 100);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= maxRowToScan; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            Map<String, Integer> columns = readColumnIndexes(row, formatter);
            if (REQUIRED_COLUMNS.stream().allMatch(columns::containsKey)) {
                return row;
            }
        }
        return null;
    }

    private void ensureRequiredColumns(Map<String, Integer> columns) {
        List<String> missing = REQUIRED_COLUMNS.stream()
            .filter(required -> !columns.containsKey(required))
            .toList();
        if (!missing.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing required Excel columns: " + String.join(", ", missing)
            );
        }
    }

    private Transaction parseTransaction(Row row,
                                         Map<String, Integer> columns,
                                         DataFormatter formatter,
                                         AppUser user,
                                         int excelRowNumber) {
        try {
            return new Transaction(
                user,
                requiredString(row, columns, "position", formatter),
                requiredString(row, columns, "symbol", formatter),
                requiredString(row, columns, "type", formatter),
                requiredBigDecimal(row, columns, "volume", formatter),
                requiredInstant(row, columns, "open time", formatter),
                requiredBigDecimal(row, columns, "open price", formatter),
                requiredInstant(row, columns, "close time", formatter),
                requiredBigDecimal(row, columns, "close price", formatter),
                optionalBigDecimal(row, columns, "sl", formatter),
                optionalBigDecimal(row, columns, "tp", formatter),
                optionalBigDecimal(row, columns, "margin", formatter),
                optionalBigDecimal(row, columns, "commission", formatter),
                optionalBigDecimal(row, columns, "swap", formatter),
                optionalBigDecimal(row, columns, "rollover", formatter),
                requiredBigDecimal(row, columns, "gross p/l", formatter),
                optionalString(row, columns, "comment", formatter)
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid data in Excel row " + excelRowNumber + ": " + ex.getMessage()
            );
        }
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }
            if (StringUtils.hasText(formatter.formatCellValue(row.getCell(cellIndex)))) {
                return false;
            }
        }
        return true;
    }

    private String requiredString(Row row, Map<String, Integer> columns, String column, DataFormatter formatter) {
        String value = optionalString(row, columns, column, formatter);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("column '" + column + "' is required");
        }
        return value;
    }

    private String optionalString(Row row, Map<String, Integer> columns, String column, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex(columns, column));
        String value = formatter.formatCellValue(cell).trim();
        return StringUtils.hasText(value) ? value : null;
    }

    private BigDecimal requiredBigDecimal(Row row,
                                          Map<String, Integer> columns,
                                          String column,
                                          DataFormatter formatter) {
        BigDecimal value = optionalBigDecimal(row, columns, column, formatter);
        if (value == null) {
            throw new IllegalArgumentException("column '" + column + "' is required");
        }
        return value;
    }

    private BigDecimal optionalBigDecimal(Row row,
                                          Map<String, Integer> columns,
                                          String column,
                                          DataFormatter formatter) {
        Integer columnIndex = columns.get(column);
        if (columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        String value = formatter.formatCellValue(cell).trim();
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String normalized = value.replace(" ", "").replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("column '" + column + "' has invalid number");
        }
    }

    private Instant requiredInstant(Row row, Map<String, Integer> columns, String column, DataFormatter formatter) {
        Instant value = optionalInstant(row, columns, column, formatter);
        if (value == null) {
            throw new IllegalArgumentException("column '" + column + "' is required");
        }
        return value;
    }

    private Instant optionalInstant(Row row, Map<String, Integer> columns, String column, DataFormatter formatter) {
        Integer columnIndex = columns.get(column);
        if (columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toInstant(ZoneOffset.UTC);
        }

        String value = formatter.formatCellValue(cell).trim();
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String normalizedWhitespaceValue = value.replace('\u00A0', ' ').trim();
        BigDecimal excelSerialDate = parseExcelSerialDate(normalizedWhitespaceValue);
        if (excelSerialDate != null) {
            return DateUtil.getLocalDateTime(excelSerialDate.doubleValue()).toInstant(ZoneOffset.UTC);
        }

        for (DateTimeFormatter dateFormatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(normalizedWhitespaceValue, dateFormatter).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        try {
            return Instant.parse(normalizedWhitespaceValue);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("column '" + column + "' has invalid date/time");
        }
    }

    private int columnIndex(Map<String, Integer> columns, String column) {
        Integer index = columns.get(column);
        if (index == null) {
            throw new IllegalArgumentException("column '" + column + "' is missing");
        }
        return index;
    }

    private String normalizeHeader(String header) {
        return header == null
            ? ""
            : header.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private BigDecimal parseExcelSerialDate(String value) {
        String normalized = value.replace(" ", "").replace(",", ".");
        if (!normalized.matches("\\d+(\\.\\d+)?")) {
            return null;
        }

        try {
            BigDecimal serial = new BigDecimal(normalized);
            if (serial.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return serial;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
