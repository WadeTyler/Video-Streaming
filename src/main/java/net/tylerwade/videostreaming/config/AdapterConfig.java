package net.tylerwade.videostreaming.config;

import lombok.RequiredArgsConstructor;
import net.tylerwade.videostreaming.adapter.ContentAdapter;
import net.tylerwade.videostreaming.adapter.S3ContentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class AdapterConfig {

	private final ResourceLoader resourceLoader;
	private final S3Client s3Client;

	private static final String bucketName = "tw-video-streaming-test-bucket";

	@Bean
	public ContentAdapter contentAdapter() {
		return new S3ContentAdapter(s3Client, bucketName);
	}

}