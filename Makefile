INCLUDES := -iquote./include
CXXFLAGS := -save-temps -std=c++26 -Wconversion -Wpedantic -Wextra -Wall
LDFLAGS  :=
LDLIBS   :=

TESTINCLUDES := -iquote.
TESTCXXFLAGS := --coverage -fsanitize=address -fsanitize=leak -fsanitize=undefined -g -O0
TESTLDFLAGS  := -Wl,--wrap=__cxa_throw
TESTLDLIBS   := -lstdc++exp -lgtest

BUILD     := ./build
SRCDIR    := $(BUILD)/src
TESTDIR   := $(BUILD)/test
REPORTDIR := $(BUILD)/report

SRC  := $(shell find ./src -type f -name *.cc ! -name main.cc)
TEST := $(shell find ./test -type f -name *.cc ! -name main.cc)

SRCOBJ  := $(SRCDIR)/main.o
TESTOBJ := $(TESTDIR)/main.o

SRCOBJS  := $(patsubst ./src/%.cc,$(SRCDIR)/%.o,$(SRC))
TESTOBJS := $(patsubst ./src/%.cc,$(TESTDIR)/src/%.o,$(SRC)) \
			$(patsubst ./test/%.cc,$(TESTDIR)/test/%.o,$(TEST))

TARGET := $(SRCDIR)/main
TEST   := $(TESTDIR)/main


.DEFAULT_GOAL=all
.PHONY: all
all: test target


.PHONY: run
run: $(TARGET)
	$(TARGET)

.PHONY: target
target: $(TARGET)
$(TARGET): $(SRCOBJS) $(SRCOBJ)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) -o $(TARGET) $(SRCOBJ) $(SRCOBJS) $(LDLIBS)


.PHONY: test
test: $(TEST)
	$(TEST)

.PHONY: report
report: test
	mkdir -p $(REPORTDIR)
	gcovr --html-nested -o $(REPORTDIR)/ $(TESTDIR)/

$(TEST): $(TESTOBJS) $(TESTOBJ)
	$(CXX) $(CXXFLAGS) $(TESTCXXFLAGS) $(LDFLAGS) $(TESTLDFLAGS) -o $(TEST) $(TESTOBJ) $(TESTOBJS) $(LDLIBS) $(TESTLDLIBS)


define COMPILE_SRC
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) $(INCLUDES) -o $@ $<
endef

define COMPILE_TEST
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) $(TESTCXXFLAGS) $(INCLUDES) $(TESTINCLUDES) -o $@ $<
endef

$(SRCDIR)/%.o: ./src/%.cc
	$(COMPILE_SRC)

$(TESTDIR)/src/%.o: ./src/%.cc
	$(COMPILE_TEST)

$(TESTDIR)/test/%.o: ./test/%.cc
	$(COMPILE_TEST)

$(TESTDIR)/%.o: ./test/%.cc
	$(COMPILE_TEST)


.PHONY: clean
clean:
	rm -rf *~ $(BUILD)
