#include <algorithm>
#include <cstdint>
#include <exception>
#include <memory>
#include <optional>
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

template <typename T>
class ArrayMaskTest : public testing::Test {
	public:
		using MaskType = std::tuple_element_t<0, T>;
		using AllocatorType = std::tuple_element_t<1, T>;

		AllocatorType allocator_one_{};
		AllocatorType allocator_two_{};
};

using ArrayMaskTypes = testing::Types<
	std::pair<bool, std::allocator<bool>>,
	std::pair<std::uint8_t, std::allocator<std::uint8_t>>,
	std::pair<std::uint16_t, std::allocator<std::uint16_t>>,
	std::pair<std::uint32_t, std::allocator<std::uint32_t>>,
	std::pair<std::uint64_t, std::allocator<std::uint64_t>>,
	std::pair<std::uintmax_t, std::allocator<std::uintmax_t>>,

	std::pair<bool, koszy::collections::GlobalAllocator<bool>>,
	std::pair<std::uint8_t, koszy::collections::GlobalAllocator<std::uint8_t>>,
	std::pair<std::uint16_t, koszy::collections::GlobalAllocator<std::uint16_t>>,
	std::pair<std::uint32_t, koszy::collections::GlobalAllocator<std::uint32_t>>,
	std::pair<std::uint64_t, koszy::collections::GlobalAllocator<std::uint64_t>>,
	std::pair<std::uintmax_t, koszy::collections::GlobalAllocator<std::uintmax_t>>,

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
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;

	auto test{
		[&](const std::size_t size, const std::size_t set) {
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> mask{this->allocator_one_, size};
			mask.set(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), i == set);
			}
			mask.unset(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), false);
			}
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(this->allocator_one_, mask);
		}
	};

	test(1U, 0U);
	test(1024U, 512U);
}

