package net.tylerwade.videostreaming.config;

import lombok.RequiredArgsConstructor;
import net.tylerwade.videostreaming.adapter.ContentAdapter;
import net.tylerwade.videostreaming.adapter.LocalContentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
public class AdapterConfig {

	private final ResourceLoader resourceLoader;

	@Bean
	public ContentAdapter contentAdapter() {
		return new LocalContentAdapter(resourceLoader);
	}

}