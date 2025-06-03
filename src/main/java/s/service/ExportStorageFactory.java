package s.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExportStorageFactory {

    private static Logger LOG = LoggerFactory.getLogger(ExportStorageFactory.class);

    private final ExportStorageInfoPdf exportStorageInfoPdf;
    private final ExportStorageInfoCsv exportStorageInfoCsv;
    private final ExportStorageInfoXlsx exportStorageInfoXlsx;

    public ExportStorageFactory(ExportStorageInfoPdf exportStorageInfoPdf, ExportStorageInfoCsv exportStorageInfoCsv,
                                 ExportStorageInfoXlsx exportStorageInfoXlsx) {
        this.exportStorageInfoPdf = exportStorageInfoPdf;
        this.exportStorageInfoCsv = exportStorageInfoCsv;
        this.exportStorageInfoXlsx = exportStorageInfoXlsx;
    }

    public Mono<Map<ExportTypes,ExportStorageFiles>> getAllExportTypes() {
        LOG.debug("Service request to get all export types");
        Map<ExportTypes,ExportStorageFiles> exportFiles = new HashMap<>();
        exportFiles.put(ExportTypes.csv, exportStorageInfoCsv);
        exportFiles.put(ExportTypes.pdf, exportStorageInfoPdf);
        exportFiles.put(ExportTypes.xlsx, exportStorageInfoXlsx);
        return Mono.just(exportFiles);
    }


}
