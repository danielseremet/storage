package s.service;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
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
import s.domain.StorageFile;
import s.repository.StorageFileRepository;

import java.io.ByteArrayOutputStream;
import java.util.List;


@Service
class ExportStorageInfoXlsx implements ExportStorageFiles {

    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfoXlsx.class);

    private final StorageFileRepository storageFileRepository;
    private final UserService userService;

    public ExportStorageInfoXlsx(StorageFileRepository storageFileRepository1, UserService userService) {

        this.storageFileRepository = storageFileRepository1;
        this.userService = userService;
    }

    public Mono<List<StorageFile>> getStorageFiles() {
        return userService.getCurrentUser()
            .flatMap(user -> storageFileRepository.findByUser(user.getId())
                .collectList());

    }

    public Mono<byte[]> exportTemplateXlsx() {
        LOG.debug("SERVICE request rendered mustache template docx for current user");
        return getStorageFiles()
            .flatMap(list -> {
                try (XSSFWorkbook document = new XSSFWorkbook();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    XSSFSheet sheet = document.createSheet();

                    Row defaultRow = sheet.createRow(0);
                    String[] headers = StringUtils.substringsBetween(list.get(0).toCustomString(), ",", "=");
                    for (int i = 0; i < headers.length; i++) {
                        defaultRow.createCell(i).setCellValue(headers[i]);
                    }   

                    for (int i = 0; i < list.size(); i++) {
                        String[] values = StringUtils.substringsBetween(list.get(i).toCustomString(), "=", ",");
                        LOG.debug("adding entity for export: {}", list.get(i));
                        Row row = sheet.createRow(i + 1);
                        for (int j = 0; j < values.length; j++) {
                            row.createCell(j).setCellValue(values[j]);
                        }

                    }
                    document.write(outputStream);
                    return Mono.just(outputStream.toByteArray());
                } catch (Exception e) {
                    throw new RuntimeException(e);

                }
            });
    }


    public Mono<ResponseEntity<Resource>> export() {
        LOG.debug("SERVICE request export StorageInfo in excel for current user");
        return exportTemplateXlsx()
            .map(bytes -> new ByteArrayResource(bytes))
            .map(resource -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource)
            );
    }


}
