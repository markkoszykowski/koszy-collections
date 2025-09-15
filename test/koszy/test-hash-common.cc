#ifndef TEST_HASH_COMMON_H
#define TEST_HASH_COMMON_H

#include <limits>

#include <gtest/gtest.h>

#include "../../include/koszy/hash-common.h"
#include "koszy/hash-common.h"


// NextPowerOfTwo

TEST(NextPowerOfTwoTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::nextPowerOfTwo(0U), 0U);
}

TEST(NextPowerOfTwoTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::nextPowerOfTwo(1U), 1U);
}

TEST(NextPowerOfTwoTest, HandlesTwo) {
	EXPECT_EQ(koszy::collections::hash::nextPowerOfTwo(2U), 2U);
}

TEST(NextPowerOfTwoTest, HandlesThree) {
	EXPECT_EQ(koszy::collections::hash::nextPowerOfTwo(3U), 4U);
}

TEST(NextPowerOfTwoTest, HandlesMaxValueMinusOne) {
	EXPECT_EQ(
		koszy::collections::hash::nextPowerOfTwo((static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)) - 1U),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)
	);
}

TEST(NextPowerOfTwoTest, HandlesMaxValue) {
	EXPECT_EQ(
		koszy::collections::hash::nextPowerOfTwo(static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)
	);
}

TEST(NextPowerOfTwoTest, Throws) {
	ASSERT_THROW(
		koszy::collections::hash::nextPowerOfTwo(std::numeric_limits<std::size_t>::max()),
		std::invalid_argument
	);
}


// Array Size

TEST(ArraySize, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.1f), 0U);
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.5f), 0U);
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.9999999f), 0U);
}

TEST(ArraySize, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.01f), 128U);
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.1f), 16U);
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.5f), 2U);
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.9999999f), 2U);
}

// Mask Size

TEST(MaskSize, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(0U), 0U);
}

TEST(MaskSize, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(1U), 1U);
}

TEST(MaskSize, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(64U), 1U);
}

TEST(MaskSize, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskSize<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint8_t>(101U), 13U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint16_t>(101U), 7U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint32_t>(101U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskSize<std::uint64_t>(101U), 2U);
}

// Mask Pos

TEST(MaskPos, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(0U), 0U);
}

TEST(MaskPos, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(1U), 0U);
}

TEST(MaskPos, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(64U), 1U);
}

TEST(MaskPos, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskPos<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint8_t>(101U), 12U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint16_t>(101U), 6U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint32_t>(101U), 3U);
	EXPECT_EQ(koszy::collections::hash::maskPos<std::uint64_t>(101U), 1U);
}

// Mask Bit

TEST(MaskBit, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(0U), 0U);
}

TEST(MaskBit, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(1U), 1U);
}

TEST(MaskBit, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(64U), 0U);
}

TEST(MaskBit, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::maskBit<bool>(101U), 0U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint8_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint16_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint32_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::maskBit<std::uint64_t>(101U), 37U);
}

#endif // TEST_HASH_COMMON_H
