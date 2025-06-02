package s.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.service.dto.ExportStorageInfoXlsx;

@Service
public class ExportStorageFactory {

    public static Logger LOG = LoggerFactory.getLogger(ExportStorageFactory.class);

    private final ExportStorageInfoPdf exportStorageInfoPdf;
    private final ExportStorageInfoCsv exportStorageInfoCsv;
    private final ExportStorageInfoXlsx exportStorageInfoXlsx;


    public ExportStorageFactory( ExportStorageInfoPdf exportStorageInfoPdf, ExportStorageInfoCsv exportStorageInfoCsv, ExportStorageInfoXlsx exportStorageInfoXlsx) {
        this.exportStorageInfoPdf = exportStorageInfoPdf;
        this.exportStorageInfoCsv = exportStorageInfoCsv;
        this.exportStorageInfoXlsx = exportStorageInfoXlsx;
    }


    public Mono<ResponseEntity<Resource>> exportStorageFileInfoGeneric(String type) {
        LOG.info("exportStorageFileInfoGeneric with type: {}", type);
        return switch (type) {
            case "pdf"->  exportStorageInfoPdf.exportPdf();
            case "xlsx"-> exportStorageInfoXlsx.exportXlsx();
            case "csv"->  exportStorageInfoCsv.exportCsv();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }










}
