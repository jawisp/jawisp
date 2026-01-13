.SILENT:
.PHONY: dev app clean

# Containers
all:

ifeq ($(dev),up)
all: dev-up
else ifeq ($(dev),stop)
all: dev-stop
else ifeq ($(dev),down)
all: dev-down
else ifeq ($(prod),up)
all: prod-up
else ifeq ($(prod),down)
all: prod-down
else ifeq ($(sonar),up)
all: sonar-up
else ifeq ($(sonar),down)
all: sonar-down
endif

prod-up:
	docker compose -f deployment/prod/compose.yml up -d --build
	docker image prune -a -f

prod-down:
	docker compose -f deployment/prod/compose.yml down

app:
	./scripts/watch.sh ./app/src ./jawisp/core/src ./jawisp/http/src ./jawisp/http-netty/src

dev:
	make -j1 app

clean:
	./gradlew clean
	find . -type d -name "build" -exec rm -rf {} +
	find . -type d -name "bin" -exec rm -rf {} +
	find . -type d -name ".settings" -exec rm -rf {} +
	find . -type f -name ".project" -exec rm -rf {} +
	find . -type f -name ".classpath" -exec rm -rf {} +

nativeRun:
	./gradlew nativeRun

test:
	./gradlew clean test
