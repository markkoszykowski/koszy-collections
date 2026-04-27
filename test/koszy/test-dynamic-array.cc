#include <memory>
#include <string>
#include <utility>

#include <gtest/gtest.h>

#include "koszy/dynamic-array.h"

#include "test/koszy/allocator.h"


// DynamicArray

template<typename T>
class DynamicArrayTest : public testing::Test {
	public:
		using ArrayType = std::tuple_element_t<0, T>;
		using AllocatorType = std::tuple_element_t<1, T>;

		AllocatorType allocator_{};
};

using DynamicArrayTypes = testing::Types<
	std::pair<std::string, std::allocator<std::string>>,
	std::pair<std::string, koszy::collections::StatefulAllocator<std::string>>,
	std::pair<std::string, koszy::collections::PropagatingStatefulAllocator<std::string>>
>;

TYPED_TEST_SUITE(DynamicArrayTest, DynamicArrayTypes);

TYPED_TEST(DynamicArrayTest, AddRemoveTest) {
	using ArrayType = typename TestFixture::ArrayType;
	using AllocatorType = typename TestFixture::AllocatorType;

	koszy::collections::array::DynamicArray<ArrayType, AllocatorType> array{128U, this->allocator_};
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), false);
	}
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 0U);
	ASSERT_EQ(array.empty(), true);

	array.insert(55U, "test");
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), i == 55U);
	}
	ASSERT_EQ(array[55U], std::string{"test"});
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 1U);
	ASSERT_EQ(array.empty(), false);

	array.emplace(77U, 5, 'a');
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), i == 55U || i == 77U);
	}
	ASSERT_EQ(array[77U], std::string{"aaaaa"});
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 2U);
	ASSERT_EQ(array.empty(), false);

	array[77U] = "bbbbb";
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), i == 55U || i == 77U);
	}
	ASSERT_EQ(array[77U], std::string{"bbbbb"});
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 2U);
	ASSERT_EQ(array.empty(), false);

	ASSERT_EQ(array.extract(77U), std::string{"bbbbb"});
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), i == 55U);
	}
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 1U);
	ASSERT_EQ(array.empty(), false);

	array.erase(55U);
	for (std::size_t i = 0U; i != 128U; ++i) {
		ASSERT_EQ(array.contains(i), false);
	}
	ASSERT_EQ(array.capacity(), 128U);
	ASSERT_EQ(array.size(), 0U);
	ASSERT_EQ(array.empty(), true);
}
