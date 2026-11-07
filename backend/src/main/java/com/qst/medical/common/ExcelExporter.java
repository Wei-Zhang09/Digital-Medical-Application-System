package com.qst.medical.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 极简 Excel 导出：只做「表头 + 若干行文本」这一件事。
 *
 * <p>不引入 EasyExcel 的注解模型，因为各模块导出的列差异大、也只需要文本，
 * 直接把前端表格里看到的列按顺序塞进来即可。</p>
 */
public final class ExcelExporter {

    private ExcelExporter() {
    }

    /**
     * 写出 .xlsx 到响应流。
     *
     * @param response 响应
     * @param fileName 文件名（不含扩展名，中文会做 URL 编码）
     * @param sheetName 工作表名
     * @param headers  表头
     * @param rows     数据行，每行长度应与表头一致
     */
    public static void write(HttpServletResponse response, String fileName, String sheetName,
                             String[] headers, List<String[]> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName == null ? "Sheet1" : sheetName);

            // 表头样式：加粗、居中、浅底色
            CellStyle headStyle = workbook.createCellStyle();
            Font headFont = workbook.createFont();
            headFont.setBold(true);
            headStyle.setFont(headFont);
            headStyle.setAlignment(HorizontalAlignment.CENTER);
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle bodyStyle = workbook.createCellStyle();
            bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bodyStyle.setWrapText(true);

            Row head = sheet.createRow(0);
            head.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                String[] data = rows.get(r);
                for (int c = 0; c < headers.length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(c < data.length && data[c] != null ? data[c] : "");
                    cell.setCellStyle(bodyStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }
            sheet.createFreezePane(0, 1);

            String encoded = URLEncoder.encode(fileName + ".xlsx", "UTF-8").replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);

            try (OutputStream out = response.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }
        }
    }

    /** null 安全地转字符串 */
    public static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
