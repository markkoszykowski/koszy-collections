#include <iostream>
#include <locale>
#include <unordered_map>

#include "koszy/open-hash-map.h"

int main(int argc, char** argv) {
	std::unordered_map<int, int> map{};
	koszy::collections::hash::OpenHashMap<std::string, std::string> map2{};

	std::cout << map.max_load_factor() << std::endl;
	std::cout << map.bucket_count() << std::endl;
	std::cout << (map.max_load_factor() * map.bucket_count()) << std::endl;

	std::cout.imbue(std::locale("en_US.UTF-8"));
	std::cout << sizeof(std::string{}) << std::endl;
	std::cout << sizeof(map2) << std::endl;
	std::cout << std::dec << map2.max_size() << std::endl;

	map2[""] = "test";
	std::cout << map2.at("") << std::endl;

	map2["test"] = "";
	std::cout << map2.at("test") << std::endl;


	map2["test"] = "test";
	std::cout << map2.at("test") << std::endl;


	map2["test1"] = "test1";
	std::cout << map2.at("test") << std::endl;
	std::cout << map2.at("test1") << std::endl;
	// std::cout << map2.at("test2") << std::endl;

	map2.clear();
	std::cout << map2.at("test") << std::endl;

	std::cout << "Hello, World!\n";
	return 0;
}