TYPED_TEST(ArrayMaskTest, Copy) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;

	auto testCopy{
		[&](AllocatorType& allocator, const std::size_t size, const std::optional<std::size_t> set, AllocatorType& allocatorCopy, const std::size_t sizeCopy, const std::optional<std::size_t> setCopy) {
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> mask{allocator, size};
			if (set.has_value()) {
				mask.set(set.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> copy{allocatorCopy, sizeCopy};
			if (setCopy.has_value()) {
				copy.set(setCopy.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::copy(allocatorCopy, copy, allocator, mask);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(copy.isSet(i), set.has_value() && i == set.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocatorCopy, copy);
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocator, mask);
		}
	};

	auto test{
		[&](const std::size_t size, const std::optional<std::size_t> set, const std::size_t sizeCopy, const std::optional<std::size_t> setCopy) {
			testCopy(this->allocator_one_, size, set, this->allocator_one_, sizeCopy, setCopy);
			AllocatorType one{this->allocator_one_};
			AllocatorType two{this->allocator_two_};
			testCopy(one, size, set, two, sizeCopy, setCopy);
		}
	};

	test(1U, 0U, 0U, std::nullopt);
	test(1U, 0U, 1U, 0U);
	test(1U, 0U, 1024U, 896U);

	test(32U, 15U, 0U, std::nullopt);
	test(32U, 15U, 1U, 0U);
	test(32U, 15U, 1024U, 896U);

	test(1024U, 128U, 0U, std::nullopt);
	test(1024U, 128U, 1U, 0U);
	test(1024U, 128U, 1024U, 896U);
	test(1024U, 128U, 2048U, 1024U);
}

TYPED_TEST(ArrayMaskTest, Move) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;

	auto testMove{
		[&](AllocatorType& allocator, const std::size_t size, const std::optional<std::size_t> set, AllocatorType& allocatorMove, const std::size_t sizeMove, const std::optional<std::size_t> setMove) {
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> mask{allocator, size};
			if (set.has_value()) {
				mask.set(set.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> move{allocatorMove, sizeMove};
			if (setMove.has_value()) {
				move.set(setMove.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::move(allocatorMove, move, std::move(allocator), std::move(mask));
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(move.isSet(i), set.has_value() && i == set.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocatorMove, move);
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocator, mask);
		}
	};

	auto test{
		[&](const std::size_t size, const std::optional<std::size_t> set, const std::size_t sizeMove, const std::optional<std::size_t> setMove) {
			testMove(this->allocator_one_, size, set, this->allocator_one_, sizeMove, setMove);
			AllocatorType one{this->allocator_one_};
			AllocatorType two{this->allocator_two_};
			testMove(one, size, set, two, sizeMove, setMove);
		}
	};

	test(1U, 0U, 0U, std::nullopt);
	test(1U, 0U, 1U, 0U);
	test(1U, 0U, 1024U, 896U);

	test(32U, 15U, 0U, std::nullopt);
	test(32U, 15U, 1U, 0U);
	test(32U, 15U, 1024U, 896U);

	test(1024U, 128U, 0U, std::nullopt);
	test(1024U, 128U, 1U, 0U);
	test(1024U, 128U, 1024U, 896U);
	test(1024U, 128U, 2048U, 1024U);
}

TYPED_TEST(ArrayMaskTest, SwapTest) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;

	auto testSwap{
		[&](AllocatorType& allocatorLeft, const std::size_t sizeLeft, const std::optional<std::size_t> setLeft, AllocatorType& allocatorRight, const std::size_t sizeRight, const std::optional<std::size_t> setRight) {
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> left{allocatorLeft, sizeLeft};
			if (setLeft.has_value()) {
				left.set(setLeft.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType> right{allocatorRight, sizeRight};
			if (setRight.has_value()) {
				right.set(setRight.value());
			}

			for (std::size_t i{0U}; i != sizeLeft; ++i) {
				EXPECT_EQ(left.isSet(i), setLeft.has_value() && i == setLeft.value());
			}
			for (std::size_t i{0U}; i != sizeRight; ++i) {
				EXPECT_EQ(right.isSet(i), setRight.has_value() && i == setRight.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::swap(allocatorLeft, left, allocatorRight, right);

			for (std::size_t i{0U}; i != sizeLeft; ++i) {
				EXPECT_EQ(right.isSet(i), setLeft.has_value() && i == setLeft.value());
			}
			for (std::size_t i{0U}; i != sizeRight; ++i) {
				EXPECT_EQ(left.isSet(i), setRight.has_value() && i == setRight.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::swap(allocatorLeft, left, allocatorRight, right);

			for (std::size_t i{0U}; i != sizeLeft; ++i) {
				EXPECT_EQ(left.isSet(i), setLeft.has_value() && i == setLeft.value());
			}
			for (std::size_t i{0U}; i != sizeRight; ++i) {
				EXPECT_EQ(right.isSet(i), setRight.has_value() && i == setRight.value());
			}

			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocatorRight, right);
			koszy::collections::mask::ArrayMask<MaskType, AllocatorType>::destroy(allocatorLeft, left);
		}
	};

	auto test{
		[&](const std::size_t sizeLeft, const std::optional<std::size_t> setLeft, const std::size_t sizeRight, const std::optional<std::size_t> setRight) {
			testSwap(this->allocator_one_, sizeLeft, setLeft, this->allocator_one_, sizeRight, setRight);
			AllocatorType one{this->allocator_one_};
			AllocatorType two{this->allocator_two_};
			testSwap(one, sizeLeft, setLeft, two, sizeRight, setRight);
		}
	};

	test(2U, 0U, 2U, 1U);
	test(1024U, 128U, 1024U, 256U);
	test(1U, 0U, 1024U, 512U);
	test(1024U, 128U, 2048U, 256U);
}

TYPED_TEST(ArrayMaskTest, GuardTest) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;

	try {
		koszy::collections::mask::Guard<MaskType, AllocatorType> guard{this->allocator_one_, 2048U};
		throw std::exception{};
	} catch (...) {}
}

TEST(ArrayMaskTest, RuleOfFive) {
	constexpr bool copyConstructible{std::is_trivially_copy_constructible_v<koszy::collections::mask::ArrayMask<unsigned int>>};
	EXPECT_TRUE(copyConstructible);
	constexpr bool moveConstructible{std::is_trivially_move_constructible_v<koszy::collections::mask::ArrayMask<unsigned int>>};
	EXPECT_TRUE(moveConstructible);

	constexpr bool copyAssignable{std::is_trivially_copy_assignable_v<koszy::collections::mask::ArrayMask<unsigned int>>};
	EXPECT_TRUE(copyAssignable);
	constexpr bool moveAssignable{std::is_trivially_move_assignable_v<koszy::collections::mask::ArrayMask<unsigned int>>};
	EXPECT_TRUE(moveAssignable);

	constexpr bool destructible{std::is_trivially_destructible_v<koszy::collections::mask::ArrayMask<unsigned int>>};
	EXPECT_TRUE(destructible);
}
