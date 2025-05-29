package s.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Storage Service.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {


    // jhipster-needle-application-properties-property
    private MinioConfig minio = new MinioConfig();

    public MinioConfig getMinio() {
        return minio;
    }

    // jhipster-needle-application-properties-property-getter

    public static class MinioConfig {
        private String url;
        private String accessKey;
        private String secretKey;
        private String bucket;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
