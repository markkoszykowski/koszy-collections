#include <memory>
#include <string>
#include <type_traits>
#include <utility>

#include <gtest/gtest.h>

#include "koszy/dynamic-array.h"

#include "test/koszy/allocator.h"


constexpr std::string_view LOREM_IPSUM{
	"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
	"tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, "
	"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo "
	"consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
	"dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non "
	"proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
};

// DynamicArray

template <typename T>
struct DynamicArrayTest : testing::Test {
	using ArrayType = std::tuple_element_t<0, T>;
	using AllocatorType = std::tuple_element_t<1, T>;
	using MaskType = std::uintptr_t;
	using MaskAllocatorType = std::allocator_traits<AllocatorType>::template rebind_alloc<MaskType>;

	MaskAllocatorType maskAllocator{};
	AllocatorType allocator{};
};

using DynamicArrayTypes = testing::Types<
	std::pair<std::string_view, std::allocator<std::string_view>>,
	std::pair<std::string_view, koszy::collections::GlobalAllocator<std::string_view>>,
	std::pair<std::string_view, koszy::collections::StatefulAllocator<std::string_view>>,
	std::pair<std::string_view, koszy::collections::PropagatingStatefulAllocator<std::string_view>>,

	std::pair<std::string, std::allocator<std::string>>,
	std::pair<std::string, koszy::collections::GlobalAllocator<std::string>>,
	std::pair<std::string, koszy::collections::StatefulAllocator<std::string>>,
	std::pair<std::string, koszy::collections::PropagatingStatefulAllocator<std::string>>
>;

TYPED_TEST_SUITE(DynamicArrayTest, DynamicArrayTypes);

TYPED_TEST(DynamicArrayTest, EmptyTest) {
	using ArrayType = TestFixture::ArrayType;
	using AllocatorType = TestFixture::AllocatorType;
	using MaskType = TestFixture::MaskType;
	using MaskAllocatorType = TestFixture::MaskAllocatorType;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, MaskAllocatorType>;
	using DynamicArray = koszy::collections::array::DynamicArray<ArrayType, AllocatorType>;

	{
		ArrayMask mask{this->maskAllocator};
		DynamicArray array{this->allocator};

		EXPECT_EQ(array.data(), nullptr);
		EXPECT_EQ(array.size(), 0U);

		DynamicArray::destroy(this->allocator, mask, array);
		ArrayMask::destroy(this->maskAllocator, mask);
	}

	{
		ArrayMask mask{this->maskAllocator, 0U};
		DynamicArray array{this->allocator, 0U};

		EXPECT_EQ(array.data(), nullptr);
		EXPECT_EQ(array.size(), 0U);

		DynamicArray::destroy(this->allocator, mask, array);
		ArrayMask::destroy(this->maskAllocator, mask);
	}
}

TYPED_TEST(DynamicArrayTest, InsertEraseTest) {
	using ArrayType = TestFixture::ArrayType;
	using AllocatorType = TestFixture::AllocatorType;
	using MaskType = TestFixture::MaskType;
	using MaskAllocatorType = TestFixture::MaskAllocatorType;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, MaskAllocatorType>;
	using DynamicArray = koszy::collections::array::DynamicArray<ArrayType, AllocatorType>;

	const std::size_t size{2048U};
	ArrayMask mask{this->maskAllocator, size};
	DynamicArray array{this->allocator, size};

	const std::size_t index{333U};

	array.insert(this->allocator, index, ArrayType{LOREM_IPSUM});
	mask.set(index);

	EXPECT_EQ(array[index], LOREM_IPSUM);

	array.erase(this->allocator, index);
	mask.unset(index);

	DynamicArray::destroy(this->allocator, mask, array);
	ArrayMask::destroy(this->maskAllocator, mask);
}

TYPED_TEST(DynamicArrayTest, MoveElementTest) {
	using ArrayType = TestFixture::ArrayType;
	using AllocatorType = TestFixture::AllocatorType;
	using MaskType = TestFixture::MaskType;
	using MaskAllocatorType = TestFixture::MaskAllocatorType;
	using ArrayMask = koszy::collections::mask::ArrayMask<MaskType, MaskAllocatorType>;
	using DynamicArray = koszy::collections::array::DynamicArray<ArrayType, AllocatorType>;

	const std::size_t size{2048U};
	ArrayMask mask{this->maskAllocator, size};
	DynamicArray array{this->allocator, size};

	const std::size_t index{333U};

	array.emplace(this->allocator, index, LOREM_IPSUM);
	mask.set(index);
	EXPECT_EQ(array[index], LOREM_IPSUM);

	const ArrayType result{std::move(array)[index]};
	EXPECT_EQ(result, LOREM_IPSUM);
	if constexpr (!std::is_trivially_copyable_v<ArrayType>) {
		EXPECT_EQ(array[index], ArrayType{});
	}

	DynamicArray::destroy(this->allocator, mask, array);
	ArrayMask::destroy(this->maskAllocator, mask);
}

TYPED_TEST(DynamicArrayTest, GuardTest) {
	using ArrayType = TestFixture::ArrayType;
	using AllocatorType = TestFixture::AllocatorType;
	using MaskType = TestFixture::MaskType;
	using MaskAllocatorType = TestFixture::MaskAllocatorType;
	using MaskGuard = koszy::collections::mask::Guard<MaskType, MaskAllocatorType>;
	using Guard = koszy::collections::array::Guard<ArrayType, AllocatorType, MaskType, MaskAllocatorType>;
	using DynamicArray = koszy::collections::array::DynamicArray<ArrayType, AllocatorType>;

	try {
		const std::size_t size{2048U};
		MaskGuard maskGuard{this->maskAllocator, size};
		Guard guard{this->allocator, maskGuard.mask, size};
		throw std::exception{};
	} catch (...) {}

	{
		const std::size_t size{2048U};
		MaskGuard maskGuard{this->maskAllocator, size};
		Guard guard{this->allocator, maskGuard.mask, size};
		DynamicArray array{guard.release()};
		DynamicArray::destroy(this->allocator, maskGuard.mask, array);
	}

	{
		const std::size_t size{2048U};
		MaskGuard maskGuard{this->maskAllocator, size};
		Guard guard{this->allocator, maskGuard.mask, size};
		Guard moveConstructed{std::move(guard)};
		Guard moveAssigned{this->allocator, maskGuard.mask, size};
		moveAssigned = std::move(moveConstructed);
	}
}

TYPED_TEST(DynamicArrayTest, RuleOfFive) {
	using ArrayType = TestFixture::ArrayType;
	using AllocatorType = TestFixture::AllocatorType;
	using DynamicArray = koszy::collections::array::DynamicArray<ArrayType, AllocatorType>;

	static_assert(std::is_trivially_copy_constructible_v<DynamicArray>);
	static_assert(std::is_trivially_move_constructible_v<DynamicArray>);

	static_assert(std::is_trivially_copy_assignable_v<DynamicArray>);
	static_assert(std::is_trivially_move_assignable_v<DynamicArray>);

	static_assert(std::is_trivially_destructible_v<DynamicArray>);
}
