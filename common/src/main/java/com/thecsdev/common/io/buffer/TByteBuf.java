package com.thecsdev.common.io.buffer;

import com.thecsdev.common.util.annotations.Virtual;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * {@link ByteBuf} implementation with additional IO-related functions.
 */
@SuppressWarnings("deprecation")
public @Virtual class TByteBuf extends ByteBuf
{
	// ==================================================
	private final @NotNull ByteBuf target;
	// ==================================================
	public TByteBuf(@NotNull ByteBuf target) {
		this.target = requireNonNull(target);
	}
	// ==================================================
	public @NotNull ByteBuf getTarget() { return this.target; }
	// ==================================================
	public final @Override String toString() { return String.format("%s[%s]", this, this.target); }
	public final @Override int hashCode() { return this.target.hashCode(); }
	public final @Override boolean equals(Object obj) {
		if(this == obj) return true;
		else if(obj == null || getClass() != obj.getClass()) return false;
		else return this.target.equals(((TByteBuf) obj).target);
	}
	// ==================================================
	public final @Override int capacity() { return this.target.capacity(); }
	public final @Override ByteBuf capacity(int newCapacity) { return this.target.capacity(newCapacity); }
	public final @Override int maxCapacity() { return this.target.maxCapacity(); }
	public final @Override ByteBufAllocator alloc() { return this.target.alloc(); }
	public final @Override ByteOrder order() { return this.target.order(); }
	public final @Override ByteBuf order(ByteOrder endianness) { return this.target.order(endianness); }
	public final @Override ByteBuf unwrap() { return this.target.unwrap(); }
	public final @Override boolean isDirect() { return this.target.isDirect(); }
	public final @Override boolean isReadOnly() { return this.target.isReadOnly(); }
	public final @Override ByteBuf asReadOnly() { return this.target.asReadOnly(); }
	public final @Override int readerIndex() { return this.target.readerIndex(); }
	public final @Override ByteBuf readerIndex(int readerIndex) { return this.target.readerIndex(readerIndex); }
	public final @Override int writerIndex() { return this.target.writerIndex(); }
	public final @Override ByteBuf writerIndex(int writerIndex) { return this.target.writerIndex(writerIndex); }
	public final @Override ByteBuf setIndex(int readerIndex, int writerIndex) { return this.target.setIndex(readerIndex, writerIndex); }
	public final @Override int readableBytes() { return this.target.readableBytes(); }
	public final @Override int writableBytes() { return this.target.writableBytes(); }
	public final @Override int maxWritableBytes() { return this.target.maxWritableBytes(); }
	public final @Override boolean isReadable() { return this.target.isReadable(); }
	public final @Override boolean isReadable(int size) { return this.target.isReadable(size); }
	public final @Override boolean isWritable() { return this.target.isWritable(); }
	public final @Override boolean isWritable(int size) { return this.target.isWritable(size); }
	public final @Override ByteBuf clear() { return this.target.clear(); }
	public final @Override ByteBuf markReaderIndex() { return this.target.markReaderIndex(); }
	public final @Override ByteBuf resetReaderIndex() { return this.target.resetReaderIndex(); }
	public final @Override ByteBuf markWriterIndex() { return this.target.markWriterIndex(); }
	public final @Override ByteBuf resetWriterIndex() { return this.target.resetWriterIndex(); }
	public final @Override ByteBuf discardReadBytes() { return this.target.discardReadBytes(); }
	public final @Override ByteBuf discardSomeReadBytes() { return this.target.discardSomeReadBytes(); }
	public final @Override ByteBuf ensureWritable(int minWritableBytes) { return this.target.ensureWritable(minWritableBytes); }
	public final @Override int ensureWritable(int minWritableBytes, boolean force) { return this.target.ensureWritable(minWritableBytes, force); }
	public final @Override boolean getBoolean(int index) { return this.target.getBoolean(index); }
	public final @Override byte getByte(int index) { return this.target.getByte(index); }
	public final @Override short getUnsignedByte(int index) { return this.target.getUnsignedByte(index); }
	public final @Override short getShort(int index) { return this.target.getShort(index); }
	public final @Override short getShortLE(int index) { return this.target.getShortLE(index); }
	public final @Override int getUnsignedShort(int index) { return this.target.getUnsignedShort(index); }
	public final @Override int getUnsignedShortLE(int index) { return this.target.getUnsignedShortLE(index); }
	public final @Override int getMedium(int index) { return this.target.getMedium(index); }
	public final @Override int getMediumLE(int index) { return this.target.getMediumLE(index); }
	public final @Override int getUnsignedMedium(int index) { return this.target.getUnsignedMedium(index); }
	public final @Override int getUnsignedMediumLE(int index) { return this.target.getUnsignedMediumLE(index); }
	public final @Override int getInt(int index) { return this.target.getInt(index); }
	public final @Override int getIntLE(int index) { return this.target.getIntLE(index); }
	public final @Override long getUnsignedInt(int index) { return this.target.getUnsignedInt(index); }
	public final @Override long getUnsignedIntLE(int index) { return this.target.getUnsignedIntLE(index); }
	public final @Override long getLong(int index) { return this.target.getLong(index); }
	public final @Override long getLongLE(int index) { return this.target.getLongLE(index); }
	public final @Override char getChar(int index) { return this.target.getChar(index); }
	public final @Override float getFloat(int index) { return this.target.getFloat(index); }
	public final @Override double getDouble(int index) { return this.target.getDouble(index); }
	public final @Override ByteBuf getBytes(int index, ByteBuf dst) { return this.target.getBytes(index, dst); }
	public final @Override ByteBuf getBytes(int index, ByteBuf dst, int length) { return this.target.getBytes(index, dst, length); }
	public final @Override ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) { return this.target.getBytes(index, dst, dstIndex, length); }
	public final @Override ByteBuf getBytes(int index, byte[] dst) { return this.target.getBytes(index, dst); }
	public final @Override ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) { return this.target.getBytes(index, dst, dstIndex, length); }
	public final @Override ByteBuf getBytes(int index, ByteBuffer dst) { return this.target.getBytes(index, dst); }
	public final @Override ByteBuf getBytes(int index, OutputStream out, int length) throws IOException
	{ return this.target.getBytes(index, out, length); }
	public final @Override int getBytes(int index, GatheringByteChannel out, int length) throws IOException { return this.target.getBytes(index, out, length); }
	public final @Override int getBytes(int index, FileChannel out, long position, int length) throws IOException { return this.target.getBytes(index, out, position, length); }
	public final @Override CharSequence getCharSequence(int index, int length, Charset charset) { return this.target.getCharSequence(index, length, charset); }
	public final @Override ByteBuf setBoolean(int index, boolean value) { return this.target.setBoolean(index, value); }
	public final @Override ByteBuf setByte(int index, int value) { return this.target.setByte(index, value); }
	public final @Override ByteBuf setShort(int index, int value) { return this.target.setShort(index, value); }
	public final @Override ByteBuf setShortLE(int index, int value) { return this.target.setShortLE(index, value); }
	public final @Override ByteBuf setMedium(int index, int value) { return this.target.setMedium(index, value); }
	public final @Override ByteBuf setMediumLE(int index, int value) { return this.target.setMediumLE(index, value); }
	public final @Override ByteBuf setInt(int index, int value) { return this.target.setInt(index, value); }
	public final @Override ByteBuf setIntLE(int index, int value) { return this.target.setIntLE(index, value); }
	public final @Override ByteBuf setLong(int index, long value) { return this.target.setLong(index, value); }
	public final @Override ByteBuf setLongLE(int index, long value) { return this.target.setLongLE(index, value); }
	public final @Override ByteBuf setChar(int index, int value) { return this.target.setChar(index, value); }
	public final @Override ByteBuf setFloat(int index, float value) { return this.target.setFloat(index, value); }
	public final @Override ByteBuf setDouble(int index, double value) { return this.target.setDouble(index, value); }
	public final @Override ByteBuf setBytes(int index, ByteBuf src) { return this.target.setBytes(index, src); }
	public final @Override ByteBuf setBytes(int index, ByteBuf src, int length) { return this.target.setBytes(index, src, length); }
	public final @Override ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) { return this.target.setBytes(index, src, srcIndex, length); }
	public final @Override ByteBuf setBytes(int index, byte[] src) { return this.target.setBytes(index, src); }
	public final @Override ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) { return this.target.setBytes(index, src, srcIndex, length); }
	public final @Override ByteBuf setBytes(int index, ByteBuffer src) { return this.target.setBytes(index, src); }
	public final @Override int setBytes(int index, InputStream in, int length) throws IOException { return this.target.setBytes(index, in, length); }
	public final @Override int setBytes(int index, ScatteringByteChannel in, int length) throws IOException { return this.target.setBytes(index, in, length); }
	public final @Override int setBytes(int index, FileChannel in, long position, int length) throws IOException { return this.target.setBytes(index, in, position, length); }
	public final @Override ByteBuf setZero(int index, int length) { return this.target.setZero(index, length); }
	public final @Override int setCharSequence(int index, CharSequence sequence, Charset charset) { return this.target.setCharSequence(index, sequence, charset); }
	public final @Override boolean readBoolean() { return this.target.readBoolean(); }
	public final @Override byte readByte() { return this.target.readByte(); }
	public final @Override short readUnsignedByte() { return this.target.readUnsignedByte(); }
	public final @Override short readShort() { return this.target.readShort(); }
	public final @Override short readShortLE() { return this.target.readShortLE(); }
	public final @Override int readUnsignedShort() { return this.target.readUnsignedShort(); }
	public final @Override int readUnsignedShortLE() { return this.target.readUnsignedShortLE(); }
	public final @Override int readMedium() { return this.target.readMedium(); }
	public final @Override int readMediumLE() { return this.target.readMediumLE(); }
	public final @Override int readUnsignedMedium() { return this.target.readUnsignedMedium(); }
	public final @Override int readUnsignedMediumLE() { return this.target.readUnsignedMediumLE(); }
	public final @Override int readInt() { return this.target.readInt(); }
	public final @Override int readIntLE() { return this.target.readIntLE(); }
	public final @Override long readUnsignedInt() { return this.target.readUnsignedInt(); }
	public final @Override long readUnsignedIntLE() { return this.target.readUnsignedIntLE(); }
	public final @Override long readLong() { return this.target.readLong(); }
	public final @Override long readLongLE() { return this.target.readLongLE(); }
	public final @Override char readChar() { return this.target.readChar(); }
	public final @Override float readFloat() { return this.target.readFloat(); }
	public final @Override double readDouble() { return this.target.readDouble(); }
	public final @Override ByteBuf readBytes(int length) { return this.target.readBytes(length); }
	public final @Override ByteBuf readSlice(int length) { return this.target.readSlice(length); }
	public final @Override ByteBuf readRetainedSlice(int length) { return this.target.readRetainedSlice(length); }
	public final @Override ByteBuf readBytes(ByteBuf dst) { return this.target.readBytes(dst); }
	public final @Override ByteBuf readBytes(ByteBuf dst, int length) { return this.target.readBytes(dst, length); }
	public final @Override ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) { return this.target.readBytes(dst, dstIndex, length); }
	public final @Override ByteBuf readBytes(byte[] dst) { return this.target.readBytes(dst); }
	public final @Override ByteBuf readBytes(byte[] dst, int dstIndex, int length) { return this.target.readBytes(dst, dstIndex, length); }
	public final @Override ByteBuf readBytes(ByteBuffer dst) { return this.target.readBytes(dst); }
	public final @Override ByteBuf readBytes(OutputStream out, int length) throws IOException { return this.target.readBytes(out, length); }
	public final @Override int readBytes(GatheringByteChannel out, int length) throws IOException { return this.target.readBytes(out, length); }
	public final @Override CharSequence readCharSequence(int length, Charset charset) { return this.target.readCharSequence(length, charset); }
	public final @Override int readBytes(FileChannel out, long position, int length) throws IOException { return this.target.readBytes(out, position, length); }
	public final @Override TByteBuf skipBytes(int length) { this.target.skipBytes(length); return this; }
	public final @Override TByteBuf writeBoolean(boolean value) { this.target.writeBoolean(value); return this; }
	public final @Override TByteBuf writeByte(int value) { this.target.writeByte(value); return this; }
	public final @Override TByteBuf writeShort(int value) { this.target.writeShort(value); return this; }
	public final @Override TByteBuf writeShortLE(int value) { this.target.writeShortLE(value); return this; }
	public final @Override TByteBuf writeMedium(int value) { this.target.writeMedium(value); return this; }
	public final @Override TByteBuf writeMediumLE(int value) { this.target.writeMediumLE(value); return this; }
	public final @Override TByteBuf writeInt(int value) { this.target.writeInt(value); return this; }
	public final @Override TByteBuf writeIntLE(int value) { this.target.writeIntLE(value); return this; }
	public final @Override TByteBuf writeLong(long value) { this.target.writeLong(value); return this; }
	public final @Override TByteBuf writeLongLE(long value) { this.target.writeLongLE(value); return this; }
	public final @Override TByteBuf writeChar(int value) { this.target.writeChar(value); return this; }
	public final @Override TByteBuf writeFloat(float value) { this.target.writeFloat(value); return this; }
	public final @Override TByteBuf writeDouble(double value) { this.target.writeDouble(value); return this; }
	public final @Override TByteBuf writeBytes(ByteBuf src) { this.target.writeBytes(src); return this; }
	public final @Override TByteBuf writeBytes(ByteBuf src, int length) { this.target.writeBytes(src, length); return this; }
	public final @Override TByteBuf writeBytes(ByteBuf src, int srcIndex, int length) { this.target.writeBytes(src, srcIndex, length); return this; }
	public final @Override TByteBuf writeBytes(byte[] src) { this.target.writeBytes(src); return this; }
	public final @Override TByteBuf writeBytes(byte[] src, int srcIndex, int length) { this.target.writeBytes(src, srcIndex, length); return this; }
	public final @Override TByteBuf writeBytes(ByteBuffer src) { this.target.writeBytes(src); return this; }
	public final @Override int writeBytes(InputStream in, int length) throws IOException { return this.target.writeBytes(in, length); }
	public final @Override int writeBytes(ScatteringByteChannel in, int length) throws IOException { return this.target.writeBytes(in, length); }
	public final @Override int writeBytes(FileChannel in, long position, int length) throws IOException { return this.target.writeBytes(in, position, length); }
	public final @Override TByteBuf writeZero(int length) { this.target.writeZero(length); return this; }
	public final @Override int writeCharSequence(CharSequence sequence, Charset charset) { return this.target.writeCharSequence(sequence, charset); }
	public final @Override int indexOf(int fromIndex, int toIndex, byte value) { return this.target.indexOf(fromIndex, toIndex, value); }
	public final @Override int bytesBefore(byte value) { return this.target.bytesBefore(value); }
	public final @Override int bytesBefore(int length, byte value) { return this.target.bytesBefore(length, value); }
	public final @Override int bytesBefore(int index, int length, byte value) { return this.target.bytesBefore(index, length, value); }
	public final @Override int forEachByte(ByteProcessor processor) { return this.target.forEachByte(processor); }
	public final @Override int forEachByte(int index, int length, ByteProcessor processor) { return this.target.forEachByte(index, length, processor); }
	public final @Override int forEachByteDesc(ByteProcessor processor) { return this.target.forEachByteDesc(processor); }
	public final @Override int forEachByteDesc(int index, int length, ByteProcessor processor) { return this.target.forEachByteDesc(index, length, processor); }
	public final @Override TByteBuf copy() { return new TByteBuf(this.target.copy()); }
	public final @Override TByteBuf copy(int index, int length) { return new TByteBuf(this.target.copy(index, length)); }
	public final @Override ByteBuf slice() { return this.target.slice(); }
	public final @Override ByteBuf retainedSlice() { return this.target.retainedSlice(); }
	public final @Override ByteBuf slice(int index, int length) { return this.target.slice(index, length); }
	public final @Override ByteBuf retainedSlice(int index, int length) { return this.target.retainedSlice(index, length); }
	public final @Override ByteBuf duplicate() { return this.target.duplicate(); }
	public final @Override ByteBuf retainedDuplicate() { return this.target.retainedDuplicate(); }
	public final @Override int nioBufferCount() { return this.target.nioBufferCount(); }
	public final @Override ByteBuffer nioBuffer() { return this.target.nioBuffer(); }
	public final @Override ByteBuffer nioBuffer(int index, int length) { return this.target.nioBuffer(index, length); }
	public final @Override ByteBuffer internalNioBuffer(int index, int length) { return this.target.internalNioBuffer(index, length); }
	public final @Override ByteBuffer[] nioBuffers() { return this.target.nioBuffers(); }
	public final @Override ByteBuffer[] nioBuffers(int index, int length) { return this.target.nioBuffers(index, length); }
	public final @Override boolean hasArray() { return this.target.hasArray(); }
	public final @Override byte[] array() { return this.target.array(); }
	public final @Override int arrayOffset() { return this.target.arrayOffset(); }
	public final @Override boolean hasMemoryAddress() { return this.target.hasMemoryAddress(); }
	public final @Override long memoryAddress() { return this.target.memoryAddress(); }
	public final @Override String toString(Charset charset) { return this.target.toString(charset); }
	public final @Override String toString(int index, int length, Charset charset) { return this.target.toString(index, length, charset); }
	public final @Override int compareTo(ByteBuf buffer) { return this.target.compareTo(buffer); }
	public final @Override ByteBuf retain(int increment) { return this.target.retain(increment); }
	public final @Override int refCnt() { return this.target.refCnt(); }
	public final @Override ByteBuf retain() { return this.target.retain(); }
	public final @Override ByteBuf touch() { return this.target.touch(); }
	public final @Override ByteBuf touch(Object hint) { return this.target.touch(hint); }
	public final @Override boolean release() { return this.target.release(); }
	public final @Override boolean release(int decrement) { return this.target.release(decrement); }
	// ==================================================
	/**
	 * Writes a null-terminated C-style {@link String} to this buffer using
	 * {@link StandardCharsets#UTF_8} encoding.
	 * @param str The {@link String} to write.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If the {@link String} contains a {@code null} character (&#92;0).
	 * @see #readCString()
	 */
	public final TByteBuf writeCString(@NotNull String str) {
		return writeCString(str, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a null-terminated C-style {@link String} to this buffer using the specified
	 * {@link Charset} for encoding.
	 * @param str The {@link String} to write.
	 * @param charset The {@link Charset} to use for encoding.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalArgumentException If the {@link String} contains a {@code null} character (&#92;0).
	 * @see #readCString(Charset)
	 */
	public final TByteBuf writeCString(@NotNull String str, @NotNull Charset charset)
			throws NullPointerException, IllegalArgumentException
	{
		//not null requirements - done FIRST to avoid data corruption
		requireNonNull(str);
		requireNonNull(charset);
		//check the string and ensure it doesn't have a null character inside, for safety
		if(str.indexOf('\0') != -1)
			throw new IllegalArgumentException("String must not contain null character (\\0)");
		//write the null-terminated C-style string to the buffer, and return
		writeCharSequence(str, charset);
		writeZero(1);
		return this;
	}

	/**
	 * Reads a null-terminated C-style {@link String} from this buffer using
	 * {@link StandardCharsets#UTF_8} encoding.
	 * @throws IndexOutOfBoundsException If no null-terminator (&#92;0) was
	 * found before the end of the readable bytes.
	 * @see #writeCString(String)
	 */
	@Contract(" -> new")
	public final @NotNull String readCString() throws IndexOutOfBoundsException {
		return readCString(StandardCharsets.UTF_8);
	}

	/**
	 * Reads a null-terminated C-style {@link String} from this buffer using the specified
	 * {@link Charset} for decoding.
	 * @param charset The {@link Charset} to use for decoding.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IndexOutOfBoundsException If no null-terminator (&#92;0) was
	 * found before the end of the readable bytes.
	 * @see #writeCString(String, Charset)
	 */
	@Contract("_ -> new")
	public final @NotNull String readCString(@NotNull Charset charset)
			throws NullPointerException, IndexOutOfBoundsException
	{
		final int initialReaderIndex = this.target.readerIndex();

		//mark the starting position of the string in the buffer, and
		//scan for the null byte and find the length
		int     strLength  = 0;
		boolean terminated = false;
		while(this.target.isReadable()) {
			if(this.target.readByte() == 0) {
				terminated = true;
				break; //found the null terminator
			}
			strLength++;
		}

		//check if the loop ran out of readable bytes before finding '\0'
		if(!terminated) {
			//string is not null-terminated in the available bytes
			this.target.readerIndex(initialReaderIndex);
			throw new IndexOutOfBoundsException("Buffer ended before null terminator (\\0) was found.");
		}

		//reset the reader index back to the start of the string data
		this.target.readerIndex(initialReaderIndex);

		//decode the data bytes directly into a String, and
		//advance the reader index past the data AND the null terminator
		final var str = this.target.toString(this.target.readerIndex(), strLength, charset);
		this.target.skipBytes(strLength + 1);
		return str;
	}
	// ==================================================
	/**
	 * Writes a variable-length {@link String} to this buffer using
	 * {@link StandardCharsets#UTF_8} encoding.
	 * <p>
	 * <b># The string is written like so:</b><br>
	 * 1. {@link #writeIntLE(int)} - The length of the {@link String}'s {@code byte[]}<br>
	 * 2. {@link #writeBytes(byte[])} - The {@link String}'s {@code byte[]} data
	 * @param str The {@link String} to write.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @see #readVarlenString()
	 */
	public final TByteBuf writeVarlenString(@NotNull String str) throws NullPointerException {
		return writeVarlenString(str, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a variable-length {@link String} to this buffer using the specified
	 * {@link Charset} for encoding.
	 * <p>
	 * <b># The string is written like so:</b><br>
	 * 1. {@link #writeIntLE(int)} - The length of the {@link String}'s {@code byte[]}<br>
	 * 2. {@link #writeBytes(byte[])} - The {@link String}'s {@code byte[]} data
	 * @param str The {@link String} to write.
	 * @param charset The {@link Charset} to use for encoding.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @see #readVarlenString(Charset)
	 */
	public final TByteBuf writeVarlenString(@NotNull String str, @NotNull Charset charset) throws NullPointerException
	{
		//not null requirements - done FIRST to avoid data corruption
		requireNonNull(str);
		requireNonNull(charset);
		//write and return
		final byte[] strBytes = str.getBytes(charset);
		this.target.writeIntLE(strBytes.length);
		this.target.writeBytes(strBytes);
		return this;
	}
	// --------------------------------------------------
	/**
	 * Reads a variable-length {@link String} from this buffer using
	 * {@link StandardCharsets#UTF_8} encoding.
	 * @see #readVarlenString(Charset)
	 */
	@Contract(" -> new")
	public final @NotNull String readVarlenString() {
		return readVarlenString(StandardCharsets.UTF_8);
	}

	/**
	 * Reads a variable-length {@link String} from this buffer using the specified
	 * {@link Charset} for decoding.
	 * @param charset The {@link Charset} to use for decoding.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @see #writeVarlenString(String, Charset)
	 */
	@Contract("_ -> new")
	public final @NotNull String readVarlenString(@NotNull Charset charset) throws NullPointerException
	{
		//not null requirements - done FIRST to avoid data corruption
		requireNonNull(charset);
		//read and return
		final int strLen = this.target.readIntLE();
		final byte[] strBytes = new byte[strLen];
		this.target.readBytes(strBytes);
		return new String(strBytes, charset);
	}
	// ==================================================
	/**
	 * Writes a "Resource interchange file format" chunk to this buffer.
	 * <p>
	 * <b># The chunk is written like so:</b><br>
	 * 1. {@link #writeCharSequence(CharSequence, Charset)} - The 4-character chunk ID<br>
	 * 2. {@link #writeIntLE(int)} - The size of the chunk data in bytes (excluding padding)<br>
	 * 3. The chunk data itself, written via the provided {@link Consumer}&lt;{@link TByteBuf}&gt;
	 * @param chunkId The 4-character chunk ID, using {@link StandardCharsets#US_ASCII} characters.
	 * @param writer A {@link Consumer}&lt;{@link TByteBuf}&gt; that writes the chunk data to this buffer.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalArgumentException If the chunk ID is not exactly 4 characters long.
	 */
	public final TByteBuf writeRiffChunk(@NotNull String chunkId, @NotNull Consumer<TByteBuf> writer)
		throws NullPointerException, IllegalArgumentException
	{
		//not null requirements - done FIRST to avoid data corruption
		requireNonNull(chunkId);
		requireNonNull(writer);

		//the chunk id's length must be exactly 4 characters
		if(chunkId.length() != 4)
			throw new IllegalArgumentException("Chunk ID must be exactly 4 characters long.");

		//write chunk ID
		writeCharSequence(chunkId, StandardCharsets.US_ASCII);

		//reserve space for chunk size (LE)
		final int sizeIndex = this.target.writerIndex();
		writeIntLE(0);

		//write chunk data
		final int dataStartIndex = this.target.writerIndex();
		writer.accept(this);

		//calculate and write size
		final int dataEndIndex = this.target.writerIndex();
		final int chunkSize    = dataEndIndex - dataStartIndex;
		this.target.setIntLE(sizeIndex, chunkSize);

		//add RIFF padding byte if size is odd
		if((chunkSize & 1) == 1)
			this.target.writeByte(0);

		//done. return this now
		return this;
	}

	/**
	 * Reads a "Resource interchange file format" chunk from this buffer. The
	 * {@link BiConsumer} receives the chunk ID and a sliced {@link TByteBuf} containing
	 * the chunk data. The sliced buffer is not a copy and shares the same underlying
	 * data as this buffer.
	 * <p>
	 * <b># The chunk is read like so:</b><br>
	 * 1. {@link #readCharSequence(int, Charset)} - The 4-character chunk ID<br>
	 * 2. {@link #readIntLE()} - The size of the chunk data in bytes (excluding padding)<br>
	 * 3. The chunk data itself, passed to the provided {@link BiConsumer}&lt;{@link String}, {@link TByteBuf}&gt;
	 * @param reader A {@link BiConsumer}&lt;{@link String}, {@link TByteBuf}&gt; that receives the chunk ID
	 * and chunk data buffer.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IndexOutOfBoundsException If there are not enough readable bytes to read the chunk.
	 */
	public final void readRiffChunk(@NotNull BiConsumer<String, TByteBuf> reader)
		throws NullPointerException, IndexOutOfBoundsException
	{
		//not null requirements, as usual
		requireNonNull(reader);

		//read chunk ID
		final String chunkId = this.target.toString(this.target.readerIndex(), 4, StandardCharsets.US_ASCII);
		this.target.skipBytes(4);

		//read chunk size (LE)
		final int chunkSize = this.target.readIntLE();

		//create a "view" buffer for the chunk data
		final int dataStartIndex = this.target.readerIndex();
		final var chunkDataBuf   = new TByteBuf(this.target.slice(dataStartIndex, chunkSize));

		//advance the reader index past the chunk data
		this.target.skipBytes(chunkSize);

		//skip RIFF padding byte if size is odd
		if((chunkSize & 1) == 1)
			this.target.skipBytes(1);

		//pass the data to the reader
		reader.accept(chunkId, chunkDataBuf);
	}
	// ==================================================
}
