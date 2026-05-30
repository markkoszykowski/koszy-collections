#include <gtest/gtest.h>

#include "test/koszy/trace.h"

int main(int argc, char** argv) {
	koszy::trace::set_terminate();
	testing::InitGoogleTest(&argc, argv);
	return RUN_ALL_TESTS();
}
