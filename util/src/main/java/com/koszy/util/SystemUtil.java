package com.koszy.util;

import java.nio.ByteOrder;
import java.util.Objects;

public final class SystemUtil {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	private static final String OS_ARCH = System.getProperty("os.arch", "unknown");

	public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();
	public static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	public static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	public static final boolean UNALIGNED_ACCESS = OpenUnsafe.theUnsafe().unalignedAccess();

	public static String osName() {
		return OS_NAME;
	}

	public static String osArch() {
		return OS_ARCH;
	}

	public static boolean isWindows() {
		return OS_NAME.startsWith("win");
	}

	public static boolean isLinux() {
		return OS_NAME.contains("linux");
	}

	public static boolean isMac() {
		return OS_NAME.startsWith("mac");
	}

	public static boolean isX64Arch() {
		return Objects.equals(OS_ARCH, "amd64") || Objects.equals(OS_ARCH, "x86_64") || Objects.equals(OS_ARCH, "x64");
	}

	public static ByteOrder nativeByteOrder() {
		return NATIVE_BYTE_ORDER;
	}

	public static boolean isBigEndian() {
		return BIG_ENDIAN;
	}

	public static boolean isLittleEndian() {
		return LITTLE_ENDIAN;
	}

	public static boolean unalignedAccess() {
		return UNALIGNED_ACCESS;
	}

	private SystemUtil() {
		LangUtil.unreachable();
	}

}
