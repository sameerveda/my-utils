package sam.io.serilizers;

import static java.nio.charset.CodingErrorAction.REPORT;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static sam.io.IOConstants.defaultCharset;
import static sam.io.IOConstants.defaultOnMalformedInput;
import static sam.io.IOConstants.defaultOnUnmappableCharacter;
import static sam.io.serilizers.Utils.write;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

import sam.logging.MyLoggerFactory;
public class StringWriter2 {
	private static final Logger LOGGER = MyLoggerFactory.logger(StringWriter2.class);
	private static final Charset DEFAULT_CHARSET = defaultCharset();

	public StringWriter2() {}

	public static WriterConfig writer() {
		return new WriterConfig();
	}
	public static class WriterConfig {
		Object target;
		Charset charset;

		CodingErrorAction onMalformedInput;
		CodingErrorAction onUnmappableCharacter;

		boolean append;
		
		private WriterConfig() {}

		public WriterConfig target(WritableByteChannel target){ this.target=target;  return this; }
		public WriterConfig target(OutputStream target){ this.target=Channels.newChannel(target);  return this; }
		public WriterConfig target(Path target, boolean append){ 
			this.target= target;
			this.append = append;
			return this; 
		}
		public WriterConfig target(File target, boolean append){ 
			this.target= target.toPath();
			this.append = append;
			return this; 
		}
		public WriterConfig charset(Charset charset){ this.charset=charset;  return this; } 
		public WriterConfig onMalformedInput(CodingErrorAction onMalformedInput){ this.onMalformedInput=onMalformedInput;  return this; }
		public WriterConfig onUnmappableCharacter(CodingErrorAction onUnmappableCharacter){ this.onUnmappableCharacter=onUnmappableCharacter;  return this; }

		public void write(CharSequence data) throws IOException {
			Objects.requireNonNull(target, "target not set");
			
			if(target instanceof WritableByteChannel) {
				writeText((WritableByteChannel)target, data, this);
			} else {
				try(FileChannel fc = FileChannel.open((Path)target, CREATE,WRITE, append ? APPEND : TRUNCATE_EXISTING)) {
					writeText(fc, data, this);
				}
			}
		}
		
		private CharsetEncoder encoder() {
			return charset()
					.newEncoder()
					.onMalformedInput(onMalformedInput == null ? defaultOnMalformedInput() : onMalformedInput)
					.onUnmappableCharacter(onUnmappableCharacter == null ? defaultOnUnmappableCharacter() : onUnmappableCharacter);
		}
		public Charset charset() {
			return (charset == null ? DEFAULT_CHARSET : charset);
		}
	} 
	public static void setText(Path path, CharSequence s, String charset) throws IOException {
		setText(path, s, Charset.forName(charset));
	}
	public static void setText(Path path, CharSequence s) throws IOException {
		setText(path, s, DEFAULT_CHARSET);
	}
	public static void setText(Path path, CharSequence s, Charset charset) throws IOException {
		writeText(path, s, charset, false);
	}
	public static void appendText(Path path, CharSequence s, String charset) throws IOException {
		appendText(path, s, Charset.forName(charset));
	}
	public static void appendText(Path path, CharSequence s) throws IOException {
		appendText(path, s, DEFAULT_CHARSET);
	}
	public static void appendText(Path path, CharSequence s, Charset charset) throws IOException {
		writeText(path, s, charset, true);
	}
	public static void writeText(Path path, CharSequence s, Charset charset, boolean append) throws IOException {
		writer()
		.charset(charset)
		.target(path, append)
		.write(s);
	}
	public static void writeText(WritableByteChannel channel, CharSequence s, WriterConfig config) throws IOException {
		if(s.length() == 0) return;
		
		CharsetEncoder encoder = config.encoder();
		CharBuffer chars = s.getClass() == CharBuffer.class ? (CharBuffer) s : CharBuffer.wrap(s);
		ByteBuffer buffer = ByteBuffer.allocate(StringIOUtils.computeBufferSize(encoder, chars));
		int loops = 0;

		while(true) {
			loops++;
			CoderResult c = encoder.encode(chars, buffer, true);
			checkResult(config, c);

			write(buffer, channel, true);

			if(!chars.hasRemaining()) {
				while(true) {
					c = encoder.flush(buffer);
					write(buffer, channel, true);
					if(c.isUnderflow()) break;
				}
				break;
			}
		}

		int t2 = loops;
		LOGGER.fine(() -> "WRITE { charset:"+config.charset()+", CharSequence.length:"+s.length()+", ByteBuffer.capacity:"+buffer.capacity()+", loopCount:"+t2+"}"); 
	}

	public static void appendTextAtBegining(Path path, CharSequence s, String charset) throws IOException {
		appendTextAtBegining(path, s, Charset.forName(charset));
	}
	public static void appendTextAtBegining(Path path, CharSequence s) throws IOException {
		appendTextAtBegining(path, s, DEFAULT_CHARSET);
	}
	public static void appendTextAtBegining(Path path, CharSequence s, Charset charset) throws IOException {
		Path temp = Files.createTempFile(path.getFileName().toString(), null);
		
		try(FileChannel fc = FileChannel.open(temp, CREATE,WRITE);
				FileChannel target = FileChannel.open(path, READ)) {
			writer().charset(charset).target(fc).write(s);
			target.transferTo(0, target.size(), fc);
		}
		Files.move(temp, path, REPLACE_EXISTING); 
	}
	private static void checkResult(WriterConfig w, CoderResult c) throws CharacterCodingException {
		if((c.isUnmappable() && w.onUnmappableCharacter == REPORT) || (c.isMalformed() && w.onMalformedInput == REPORT))
			c.throwException();
	}
}
