package s.service.dto;

import java.io.ByteArrayInputStream;

public record FileDTO(double size, String name, String content, ByteArrayInputStream inputStream,Long sizeInBytes) {

}
