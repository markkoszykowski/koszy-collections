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
struct ArrayMaskTest : testing::Test {
	using MaskType = std::tuple_element_t<0, T>;
	using AllocatorType = std::tuple_element_t<1, T>;

	AllocatorType allocatorOne{};
	AllocatorType allocatorTwo{};
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
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	auto test{
		[&](const std::size_t size, const std::size_t set) {
			ArrayMask mask{this->allocatorOne, size};
			mask.set(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), i == set);
			}
			mask.unset(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), false);
			}
			mask.toggle(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), i == set);
			}
			mask.toggle(set);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(mask.isSet(i), false);
			}
			ArrayMask::destroy(this->allocatorOne, mask);
		}
	};

	test(1U, 0U);
	test(1024U, 512U);
}

TYPED_TEST(ArrayMaskTest, Copy) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	auto testCopy{
		[&](AllocatorType& allocator, const std::size_t size, const std::optional<std::size_t> set, AllocatorType& allocatorCopy, const std::size_t sizeCopy, const std::optional<std::size_t> setCopy) {
			ArrayMask mask{allocator, size};
			if (set.has_value()) {
				mask.set(set.value());
			}

			ArrayMask copy{allocatorCopy, sizeCopy};
			if (setCopy.has_value()) {
				copy.set(setCopy.value());
			}

			ArrayMask::copy(allocatorCopy, copy, allocator, mask);
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(copy.isSet(i), set.has_value() && i == set.value());
			}

			ArrayMask::destroy(allocatorCopy, copy);
			ArrayMask::destroy(allocator, mask);
		}
	};

	auto test{
		[&](const std::size_t size, const std::optional<std::size_t> set, const std::size_t sizeCopy, const std::optional<std::size_t> setCopy) {
			testCopy(this->allocatorOne, size, set, this->allocatorOne, sizeCopy, setCopy);
			AllocatorType one{this->allocatorOne};
			AllocatorType two{this->allocatorTwo};
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
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	auto testMove{
		[&](AllocatorType& allocator, const std::size_t size, const std::optional<std::size_t> set, AllocatorType& allocatorMove, const std::size_t sizeMove, const std::optional<std::size_t> setMove) {
			ArrayMask mask{allocator, size};
			if (set.has_value()) {
				mask.set(set.value());
			}

			ArrayMask move{allocatorMove, sizeMove};
			if (setMove.has_value()) {
				move.set(setMove.value());
			}

			ArrayMask::move(allocatorMove, move, std::move(allocator), std::move(mask));
			for (std::size_t i{0U}; i != size; ++i) {
				EXPECT_EQ(move.isSet(i), set.has_value() && i == set.value());
			}

			ArrayMask::destroy(allocatorMove, move);
			ArrayMask::destroy(allocator, mask);
		}
	};

	auto test{
		[&](const std::size_t size, const std::optional<std::size_t> set, const std::size_t sizeMove, const std::optional<std::size_t> setMove) {
			testMove(this->allocatorOne, size, set, this->allocatorOne, sizeMove, setMove);
			AllocatorType one{this->allocatorOne};
			AllocatorType two{this->allocatorTwo};
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
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	auto testSwap{
		[&](AllocatorType& allocatorLeft, const std::size_t sizeLeft, const std::optional<std::size_t> setLeft, AllocatorType& allocatorRight, const std::size_t sizeRight, const std::optional<std::size_t> setRight) {
			ArrayMask left{allocatorLeft, sizeLeft};
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

			ArrayMask::swap(allocatorLeft, left, allocatorRight, right);

			for (std::size_t i{0U}; i != sizeLeft; ++i) {
				EXPECT_EQ(right.isSet(i), setLeft.has_value() && i == setLeft.value());
			}
			for (std::size_t i{0U}; i != sizeRight; ++i) {
				EXPECT_EQ(left.isSet(i), setRight.has_value() && i == setRight.value());
			}

			ArrayMask::swap(allocatorLeft, left, allocatorRight, right);

			for (std::size_t i{0U}; i != sizeLeft; ++i) {
				EXPECT_EQ(left.isSet(i), setLeft.has_value() && i == setLeft.value());
			}
			for (std::size_t i{0U}; i != sizeRight; ++i) {
				EXPECT_EQ(right.isSet(i), setRight.has_value() && i == setRight.value());
			}

			ArrayMask::destroy(allocatorRight, right);
			ArrayMask::destroy(allocatorLeft, left);
		}
	};

	auto test{
		[&](const std::size_t sizeLeft, const std::optional<std::size_t> setLeft, const std::size_t sizeRight, const std::optional<std::size_t> setRight) {
			testSwap(this->allocatorOne, sizeLeft, setLeft, this->allocatorOne, sizeRight, setRight);
			AllocatorType one{this->allocatorOne};
			AllocatorType two{this->allocatorTwo};
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
	using Guard = koszy::collections::mask::Guard<MaskType, AllocatorType>;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	try {
		Guard guard{this->allocatorOne, 2048U};
		throw std::exception{};
	} catch (...) {}

	{
		Guard guard{this->allocatorOne, 2048U};
		ArrayMask mask{guard.release()};
		ArrayMask::destroy(this->allocatorOne, mask);
	}

	{
		Guard guard{this->allocatorOne, 2048U};
		Guard moveConstructed{std::move(guard)};
		Guard moveAssigned{this->allocatorOne, 2048U};
		moveAssigned = std::move(moveConstructed);
	}
}

TYPED_TEST(ArrayMaskTest, RuleOfFive) {
	using MaskType = TestFixture::MaskType;
	using AllocatorType = TestFixture::AllocatorType;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, AllocatorType>;

	static_assert(std::is_trivially_copy_constructible_v<ArrayMask>);
	static_assert(std::is_trivially_move_constructible_v<ArrayMask>);

	static_assert(std::is_trivially_copy_assignable_v<ArrayMask>);
	static_assert(std::is_trivially_move_assignable_v<ArrayMask>);

	static_assert(std::is_trivially_destructible_v<ArrayMask>);
}
