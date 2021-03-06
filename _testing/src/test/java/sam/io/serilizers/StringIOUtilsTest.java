package sam.io.serilizers;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sam.test.commons.Utils.random_string;
import static sam.test.commons.Utils.writeable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.thedeanda.lorem.LoremIpsum;

import sam.io.IOConstants;
import sam.io.ReadableByteChannelCustom;
import sam.io.WritableByteChannelCustom;
import sam.nopkg.Resources;

public class StringIOUtilsTest {

	@Test
	public void writeTest() throws IOException {
		assertThrows(NullPointerException.class, () -> StringIOUtils.write(writeable(b -> fail()), null));

		//check empty write
		StringIOUtils.write(writeable(b -> fail()), "");
		CharBuffer c = CharBuffer.allocate(1000);
		Random r = new Random();
		

		for (int i = 0; i < 100; i++) {
			writeTest(random_string(c, r));
			writeTest(random_string(c, r));
		}
	}

	private void writeTest(String s) throws IOException {
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		ByteBuffer expected = encoder.encode(CharBuffer.wrap(s)).asReadOnlyBuffer();

		ByteBuffer actual = ByteBuffer.allocate(expected.remaining());

		write0(expected, actual, null, s);
		write0(expected, actual, null, CharBuffer.wrap(s));
		
		ByteBuffer buffer = ByteBuffer.allocate(100);
		write0(expected, actual, buffer, s);
		write0(expected, actual, buffer, CharBuffer.wrap(s));
		
		System.out.println("len: "+s.length()+", bytes: "+actual.remaining());
	}

	private void write0(ByteBuffer expected, ByteBuffer actual, ByteBuffer buffer, CharSequence s) throws IOException {
		actual.clear();
		if(buffer != null)
			buffer.clear();

		StringIOUtils.write(writeable(b -> actual.put(b), buffer), s);
		actual.flip();

		assertEquals(expected, actual);
	}
	
	@Test
	void writeJoiningTest() throws IOException {
		try {
			writeJoiningTest0("\n");
			writeJoiningTest0("josen");
		} catch (Throwable e) {
			fail(e);
		}
	}

	private void writeJoiningTest0(String separator) throws IOException {
		Random r = new Random();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);

		ArrayList<String> list = new ArrayList<>(100);
		CharBuffer chars = CharBuffer.allocate(1000);

		StringIOUtils.writeJoining(new Iterator<String>() {
			int n = 0;
			@Override
			public String next() {
				if(n >= 100)
					throw new NoSuchElementException();

				String s = random_string(chars, r);
				list.add(s);
				n++;
				return s;
			}

			@Override
			public boolean hasNext() {
				return n < 100;
			}
		}, separator, WritableByteChannelCustom.of(bos, null), null, null);

		StringBuilder sb = new StringBuilder();
		list.forEach(s -> sb.append(s).append(separator));
		String expected = sb.toString();
		String actual = bos.toString("utf-8");

		assertEquals(expected.length(), actual.length());
		assertEquals(expected, actual);
	}

	@Test
	public void collectTest() throws IOException {
		collectTest0('\n');
		collectTest0('\t');
	}

	private void collectTest0(char separator) throws IOException {
		Random r = new Random();
		LoremIpsum lorem = new LoremIpsum();
		
		final List<String> list = Stream.generate(() -> lorem.getWords(r.nextInt(10))).limit(100).collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		list.forEach(s -> sb.append(s).append(separator));

		ByteBuffer buffer = IOConstants.newEncoder().encode(CharBuffer.wrap(sb));
		sb.setLength(0);

		ByteArrayInputStream bos = new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
		ArrayList<String> list2 = new ArrayList<>(100);

		StringBuilder sb2 = new StringBuilder();
		int cap = sb2.capacity();
		ByteBuffer buf = ByteBuffer.allocate(300);
		StringIOUtils.collect(ReadableByteChannelCustom.of(bos, buf), separator, list2::add, null, null, sb2);

		System.out.println("max: "+list.stream().mapToInt(s -> s.length()).max().getAsInt()+", sb cap: ("+cap+" -> "+sb2.capacity()+")");

		assertEquals(list, list2);
	}

	@Test
	public void encodeTest() throws IOException {
		try(Resources r = Resources.get()) {
			String s = "sameer";
			ByteBuffer expected = r.encoder().encode(CharBuffer.wrap(s));
			
			ByteBuffer b2 = IOConstants.EMPTY_BUFFER;
			ByteBuffer actual = StringIOUtils.encode(s, b2, r.encoder());
			
			assertNotSame(b2, actual);
			assertEquals(expected, actual);
			
			b2 = r.buffer();
			actual = StringIOUtils.encode(s, b2, r.encoder());
			
			assertSame(b2, actual);
			assertEquals(expected, actual);
			
		}
	}
}
