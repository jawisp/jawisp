.SILENT:
.PHONY: dev app

app:
	./scripts/watch.sh ./app/src ./jawisp/core/src ./jawisp/http/src ./jawisp/http-netty/src 

dev:
	make -j1 app