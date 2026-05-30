INCLUDES := -iquote./include
CXXFLAGS := -save-temps -std=c++26 -Wconversion -Wpedantic -Wextra -Wall
LDFLAGS  :=
LDLIBS   :=

BUILD     := ./build
SRCDIR    := $(BUILD)/src
TESTDIR   := $(BUILD)/test
REPORTDIR := $(BUILD)/report

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

.PHONY: report
report: test
	mkdir -p $(REPORTDIR)
	gcovr --html-nested -o $(REPORTDIR)/

$(TEST): $(SRCOBJS) $(TESTOBJS) $(TESTOBJ)
	$(CXX) $(CXXFLAGS) --coverage -fsanitize=address -fsanitize=leak -fsanitize=undefined -g -O0 $(LDFLAGS) -o $(TEST) $(TESTOBJ) $(SRCOBJS) $(TESTOBJS) $(LDLIBS) -lstdc++exp -lgtest -Wl,--wrap=__cxa_throw


$(SRCDIR)/%.o: ./src/%.cc
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) $(INCLUDES) -o $@ $<

$(TESTDIR)/%.o: ./test/%.cc
	@mkdir -p $(@D)
	$(CXX) -c $(CXXFLAGS) --coverage -fsanitize=address -fsanitize=leak -fsanitize=undefined -g -O0 $(INCLUDES) -iquote. -o $@ $<


.PHONY: clean
clean:
	rm -rf *~ $(BUILD)
