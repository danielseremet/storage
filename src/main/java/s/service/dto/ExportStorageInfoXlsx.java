package s.service.dto;

import com.google.common.net.HttpHeaders;
import com.samskivert.mustache.Mustache;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.repository.StorageFileRepository;
import s.service.ExportStorageInfo;
import s.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;


@Service
public class ExportStorageInfoXlsx extends ExportStorageInfo {

    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfo.class);

    public ExportStorageInfoXlsx(UserService userService, StorageFileRepository storageFileRepository, Mustache.Compiler mustacheCompiler) {
        super(userService, storageFileRepository, mustacheCompiler);
    }

    public Mono<byte[]> exportMustacheTemplateXlsx() {
        LOG.debug("SERVICE request rendered mustache template docx for current user");
        return renderedMustacheTemplate()
            .map(html -> {
                try(XSSFWorkbook document = new XSSFWorkbook();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    XSSFSheet sheet = document.createSheet("report") ;

                    Document doc = Jsoup.parse(html);
                    Elements rows = doc.select( "tr");

                    int rowIndex = 0;
                    for(Element row : rows) {
                        Row excelRow = sheet.createRow(rowIndex++);
                        Elements cells = row.select("td");
                        for (int i = 0; i < cells.size(); i++) {
                            sheet.autoSizeColumn(i);
                            excelRow.createCell(i).setCellValue(cells.get(i).text());
                        }
                    }
                    document.write(outputStream);
                    return outputStream.toByteArray();


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public Mono<ResponseEntity<Resource>> exportXlsx() {
        LOG.debug("SERVICE request export StorageInfo in excel for current user");
        return exportMustacheTemplateXlsx()
            .map(bytes -> new ByteArrayResource(bytes))
            .map(resource -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"report.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource)
            );
    }


}
