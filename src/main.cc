#include <iostream>
#include <unordered_map>

#include "koszy/open-hash-map.h"

int main(int argc, char** argv) {
	std::unordered_map<int, int> map{};
	koszy::collections::hash::OpenHashMap<std::string, std::string> map2{};

	std::cout << "Hello, World!\n";
	return 0;
}
