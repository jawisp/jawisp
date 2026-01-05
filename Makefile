.SILENT:
.PHONY: dev app clean

app:
	./scripts/watch.sh ./app/src ./jawisp/core/src ./jawisp/http/src ./jawisp/http-netty/src 

dev:
	make -j1 app

clean:
	./gradlew clean
	rm -rf build

nativeRun:
	./gradlew nativeRun