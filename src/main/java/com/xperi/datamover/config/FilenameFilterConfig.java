package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** This class contains read properties with prefix "filename-filter".
 * It configures file-name ignore list for digestion */
@Configuration
@ConfigurationProperties(prefix = "filename-filter")
@Data
public class FilenameFilterConfig {
    /** file name exclusion list */
    private List<String> ignoreListRegex;
}
