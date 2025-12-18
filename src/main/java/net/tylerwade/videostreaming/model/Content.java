package net.tylerwade.videostreaming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Content {

	private String fileName;
	private String contentType;
	private Long fileSize;
	private StreamingResponseBody content;
	private Long contentLength;
	private Range range;

}