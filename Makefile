INCLUDES := -iquote./include
CXXFLAGS := -std=c++23 -Wpedantic -Wextra -Wall -g
LDFLAGS  :=
LDLIBS   :=

BUILD   := ./build
SRCDIR  := $(BUILD)/src
TESTDIR := $(BUILD)/test

SRC  := $(shell find ./src -type f -name *.cc ! -name main.cc)
TEST := $(shell find ./test -type f -name *.cc ! -name main.cc)

SRCOBJ  := $(SRCDIR)/main.o
TESTOBJ := $(TESTDIR)/main.o

SRCOBJS  := $(patsubst ./src/%.cc,$(SRCDIR)/%.o,$(SRC))
TESTOBJS := $(patsubst ./test/%.cc,$(TESTDIR)/%.o,$(TEST))

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

$(TEST): $(SRCOBJS) $(TESTOBJS) $(TESTOBJ)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) -o $(TEST) $(TESTOBJ) $(SRCOBJS) $(TESTOBJS) $(LDLIBS) -lgtest


$(TESTDIR)/%.o: ./test/%.cc
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) $(INCLUDES) -o $@ $<

$(SRCDIR)/%.o: ./src/%.cc
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) $(INCLUDES) -o $@ $<


.PHONY: clean
clean:
	rm -rf *~ $(BUILD)
