package com.koszy.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectUtilTest {

	@Test
	void cloneTest() {
		Assertions.assertNull(ObjectUtil.clone((Object) null));
		Assertions.assertThrows(CloneNotSupportedException.class, () -> ObjectUtil.clone(new Object()));
		Assertions.assertThrows(CloneNotSupportedException.class, () -> ObjectUtil.clone(new NoClone()));
		Assertions.assertThrows(CloneNotSupportedException.class, () -> ObjectUtil.clone(new ProtectedClone()));
		Assertions.assertThrows(RuntimeException.class, () -> ObjectUtil.clone(new ExceptionalClone()));
		Assertions.assertNotNull(Assertions.assertDoesNotThrow(() -> ObjectUtil.clone(new PublicClone())));

		Assertions.assertThrows(CloneNotSupportedException.class, () -> ObjectUtil.clone(new Object(), true));
		Assertions.assertThrows(CloneNotSupportedException.class, () -> ObjectUtil.clone(new NoClone(), true));
		Assertions.assertNotNull(Assertions.assertDoesNotThrow(() -> ObjectUtil.clone(new ProtectedClone(), true)));
		Assertions.assertNotNull(Assertions.assertDoesNotThrow(() -> ObjectUtil.clone(new PublicClone(), true)));
	}

	static class NoClone {
	}

	static class ProtectedClone implements Cloneable {
		@Override
		protected ProtectedClone clone() {
			try {
				return (ProtectedClone) super.clone();
			} catch (final CloneNotSupportedException exception) {
				return LangUtil.rethrow(exception);
			}
		}
	}

	static class PublicClone implements Cloneable {
		@Override
		public PublicClone clone() {
			try {
				return (PublicClone) super.clone();
			} catch (final CloneNotSupportedException exception) {
				return LangUtil.rethrow(exception);
			}
		}
	}

	static class ExceptionalClone implements Cloneable {
		@Override
		public ExceptionalClone clone() {
			throw new RuntimeException();
		}
	}

}
