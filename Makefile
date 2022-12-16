authenticate-docker:
	./scripts/authenticate_docker.sh

build-dev:
	docker-compose build

serve: build-dev
	docker-compose up -d

publish: build-dev
	./scripts/publish.sh

stop:
	docker-compose down

test: serve
	./gradlew test
	docker-compose exec client ./run_test.sh

.PHONY: authenticate-docker build-dev test serve publish
