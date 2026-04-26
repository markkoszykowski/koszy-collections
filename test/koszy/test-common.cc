#include <limits>

#include <gtest/gtest.h>

#include "koszy/common.h"


// NextPowerOfTwo

TEST(NextPowerOfTwoTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::nextPowerOfTwo(0U), 0U);
}

TEST(NextPowerOfTwoTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::nextPowerOfTwo(1U), 1U);
}

TEST(NextPowerOfTwoTest, HandlesTwo) {
	EXPECT_EQ(koszy::collections::nextPowerOfTwo(2U), 2U);
}

TEST(NextPowerOfTwoTest, HandlesThree) {
	EXPECT_EQ(koszy::collections::nextPowerOfTwo(3U), 4U);
}

TEST(NextPowerOfTwoTest, HandlesMaxValueMinusOne) {
	EXPECT_EQ(
		koszy::collections::nextPowerOfTwo((static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)) - 1U),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)
	);
}

TEST(NextPowerOfTwoTest, HandlesMaxValue) {
	EXPECT_EQ(
		koszy::collections::nextPowerOfTwo(static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)),
		static_cast<std::size_t>(1U) << (std::numeric_limits<std::size_t>::digits - 1)
	);
}

TEST(NextPowerOfTwoTest, Throws) {
	ASSERT_THROW(
		koszy::collections::nextPowerOfTwo(std::numeric_limits<std::size_t>::max()),
		std::invalid_argument
	);
}


// Array Size

TEST(ArraySizeTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::arraySize(0U, 0.1f), 0U);
	EXPECT_EQ(koszy::collections::arraySize(0U, 0.5f), 0U);
	EXPECT_EQ(koszy::collections::arraySize(0U, 0.9999999f), 0U);
}

TEST(ArraySizeTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::arraySize(1U, 0.01f), 128U);
	EXPECT_EQ(koszy::collections::arraySize(1U, 0.1f), 16U);
	EXPECT_EQ(koszy::collections::arraySize(1U, 0.5f), 2U);
	EXPECT_EQ(koszy::collections::arraySize(1U, 0.9999999f), 2U);
}
