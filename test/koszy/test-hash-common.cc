#ifndef TEST_HASH_COMMON_H
#define TEST_HASH_COMMON_H

#include <limits>

#include <gtest/gtest.h>

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
		koszy::collections::hash::nextPowerOfTwo((static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1U)) - 1U),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1U)
	);
}

TEST(NextPowerOfTwoTest, HandlesMaxValue) {
	EXPECT_EQ(
		koszy::collections::hash::nextPowerOfTwo(static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1U)),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1U)
	);
}

TEST(NextPowerOfTwoTest, Throws) {
	ASSERT_THROW(
		koszy::collections::hash::nextPowerOfTwo(std::numeric_limits<std::size_t>::max()),
		std::invalid_argument
	);
}


// Mask

TEST(Mask, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::arrayMask(0U), 0U);
}

TEST(Mask, HandlesPowerOfTwo) {
	EXPECT_EQ(koszy::collections::hash::arrayMask(16U), 15U);
}

// Size

TEST(Size, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.1f), 0U);
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.5f), 0U);
	EXPECT_EQ(koszy::collections::hash::arraySize(0U, 0.9999999f), 0U);
}

TEST(Size, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.1f), 16U);
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.5f), 2U);
	EXPECT_EQ(koszy::collections::hash::arraySize(1U, 0.9999999f), 2U);
}

#endif // TEST_HASH_COMMON_H
