package com.koszy.util.string;

import com.koszy.util.ArrayUtil;
import com.koszy.util.LangUtil;
import com.koszy.util.NumberFormatUtil;
import com.koszy.util.NumberUtil;
import com.koszy.util.Require;
import com.koszy.util.StringUtil;
import com.koszy.util.SystemUtil;
import com.koszy.util.buffer.DirectBuffer;
import com.koszy.util.buffer.UnsafeBuffer;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UnsafeCharSequence implements MutableCharSequence {

	private static final UnsafeBuffer EMPTY = new UnsafeBuffer();

	private static UnsafeBuffer buffer(final int capacity) {
		return new UnsafeBuffer(new byte[capacity]);
	}

	private static UnsafeBuffer copy(final UnsafeBuffer buffer, final int capacity) {
		return new UnsafeBuffer(ArrayUtil.copyOf(buffer.array(), capacity));
	}

	private static UnsafeBuffer copy(final UnsafeBuffer buffer) {
		return copy(buffer, buffer.capacity());
	}

	private static String string(final UnsafeBuffer buffer, final int offset, final int length, final Charset charset) {
		return new String(buffer.array(), offset, length, charset);
	}

	private Encoding encoding;
	private UnsafeBuffer buffer;
	private int length;

	public UnsafeCharSequence() {
		this.encoding = Encoding.LATIN1;
		this.buffer = EMPTY;
		this.length = 0;
	}

	public UnsafeCharSequence(final int capacity) {
		Require.isNonNegative(capacity);
		this.encoding = Encoding.LATIN1;
		this.buffer = capacity == 0 ? EMPTY : buffer(Encoding.size(capacity));
		this.length = 0;
	}

	public UnsafeCharSequence(final CharSequence string) {
		this.encoding = Encoding.LATIN1;
		this.buffer = EMPTY;
		this.length = 0;

		this.encoding.append(this, string);
	}

	public UnsafeCharSequence(final UnsafeCharSequence string) {
		if (StringUtil.length(string) == 0) {
			this.encoding = Encoding.LATIN1;
			this.buffer = EMPTY;
			this.length = 0;
		} else {
			final Encoding encoding = string.encoding;
			final UnsafeBuffer buffer = string.buffer;
			final int length = string.length;

			this.encoding = encoding;
			this.buffer = copy(buffer);
			this.length = length;
		}
	}

	public UnsafeCharSequence reserve(final int capacity) {
		return this.encoding.reserve(this, capacity);
	}

	@Override
	public UnsafeCharSequence append(final boolean b) {
		return this.encoding.append(this, b);
	}

	@Override
	public UnsafeCharSequence append(final int i) {
		return this.encoding.append(this, i);
	}

	@Override
	public UnsafeCharSequence append(final long l) {
		return this.encoding.append(this, l);
	}

	@Override
	public UnsafeCharSequence append(final float f) {
		return this.encoding.append(this, f);
	}

	@Override
	public UnsafeCharSequence append(final double d) {
		return this.encoding.append(this, d);
	}

	@Override
	public UnsafeCharSequence append(final char c) {
		return this.encoding.append(this, c);
	}

	@Override
	public MutableCharSequence appendCodePoint(final int c) {
		return LangUtil.todo();
	}

	@Override
	public UnsafeCharSequence append(final CharSequence string) {
		return string instanceof UnsafeCharSequence ?
				this.encoding.append(this, (UnsafeCharSequence) string) :
				this.encoding.append(this, string);
	}

	@Override
	public UnsafeCharSequence append(final CharSequence string, final int begin, final int end) {
		return string instanceof UnsafeCharSequence ?
				this.encoding.append(this, (UnsafeCharSequence) string, begin, end) :
				this.encoding.append(this, string, begin, end);
	}

	@Override
	public MutableCharSequence append(final char[] string) {
		return this.encoding.append(this, string);
	}

	@Override
	public MutableCharSequence append(final char[] string, final int offset, final int length) {
		return this.encoding.append(this, string, offset, length);
	}

	public UnsafeCharSequence append(final UnsafeCharSequence string) {
		return this.encoding.append(this, string);
	}

	public UnsafeCharSequence append(final UnsafeCharSequence string, final int begin, final int end) {
		return this.encoding.append(this, string, begin, end);
	}

	@Override
	public UnsafeCharSequence append(final Object object) {
		return this.encoding.append(this, String.valueOf(object));
	}

	@Override
	public MutableCharSequence length(final int length) {
		return null;
	}

	@Override
	public MutableCharSequence trim() {
		return null;
	}

	@Override
	public MutableCharSequence clear() {
		this.encoding = Encoding.LATIN1;
		this.length = 0;
		return this;
	}

	@Override
	public int length() {
		return this.encoding.length(this);
	}

	@Override
	public char charAt(final int index) {
		return this.encoding.charAt(this, index);
	}

	@Override
	public CharSequence subSequence(final int begin, final int end) {
		return this.encoding.subSequence(this, begin, end);
	}

	public Charset getCharset() {
		return this.encoding.charset();
	}

	public UnsafeBuffer getByteBuffer() {
		return this.buffer;
	}

	public int getByteLength() {
		return this.length;
	}

	@Override
	public String toString() {
		return string(this.buffer, 0, this.length, this.encoding.charset());
	}

	enum Encoding {
		LATIN1 {
			@Override
			public Charset charset() {
				return StandardCharsets.ISO_8859_1;
			}

			@Override
			public UnsafeCharSequence reserve(final UnsafeCharSequence string, final int capacity) {
				Require.isNonNegative(capacity);
				string.buffer = reserve(string.buffer, capacity);
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final boolean b) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				if (b) {
					buffer = expand(buffer, length, 4);
					buffer.putByte(string.length, (byte) 't');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'r');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'u');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'e');
					length += NumberUtil.BYTE_BYTES;
				} else {
					buffer = expand(buffer, length, 5);
					buffer.putByte(string.length, (byte) 'f');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'a');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'l');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 's');
					length += NumberUtil.BYTE_BYTES;
					buffer.putByte(string.length, (byte) 'e');
					length += NumberUtil.BYTE_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final int i) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int additional = NumberFormatUtil.length(i);

				buffer = expand(buffer, length, additional);
				int index = length + additional;
				int remainder = i;
				do {
					buffer.putByte(--index, (byte) ((remainder % 10) + '0'));
					length += NumberUtil.BYTE_BYTES;

					remainder /= 10;
				} while (remainder != 0);
				if (i < 0) {
					buffer.putByte(--index, (byte) '-');
					length += NumberUtil.BYTE_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final long l) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int additional = NumberFormatUtil.length(l);

				buffer = expand(buffer, length, additional);
				int index = length + additional;
				long remainder = l;
				do {
					buffer.putByte(--index, (byte) ((remainder % 10L) + '0'));
					length += NumberUtil.BYTE_BYTES;

					remainder /= 10L;
				} while (remainder != 0L);
				if (l < 0L) {
					buffer.putByte(--index, (byte) '-');
					length += NumberUtil.BYTE_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final float f) {
				NumberFormatUtil.append(f, string);
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final double d) {
				NumberFormatUtil.append(d, string);
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char c) {
				Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				if (StringUtil.isLatin1(c)) {
					buffer = expand(buffer, length, NumberUtil.BYTE_BYTES);
					buffer.putByte(length, StringUtil.toLatin1Byte(c));
					length += NumberUtil.BYTE_BYTES;
				} else {
					encoding = UTF16;
					buffer = inflate(buffer, length, NumberUtil.BYTE_BYTES);
					length <<= INFLATE;

					buffer.putCharUnaligned(length, c);
					length += NumberUtil.CHAR_BYTES;
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			public UnsafeCharSequence nulll(final UnsafeCharSequence string) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, 4);
				buffer.putByte(length, (byte) 'n');
				length += NumberUtil.BYTE_BYTES;
				buffer.putByte(length, (byte) 'u');
				length += NumberUtil.BYTE_BYTES;
				buffer.putByte(length, (byte) 'l');
				length += NumberUtil.BYTE_BYTES;
				buffer.putByte(length, (byte) 'l');
				length += NumberUtil.BYTE_BYTES;

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			public UnsafeCharSequence nulll(final UnsafeCharSequence string, final int begin, final int end) {
				Require.checkBeginEnd(begin, end, 4);

				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, end - begin);
				for (int i = begin; i != end; ++i) {
					final byte b;
					switch (i) {
						case 0:
							b = 'n';
							break;
						case 1:
							b = 'u';
							break;
						case 2:
						case 3:
							b = 'l';
							break;
						default:
							return LangUtil.unreachable();
					}
					buffer.putByte(length, b);
					length += NumberUtil.BYTE_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final CharSequence chars) {
				if (chars == null) {
					return this.nulll(string);
				}

				Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int size = Require.isNonNegative(chars.length());
				buffer = expand(buffer, length, size);
				for (int i = 0; i != size; ++i) {
					final char c = chars.charAt(i);
					if (encoding == UTF16) {
						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					} else if (StringUtil.isLatin1(c)) {
						buffer.putByte(length, StringUtil.toLatin1Byte(c));
						length += NumberUtil.BYTE_BYTES;
					} else {
						encoding = UTF16;
						buffer = inflate(buffer, length, size - i);
						length <<= INFLATE;

						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final CharSequence chars, final int begin, final int end) {
				if (chars == null) {
					return this.nulll(string, begin, end);
				}

				Require.checkBeginEnd(begin, end, chars.length());

				Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, end - begin);
				for (int i = begin; i != end; ++i) {
					final char c = chars.charAt(i);
					if (encoding == UTF16) {
						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					} else if (StringUtil.isLatin1(c)) {
						buffer.putByte(length, StringUtil.toLatin1Byte(c));
						length += NumberUtil.BYTE_BYTES;
					} else {
						encoding = UTF16;
						buffer = inflate(buffer, length, end - i);
						length <<= INFLATE;

						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char[] chars) {
				Require.nonNull(chars);

				Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int size = chars.length;
				buffer = expand(buffer, length, size);
				for (int i = 0; i != size; ++i) {
					final char c = chars[i];
					if (encoding == UTF16) {
						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					} else if (StringUtil.isLatin1(c)) {
						buffer.putByte(length, StringUtil.toLatin1Byte(c));
						length += NumberUtil.BYTE_BYTES;
					} else {
						encoding = UTF16;
						buffer = inflate(buffer, length, size - i);
						length <<= INFLATE;

						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char[] chars, final int o, final int l) {
				Require.checkOffsetLength(o, l, chars.length);

				Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int begin = o;
				final int end = begin + l;
				buffer = expand(buffer, length, l);
				for (int i = begin; i != end; ++i) {
					final char c = chars[i];
					if (encoding == UTF16) {
						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					} else if (StringUtil.isLatin1(c)) {
						buffer.putByte(length, StringUtil.toLatin1Byte(c));
						length += NumberUtil.BYTE_BYTES;
					} else {
						encoding = UTF16;
						buffer = inflate(buffer, length, end - i);
						length <<= INFLATE;

						buffer.putCharUnaligned(length, c);
						length += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final UnsafeCharSequence chars) {
				if (chars == null) {
					return this.nulll(string);
				}

				Encoding thisEncoding = string.encoding;
				UnsafeBuffer thisBuffer = string.buffer;
				int thisLength = string.length;

				final Encoding thatEncoding = chars.encoding;
				final DirectBuffer thatBuffer = chars.buffer;
				final int thatLength = chars.length;
				if (thatEncoding == thisEncoding) {
					thisBuffer = expand(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, 0, thatLength);
					thisLength += thatLength;
				} else {
					thisEncoding = UTF16;
					thisBuffer = inflate(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, 0, thatLength);
					thisLength += thatLength;
				}

				string.encoding = thisEncoding;
				string.buffer = thisBuffer;
				string.length = thisLength;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final UnsafeCharSequence chars, final int begin, final int end) {
				if (chars == null) {
					return this.nulll(string, begin, end);
				}

				Require.checkBeginEnd(begin, end, chars.length());

				Encoding thisEncoding = string.encoding;
				UnsafeBuffer thisBuffer = string.buffer;
				int thisLength = string.length;

				final Encoding thatEncoding = chars.encoding;
				final DirectBuffer thatBuffer = chars.buffer;
				final int thatLength = end - begin;
				if (thatEncoding == thisEncoding) {
					thisBuffer = expand(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, begin, thatLength);
					thisLength += thatLength;
				} else {
					thisEncoding = UTF16;
					thisBuffer = inflate(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, begin, thatLength);
					thisLength += thatLength;
				}

				string.encoding = thisEncoding;
				string.buffer = thisBuffer;
				string.length = thisLength;
				return string;
			}

			@Override
			public int length(final UnsafeCharSequence string) {
				return string.length;
			}

			@Override
			public char charAt(final UnsafeCharSequence string, final int index) {
				Require.checkIndex(index, string.length());
				return StringUtil.toLatin1Char(string.buffer.getByte(index));
			}

			@Override
			public CharSequence subSequence(final UnsafeCharSequence string, final int begin, final int end) {
				Require.checkBeginEnd(begin, end, string.length());
				return string(string.buffer, begin, end - begin, this.charset());
			}
		},
		UTF16 {
			@Override
			public Charset charset() {
				return SystemUtil.BIG_ENDIAN ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE;
			}

			@Override
			public UnsafeCharSequence reserve(final UnsafeCharSequence string, final int capacity) {
				Require.isNonNegative(capacity);
				string.buffer = reserve(string.buffer, inflate(capacity));
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final boolean b) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				if (b) {
					buffer = expand(buffer, length, 4 << INFLATE);
					buffer.putCharUnaligned(string.length, 't');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'r');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'u');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'e');
					length += NumberUtil.CHAR_BYTES;
				} else {
					buffer = expand(buffer, length, 5 << INFLATE);
					buffer.putCharUnaligned(string.length, 'f');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'a');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'l');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 's');
					length += NumberUtil.CHAR_BYTES;
					buffer.putCharUnaligned(string.length, 'e');
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final int i) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int additional = NumberFormatUtil.length(i);

				buffer = expand(buffer, length, additional);
				int index = length + additional;
				int remainder = i;
				do {
					buffer.putCharUnaligned(--index, (char) ((remainder % 10) + '0'));
					length += NumberUtil.CHAR_BYTES;

					remainder /= 10;
				} while (remainder != 0);
				if (i < 0) {
					buffer.putCharUnaligned(--index, '-');
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final long l) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int additional = NumberFormatUtil.length(l);

				buffer = expand(buffer, length, additional);
				int index = length + additional;
				long remainder = l;
				do {
					buffer.putCharUnaligned(--index, (char) ((remainder % 10L) + '0'));
					length += NumberUtil.CHAR_BYTES;

					remainder /= 10L;
				} while (remainder != 0L);
				if (l < 0L) {
					buffer.putCharUnaligned(--index, '-');
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final float f) {
				return LangUtil.todo();
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final double d) {
				return LangUtil.todo();
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char c) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, NumberUtil.CHAR_BYTES);
				buffer.putCharUnaligned(length, c);
				length += NumberUtil.BYTE_BYTES;

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			public UnsafeCharSequence nulll(final UnsafeCharSequence string) {
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, 4 << INFLATE);
				buffer.putCharUnaligned(length, 'n');
				length += NumberUtil.CHAR_BYTES;
				buffer.putCharUnaligned(length, 'u');
				length += NumberUtil.CHAR_BYTES;
				buffer.putCharUnaligned(length, 'l');
				length += NumberUtil.CHAR_BYTES;
				buffer.putCharUnaligned(length, 'l');
				length += NumberUtil.CHAR_BYTES;

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			public UnsafeCharSequence nulll(final UnsafeCharSequence string, final int begin, final int end) {
				Require.checkBeginEnd(begin, end, 4);

				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, (end - begin) << INFLATE);
				for (int i = begin; i != end; ++i) {
					final char c;
					switch (i) {
						case 0:
							c = 'n';
							break;
						case 1:
							c = 'u';
							break;
						case 2:
						case 3:
							c = 'l';
							break;
						default:
							return LangUtil.unreachable();
					}
					buffer.putCharUnaligned(length, c);
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final CharSequence chars) {
				if (chars == null) {
					return this.nulll(string);
				}

				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int size = Require.isNonNegative(chars.length());
				buffer = expand(buffer, length, inflate(size));
				for (int i = 0; i != size; ++i) {
					buffer.putCharUnaligned(length, chars.charAt(i));
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final CharSequence chars, final int begin, final int end) {
				if (chars == null) {
					return this.nulll(string, begin, end);
				}

				Require.checkBeginEnd(begin, end, chars.length());

				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				buffer = expand(buffer, length, end - begin);
				for (int i = begin; i != end; ++i) {
					buffer.putCharUnaligned(length, chars.charAt(i));
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char[] chars) {
				Require.nonNull(chars);

				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int size = chars.length;
				buffer = expand(buffer, length, size);
				for (int i = 0; i != size; ++i) {
					buffer.putCharUnaligned(length, chars[i]);
					length += NumberUtil.CHAR_BYTES;
				}

				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final char[] chars, final int o, final int l) {
				Require.checkOffsetLength(o, l, chars.length);

				final Encoding encoding = string.encoding;
				UnsafeBuffer buffer = string.buffer;
				int length = string.length;

				final int begin = o;
				final int end = begin + l;
				buffer = expand(buffer, length, l);
				for (int i = begin; i != end; ++i) {
					buffer.putCharUnaligned(length, chars[i]);
					length += NumberUtil.CHAR_BYTES;
				}

				string.encoding = encoding;
				string.buffer = buffer;
				string.length = length;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final UnsafeCharSequence chars) {
				if (chars == null) {
					return this.nulll(string);
				}

				final Encoding thisEncoding = string.encoding;
				UnsafeBuffer thisBuffer = string.buffer;
				int thisLength = string.length;

				final Encoding thatEncoding = chars.encoding;
				final DirectBuffer thatBuffer = chars.buffer;
				final int thatLength = chars.length;
				if (thatEncoding == thisEncoding) {
					thisBuffer = expand(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, 0, thatLength);
					thisLength += thatLength;
				} else {
					for (int i = 0; i != thatLength; ++i) {
						thisBuffer.putCharUnaligned(thisLength, StringUtil.toLatin1Char(thatBuffer.getByte(i)));
						thisLength += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = thisEncoding;
				string.buffer = thisBuffer;
				string.length = thisLength;
				return string;
			}

			@Override
			public UnsafeCharSequence append(final UnsafeCharSequence string, final UnsafeCharSequence chars, final int begin, final int end) {
				if (chars == null) {
					return this.nulll(string, begin, end);
				}

				Require.checkBeginEnd(begin, end, chars.length());

				final Encoding thisEncoding = string.encoding;
				UnsafeBuffer thisBuffer = string.buffer;
				int thisLength = string.length;

				final Encoding thatEncoding = chars.encoding;
				final DirectBuffer thatBuffer = chars.buffer;
				final int thatLength = end - begin;
				if (thatEncoding == thisEncoding) {
					thisBuffer = expand(thisBuffer, thisLength, thatLength);
					thisBuffer.putBytes(thisLength, thatBuffer, begin, thatLength);
					thisLength += thatLength;
				} else {
					for (int i = begin; i != end; ++i) {
						thisBuffer.putCharUnaligned(thisLength, StringUtil.toLatin1Char(thatBuffer.getByte(i)));
						thisLength += NumberUtil.CHAR_BYTES;
					}
				}

				string.encoding = thisEncoding;
				string.buffer = thisBuffer;
				string.length = thisLength;
				return string;
			}

			@Override
			public int length(final UnsafeCharSequence string) {
				return string.length >>> INFLATE;
			}

			@Override
			public char charAt(final UnsafeCharSequence string, final int index) {
				Require.checkIndex(index, string.length());
				return string.buffer.getChar(index << INFLATE);
			}

			@Override
			public CharSequence subSequence(final UnsafeCharSequence string, final int begin, final int end) {
				Require.checkBeginEnd(begin, end, string.length());
				return string(string.buffer, begin << INFLATE, (end - begin) << INFLATE, this.charset());
			}
		};

		public static final int INFLATE = 1;


		public static int size(final int minimum) {
			try {
				return minimum <= 16 ? 16 : NumberUtil.nextPowerOfTwo(minimum);
			} catch (final Exception exception) {
				return minimum;
			}
		}

		public static int inflate(final int x) {
			final int inflated = x << INFLATE;
			if (inflated <= x) {
				throw new ArithmeticException("integer overflow");
			}
			return inflated;
		}

		public static UnsafeBuffer inflate(final UnsafeBuffer buffer, final int length, final int additional) {
			final int size = inflate(Math.addExact(length, additional));
			if (size <= buffer.capacity()) {
				for (int i = length; i != 0; --i) {
					final int index = i - 1;
					buffer.putCharUnaligned(index << INFLATE, StringUtil.toLatin1Char(buffer.getByte(index)));
				}
				return buffer;
			} else {
				final UnsafeBuffer inflated = buffer(size(size));
				for (int i = 0; i != length; ++i) {
					inflated.putCharUnaligned(i << INFLATE, StringUtil.toLatin1Char(buffer.getByte(i)));
				}
				return inflated;
			}
		}

		public static UnsafeBuffer reserve(final UnsafeBuffer buffer, final int capacity) {
			return capacity <= buffer.capacity() ? buffer : copy(buffer, size(capacity));
		}

		public static UnsafeBuffer expand(final UnsafeBuffer buffer, final int length, final int additional) {
			return reserve(buffer, Math.addExact(length, additional));
		}

		public abstract Charset charset();

		public abstract UnsafeCharSequence reserve(UnsafeCharSequence string, int capacity);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, boolean b);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, int i);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, long l);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, float f);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, double d);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, char c);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, CharSequence chars);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, CharSequence chars, int begin, int end);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, char[] chars);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, char[] chars, int offset, int length);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, UnsafeCharSequence chars);

		public abstract UnsafeCharSequence append(UnsafeCharSequence string, UnsafeCharSequence chars, int begin, int end);

		public abstract int length(UnsafeCharSequence string);

		public abstract char charAt(UnsafeCharSequence string, int index);

		public abstract CharSequence subSequence(UnsafeCharSequence string, int start, int end);
	}

}
