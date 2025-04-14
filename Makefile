IMAGE := ministryofjustice/tech-docs-github-pages-publisher:v3

authenticate-docker:
	./scripts/authenticate_docker.sh

build-dev:
	#docker compose pull hmpps-auth
	docker compose build

build:
	docker build -t hmpps-integration-api .

serve-dependencies:
	docker compose up prism local-stack-aws --build -d

analyse-dependencies:
	./gradlew dependencyCheckAnalyze --info

serve: build-dev
	docker compose up -d --wait

publish:
	./scripts/publish.sh

stop:
	docker compose down

unit-test:
	./gradlew unitTest

integration-test: serve
	./gradlew integrationTest --warning-mode all

heartbeat:
	./scripts/heartbeat.sh

smoke-tests:
	./scripts/smoke-tests.sh

test: unit-test

e2e:
	./gradlew integrationTest --warning-mode all

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

check:
	./gradlew check

generate-client-certificate:
	cd ./scripts/client_certificates && ./generate.sh

preview-docs:
	docker run --rm \
		-v $$(pwd)/tech-docs/config:/app/config \
		-v $$(pwd)/tech-docs/source:/app/source \
		-p 4567:4567 \
		-it $(IMAGE) /scripts/preview.sh

deploy-docs:
	docker run --rm \
		-v $$(pwd)/tech-docs/config:/app/config \
		-v $$(pwd)/tech-docs/source:/app/source \
		-it $(IMAGE) /scripts/deploy.sh

check-docs:
	docker run --rm \
		-v $$(pwd)/tech-docs/config:/app/config \
		-v $$(pwd)/tech-docs/source:/app/source \
		-it $(IMAGE) /scripts/check-url-links.sh

.PHONY: authenticate-docker build-dev test serve publish unit-test build lint preview-docs check-docs
