#ifndef TEST_HASH_COMMON_H
#define TEST_HASH_COMMON_H

#include <gtest/gtest.h>

#include "koszy/hash-mask.h"


// Mask Size

TEST(MaskSizeTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(0U), 0U);
}

TEST(MaskSizeTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(1U), 1U);
}

TEST(MaskSizeTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(64U), 1U);
}

TEST(MaskSizeTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(101U), 13U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(101U), 7U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(101U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(101U), 2U);
}


// Mask Pos

TEST(MaskPosTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(0U), 0U);
}

TEST(MaskPosTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(1U), 0U);
}

TEST(MaskPosTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(64U), 1U);
}

TEST(MaskPosTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(101U), 12U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(101U), 6U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(101U), 3U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(101U), 1U);
}


// Mask Bit

TEST(MaskBitTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(0U), 0U);
}

TEST(MaskBitTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(1U), 1U);
}

TEST(MaskBitTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(64U), 0U);
}

TEST(MaskBitTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(101U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(101U), 37U);
}


// HashMask

template<typename T>
class HashMaskTest : public testing::Test {
	public:
		koszy::collections::hash::HashMask<T> mask_{1U};
};

using HashMaskTypes = testing::Types<bool, std::uint8_t, std::uint16_t, std::uint32_t, std::uint64_t, std::uintmax_t>;

TYPED_TEST_SUITE(HashMaskTest, HashMaskTypes);
TYPED_TEST(HashMaskTest, SetAndUnset) {
	std::size_t size{1U};
	this->mask_.reset(size);

	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), false);
	}
	this->mask_.set(0U);
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), i == 0U);
	}

	size = 1024U;
	this->mask_.reset(size);

	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), false);
	}
	this->mask_.set(512U);
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), i == 512U);
	}
}

#endif // TEST_HASH_COMMON_H
