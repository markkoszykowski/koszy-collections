#include <algorithm>
#include <memory>
#include <utility>

#include <gtest/gtest.h>

#include "koszy/array-mask.h"

#include "test/koszy/allocator.h"


// Mask Size

TEST(MaskSizeTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::mask::maskSize<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint64_t>(0U), 0U);
}

TEST(MaskSizeTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::mask::maskSize<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint64_t>(1U), 1U);
}

TEST(MaskSizeTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::mask::maskSize<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint64_t>(64U), 1U);
}

TEST(MaskSizeTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::mask::maskSize<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint8_t>(101U), 13U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint16_t>(101U), 7U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint32_t>(101U), 4U);
	EXPECT_EQ(koszy::collections::mask::maskSize<std::uint64_t>(101U), 2U);
}


// Mask Pos

TEST(MaskPosTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::mask::maskPos<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint64_t>(0U), 0U);
}

TEST(MaskPosTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::mask::maskPos<bool>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint8_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint16_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint32_t>(1U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint64_t>(1U), 0U);
}

TEST(MaskPosTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::mask::maskPos<bool>(64U), 64U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint8_t>(64U), 8U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint16_t>(64U), 4U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint32_t>(64U), 2U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint64_t>(64U), 1U);
}

TEST(MaskPosTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::mask::maskPos<bool>(101U), 101U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint8_t>(101U), 12U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint16_t>(101U), 6U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint32_t>(101U), 3U);
	EXPECT_EQ(koszy::collections::mask::maskPos<std::uint64_t>(101U), 1U);
}


// Mask Bit

TEST(MaskBitTest, HandlesZero) {
	EXPECT_EQ(koszy::collections::mask::maskBit<bool>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint8_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint16_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint32_t>(0U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint64_t>(0U), 0U);
}

TEST(MaskBitTest, HandlesOne) {
	EXPECT_EQ(koszy::collections::mask::maskBit<bool>(1U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint8_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint16_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint32_t>(1U), 1U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint64_t>(1U), 1U);
}

TEST(MaskBitTest, HandlesPowersOfTwo) {
	EXPECT_EQ(koszy::collections::mask::maskBit<bool>(64U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint8_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint16_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint32_t>(64U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint64_t>(64U), 0U);
}

TEST(MaskBitTest, HandlesOdds) {
	EXPECT_EQ(koszy::collections::mask::maskBit<bool>(101U), 0U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint8_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint16_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint32_t>(101U), 5U);
	EXPECT_EQ(koszy::collections::mask::maskBit<std::uint64_t>(101U), 37U);
}


// ArrayMask

template<typename T>
class ArrayMaskTest : public testing::Test {
	public:
		using MaskType = std::tuple_element_t<0, T>;
		using AllocatorType = std::tuple_element_t<1, T>;

		AllocatorType allocator_{};
		koszy::collections::mask::ArrayMask<MaskType, AllocatorType> mask_{this->allocator_};
};


using ArrayMaskTypes = testing::Types<
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
	std::pair<std::uintmax_t, koszy::collections::StatefulAllocator<std::uintmax_t>>,

	std::pair<bool, koszy::collections::PropagatingStatefulAllocator<bool>>,
	std::pair<std::uint8_t, koszy::collections::PropagatingStatefulAllocator<std::uint8_t>>,
	std::pair<std::uint16_t, koszy::collections::PropagatingStatefulAllocator<std::uint16_t>>,
	std::pair<std::uint32_t, koszy::collections::PropagatingStatefulAllocator<std::uint32_t>>,
	std::pair<std::uint64_t, koszy::collections::PropagatingStatefulAllocator<std::uint64_t>>,
	std::pair<std::uintmax_t, koszy::collections::PropagatingStatefulAllocator<std::uintmax_t>>
>;

TYPED_TEST_SUITE(ArrayMaskTest, ArrayMaskTypes);
TYPED_TEST(ArrayMaskTest, SetAndUnset) {
	auto test{
		[&](const std::size_t size, const std::size_t set) {
			this->mask_.reset(size);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(this->mask_.isSet(i), false);
			}
			this->mask_.set(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(this->mask_.isSet(i), i == set);
			}
			this->mask_.unset(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(this->mask_.isSet(i), false);
			}
		}
	};

	test(1U, 0U);
	test(1024U, 512U);
}

TYPED_TEST(ArrayMaskTest, CopyMoveConstruction) {
	using MaskType = typename TestFixture::MaskType;
	using AllocatorType = typename TestFixture::AllocatorType;

	auto test{
		[&](const std::size_t size, const std::size_t set) {
			this->mask_.reset(size);
			this->mask_.set(set);
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> copy{this->mask_};
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(copy.isSet(i), this->mask_.isSet(i));
			}
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> move{std::move(copy)};
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(move.isSet(i), this->mask_.isSet(i));
			}
		}
	};

	test(1U, 0U);
	test(1024U, 512U);
}

TYPED_TEST(ArrayMaskTest, CopyMoveAssignment) {
	using MaskType = typename TestFixture::MaskType;
	using AllocatorType = typename TestFixture::AllocatorType;

	auto testSize{
		[&](const std::size_t initial, const std::size_t size) {
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> copy{initial, this->allocator_};
			copy = this->mask_;
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(copy.isSet(i), this->mask_.isSet(i));
			}
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> move{initial, this->allocator_};
			move = std::move(copy);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(move.isSet(i), this->mask_.isSet(i));
			}
		}
	};
	auto test{
		[&](const std::size_t size, const std::size_t set) {
			this->mask_.reset(size);
			this->mask_.set(set);
			testSize(size, size);
			testSize(size + 1U, size);
		}
	};

	test(1U, 0U);
	test(1024U, 512U);
}


TYPED_TEST(ArrayMaskTest, SwapTest) {
	using MaskType = typename TestFixture::MaskType;
	using AllocatorType = typename TestFixture::AllocatorType;

	auto test{
		[&](const std::size_t leftSize, const std::size_t leftSet, const std::size_t rightSize, const std::size_t rightSet) {
			using std::swap;

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> left{leftSize, this->allocator_};
			left.set(leftSet);

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> right{rightSize, this->allocator_};
			right.set(rightSet);

			for (std::size_t i{0U}; i != leftSize; ++i) {
				EXPECT_EQ(left.isSet(i), i == leftSet);
			}
			for (std::size_t i{0U}; i != rightSize; ++i) {
				EXPECT_EQ(right.isSet(i), i == rightSet);
			}

			swap(left, right);

			for (std::size_t i{0U}; i != leftSize; ++i) {
				EXPECT_EQ(right.isSet(i), i == leftSet);
			}
			for (std::size_t i{0U}; i != rightSize; ++i) {
				EXPECT_EQ(left.isSet(i), i == rightSet);
			}

			swap(left, right);

			for (std::size_t i{0U}; i != leftSize; ++i) {
				EXPECT_EQ(left.isSet(i), i == leftSet);
			}
			for (std::size_t i{0U}; i != rightSize; ++i) {
				EXPECT_EQ(right.isSet(i), i == rightSet);
			}
		}
	};

	test(2U, 0U, 2U, 1U);
	test(1024U, 128U, 1024U, 256U);
	test(1U, 0U, 1024U, 512U);
}
