package net.tylerwade.videostreaming.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.tylerwade.videostreaming.model.Content;
import net.tylerwade.videostreaming.model.ContentRequest;
import net.tylerwade.videostreaming.model.Range;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LocalContentAdapter implements ContentAdapter {

	private static final String VIDEO_PATH = "classpath:videos";
	private static final long MAX_CHUNK_SIZE = 1024 * 1024L;

	private final ResourceLoader resourceLoader;

	@Override
	public Content loadContent(ContentRequest contentRequest) throws IOException {
		Resource videoResource = loadResource(contentRequest);

		String fileName = contentRequest.getFileName();
		String contentType = MediaTypeFactory.getMediaType(videoResource)
				.map(MediaType::toString)
				.orElse("application/octet-stream");
		long fileSize = videoResource.contentLength();

		// Get validated range from helper
		Range validatedRange = validateRange(contentRequest.getRange(), fileSize);
		Long start = validatedRange.start();
		Long end = validatedRange.end();

		Long contentLength = end - start + 1;

		StreamingResponseBody content = readContent(videoResource, start, end, contentLength);

		return Content.builder()
				.fileName(fileName)
				.contentType(contentType)
				.fileSize(fileSize)
				.content(content)
				.contentLength(contentLength)
				.range(new Range(start, end))
				.build();
	}

	private Range validateRange(Range requestedRange, Long fileSize) {
		Long start = requestedRange.start() == null ? 0L : requestedRange.start();
		Long end = requestedRange.end();

		if (end == null || end - start + 1 > MAX_CHUNK_SIZE) {
			end = Math.min(start + MAX_CHUNK_SIZE - 1, fileSize - 1);
		}

		return new Range(start, end);
	}

	private StreamingResponseBody readContent(Resource videoResource, Long start, Long end, Long contentLength) {
		return outputStream -> {
			try (InputStream is = videoResource.getInputStream()) {
				long skipped = is.skip(start);
				if (skipped < start) {
					throw new IOException("Could not skip to desired start position.");
				}

				byte[] buffer = new byte[8192]; // 8KB internal buffer
				long bytesToRead = contentLength;
				int read;

				while (bytesToRead > 0 && (read = is.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead))) != -1) {
					outputStream.write(buffer, 0, read);
					bytesToRead -= read;
				}
				outputStream.flush();
			}
		};
	}

	@Override
	public Long getContentSize(ContentRequest contentRequest) throws IOException {
		Resource videoResource = loadResource(contentRequest);
		return videoResource.contentLength();
	}

	@Override
	public List<Content> getAllContentsMetadata() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(VIDEO_PATH + "/*");

		return Arrays.stream(resources)
				.map(resource -> {
					if (resource.getFilename() == null || !resource.getFilename().contains(".")) {
						log.warn("{} is not a valid resource.", resource.getFilename());
						return null;
					}

					String[] fileNameSplit = resource.getFilename().split("\\.");
					String contentType = fileNameSplit[fileNameSplit.length - 1];

					try {
						return new Content(
								resource.getFilename(),
								contentType,
								resource.contentLength(),
								null,
								null,
								new Range(0L, resource.contentLength() - 1)
						);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).toList();
	}

	private Resource loadResource(ContentRequest contentRequest) {
		return resourceLoader.getResource(VIDEO_PATH + "/" + contentRequest.getFileName());
	}
}