package net.tylerwade.videostreaming.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Content {

	private String fileName;
	private String contentType;
	private Long contentLength;
	private byte[] content;
	private Long start;
	private Long end;

}