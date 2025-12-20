package net.tylerwade.videostreaming.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.tylerwade.videostreaming.model.Content;
import net.tylerwade.videostreaming.model.ContentRequest;
import net.tylerwade.videostreaming.model.Range;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class S3ContentAdapter implements ContentAdapter {

	private final S3Client s3Client;
	private final String bucketName;

	private static final long MAX_CHUNK_SIZE = 1024 * 1024L; // 1MB

	@Override
	public Content loadContent(ContentRequest contentRequest) throws IOException {
		// 1. Get total file size.
		long fileSize = getContentSize(contentRequest);

		// 2. Validate/Calculate Range.
		Range validatedRange = validateRange(contentRequest.getRange(), fileSize);
		Long contentLength = validatedRange.end() - validatedRange.start() + 1;

		// 3. Stream content
		StreamingResponseBody streamingResponseBody = streamContent(contentRequest.getFileName(), validatedRange);

		// 4. Build content object
		return Content.builder()
				.fileName(contentRequest.getFileName())
				.contentType(extractContentType(contentRequest.getFileName()))
				.fileSize(fileSize)
				.content(streamingResponseBody)
				.contentLength(contentLength)
				.range(validatedRange)
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

	private StreamingResponseBody streamContent(String objectKey, Range range) {
		String rangeHeader = String.format("bytes=%d-%d", range.start(), range.end());

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.range(rangeHeader)
				.build();

		log.debug("Streaming S3 Object '{}'. {}", objectKey, rangeHeader);

		return outputStream -> {
			try (InputStream is = s3Client.getObject(getObjectRequest)) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
			}
		};
	}

	@Override
	public Long getContentSize(ContentRequest contentRequest) throws IOException {
		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
				.bucket(bucketName)
				.key(contentRequest.getFileName())
				.build();

		return s3Client.headObject(headObjectRequest).contentLength();
	}

	@Override
	public List<Content> getAllContentsMetadata() throws IOException {
		ListObjectsV2Response objectsResponse = s3Client.listObjectsV2(ListObjectsV2Request.builder()
				.bucket(bucketName)
				.prefix("")
				.build());

		return objectsResponse.contents().stream()
				// Ignore folders and objects without "."
				.filter(object -> !object.key().endsWith("/") || !object.key().contains("."))
				.map(object ->
						Content.builder()
								.fileName(object.key())
								.contentType(extractContentType(object.key()))
								.fileSize(object.size())
								.range(new Range(0L, object.size() - 1))
								.build()
				).toList();
	}

	private static String extractContentType(String fileName) {
		if (fileName.contains(".")) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
			return "video/" + extension;
		}
		return "application/octet-stream";
	}
}