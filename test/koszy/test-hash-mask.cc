#include <memory>
#include <utility>

#include <gtest/gtest.h>

#include "koszy/hash-mask.h"

#include "test/koszy/allocator.h"


// Mask Size

TEST(MaskSizeTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint64_t>(0U), 0U);
}

TEST(MaskSizeTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint64_t>(1U), 1U);
}

TEST(MaskSizeTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint64_t>(64U), 1U);
}

TEST(MaskSizeTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint8_t>(101U), 13U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint16_t>(101U), 7U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint32_t>(101U), 4U);
	EXPECT_EQ(koszy::collections::hash::mask::maskSize<std::uint64_t>(101U), 2U);
}


// Mask Pos

TEST(MaskPosTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint64_t>(0U), 0U);
}

TEST(MaskPosTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint8_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint16_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint32_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint64_t>(1U), 0U);
}

TEST(MaskPosTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint64_t>(64U), 1U);
}

TEST(MaskPosTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint8_t>(101U), 12U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint16_t>(101U), 6U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint32_t>(101U), 3U);
	EXPECT_EQ(koszy::collections::hash::mask::maskPos<std::uint64_t>(101U), 1U);
}


// Mask Bit

TEST(MaskBitTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint64_t>(0U), 0U);
}

TEST(MaskBitTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<bool>(1U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint64_t>(1U), 1U);
}

TEST(MaskBitTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<bool>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint8_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint16_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint32_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint64_t>(64U), 0U);
}

TEST(MaskBitTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<bool>(101U), 0U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint8_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint16_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint32_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::hash::mask::maskBit<std::uint64_t>(101U), 37U);
}


// HashMask

template<typename T>
class HashMaskTest : public testing::Test {
	public:
		using MaskType = std::tuple_element_t<0, T>;
		using AllocatorType = std::tuple_element_t<1, T>;

		koszy::collections::hash::mask::HashMask<MaskType, AllocatorType> mask_;
};


using HashMaskTypes = testing::Types<
	std::pair<bool, std::allocator<bool>>,
	std::pair<std::uint8_t, std::allocator<std::uint8_t>>,
	std::pair<std::uint16_t, std::allocator<std::uint16_t>>,
	std::pair<std::uint32_t, std::allocator<std::uint32_t>>,
	std::pair<std::uint64_t, std::allocator<std::uint64_t>>,
	std::pair<std::uintmax_t, std::allocator<std::uintmax_t>>,

	std::pair<bool, koszy::collections::StatefulAllocator<bool>>,
	std::pair<std::uint8_t, koszy::collections::StatefulAllocator<std::uint8_t>>,
	std::pair<std::uint16_t, koszy::collections::StatefulAllocator<std::uint16_t>>,
	std::pair<std::uint32_t, koszy::collections::StatefulAllocator<std::uint32_t>>,
	std::pair<std::uint64_t, koszy::collections::StatefulAllocator<std::uint64_t>>,
	std::pair<std::uintmax_t, koszy::collections::StatefulAllocator<std::uintmax_t>>
>;

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
	this->mask_.unset(0U);
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), false);
	}
	this->mask_.set(0U);

	size = 1024U;
	this->mask_.reset(size);

	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), false);
	}
	this->mask_.set(512U);
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), i == 512U);
	}
	this->mask_.unset(512U);
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(this->mask_.isSet(i), false);
	}
	this->mask_.set(512U);
}

TYPED_TEST(HashMaskTest, CopyConstruction) {
	using MaskType = typename TestFixture::MaskType;
	using AllocatorType = typename TestFixture::AllocatorType;

	std::size_t size{1U};
	this->mask_.reset(size);

	this->mask_.set(0U);
	koszy::collections::hash::mask::HashMask<MaskType, AllocatorType> copy1{this->mask_};
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(copy1.isSet(i), this->mask_.isSet(i));
	}

	size = 1024U;
	this->mask_.reset(size);

	this->mask_.set(512U);
	koszy::collections::hash::mask::HashMask<MaskType, AllocatorType> copy2{this->mask_};
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(copy2.isSet(i), this->mask_.isSet(i));
	}
}

TYPED_TEST(HashMaskTest, CopyAssignment) {
	using MaskType = typename TestFixture::MaskType;
	using AllocatorType = typename TestFixture::AllocatorType;

	std::size_t size{1U};
	this->mask_.reset(size);

	this->mask_.set(0U);
	koszy::collections::hash::mask::HashMask<MaskType, AllocatorType> copy1{};
	copy1 = this->mask_;
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(copy1.isSet(i), this->mask_.isSet(i));
	}

	size = 1024U;
	this->mask_.reset(size);

	this->mask_.set(512U);
	koszy::collections::hash::mask::HashMask<MaskType, AllocatorType> copy2{};
	copy2 = this->mask_;
	for (std::size_t i{0U}; i != size; ++i) {
		EXPECT_EQ(copy2.isSet(i), this->mask_.isSet(i));
	}
}
